package no.reliablesolutions.release_notes_portal.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.domain.repository.GitRepositoryRepository;
import no.reliablesolutions.release_notes_portal.service.ChangeNoteService;
import no.reliablesolutions.release_notes_portal.util.ChangeNoteFileHandler;

class ChangeNotesSyncHandlerTest {

  @TempDir
  Path tempDir;

  private GitRepositoryRepository gitRepositoryRepository;
  private ChangeNoteService changeNoteService;
  private ChangeNoteFileHandler changeNoteFileHandler;
  private ChangeNotesSyncHandler handler;
  private Path remoteDir;
  private Path cloneDir;
  private GitRepository gitRepository;

  @BeforeEach
  void setUp() throws Exception {
    gitRepositoryRepository = mock(GitRepositoryRepository.class);
    changeNoteService = mock(ChangeNoteService.class);
    changeNoteFileHandler = mock(ChangeNoteFileHandler.class);
    when(changeNoteFileHandler.getChangeNoteFromFile(any(File.class))).thenAnswer(invocation -> new ChangeNote());
    handler = new ChangeNotesSyncHandler(gitRepositoryRepository, changeNoteService, changeNoteFileHandler);

    remoteDir = tempDir.resolve("remote");
    cloneDir = tempDir.resolve("clone");
    // Override the local path so the test clone stays inside the temp directory instead of git_repositories/<name>
    gitRepository = new GitRepository() {
      @Override
      public String getLocalPath() {
        return cloneDir.toString();
      }
    };
    gitRepository.setName("sync-test-repo");
    gitRepository.setUrl(remoteDir.toUri().toString());
    gitRepository.setChangeNoteDirectory("notes");
  }

  private Git initRemote() throws Exception {
    return Git.init().setDirectory(remoteDir.toFile()).setInitialBranch("main").call();
  }

  private RevCommit commitFiles(Git remote, String message, String... paths) throws Exception {
    for (String path : paths) {
      Path file = remoteDir.resolve(path);
      Files.createDirectories(file.getParent());
      Files.writeString(file, "change: Change from " + path);
      remote.add().addFilepattern(path).call();
    }
    return remote.commit().setMessage(message)
        .setAuthor("Test", "test@example.com").setCommitter("Test", "test@example.com").call();
  }

  private ObjectId mergeIntoMain(Git remote, String branch, String message) throws Exception {
    remote.checkout().setName("main").call();
    return remote.merge().include(remote.getRepository().resolve(branch))
        .setFastForward(MergeCommand.FastForwardMode.NO_FF).setMessage(message).call().getNewHead();
  }

  @Test
  void noteFileAddedOnMergedBranchCreatesExactlyOneChangeNote() throws Exception {
    try (Git remote = initRemote()) {
      commitFiles(remote, "initial commit", "README.md");
      remote.checkout().setCreateBranch(true).setName("feature").call();
      commitFiles(remote, "add change note", "notes/feature-note.yaml");
      remote.checkout().setName("main").call();
      commitFiles(remote, "unrelated change on main", "src/Main.java");
      ObjectId mergeCommitId = mergeIntoMain(remote, "feature", "merge feature into main");

      handler.syncGitRepository(gitRepository);

      ArgumentCaptor<ChangeNote> captor = ArgumentCaptor.forClass(ChangeNote.class);
      verify(changeNoteService, times(1)).updateChangeNote(captor.capture());
      assertEquals(mergeCommitId.getName(), captor.getValue().getGitCommitHash());
      assertEquals("notes/feature-note.yaml", captor.getValue().getGitFilePath());
    }
  }

  @Test
  void mergeCommitWithMultipleNoteFilesCreatesOneChangeNotePerFile() throws Exception {
    try (Git remote = initRemote()) {
      commitFiles(remote, "initial commit", "README.md");
      remote.checkout().setCreateBranch(true).setName("feature").call();
      commitFiles(remote, "add two change notes", "notes/a.yaml", "notes/b.yaml");
      commitFiles(remote, "add another change note", "notes/c.yml");
      ObjectId mergeCommitId = mergeIntoMain(remote, "feature", "merge feature into main");

      handler.syncGitRepository(gitRepository);

      ArgumentCaptor<ChangeNote> captor = ArgumentCaptor.forClass(ChangeNote.class);
      verify(changeNoteService, times(3)).updateChangeNote(captor.capture());
      assertEquals(Set.of(mergeCommitId.getName()),
          captor.getAllValues().stream().map(ChangeNote::getGitCommitHash).collect(Collectors.toSet()));
      assertEquals(Set.of("notes/a.yaml", "notes/b.yaml", "notes/c.yml"),
          captor.getAllValues().stream().map(ChangeNote::getGitFilePath).collect(Collectors.toSet()));
    }
  }

  @Test
  void secondSyncOnlyProcessesCommitsAfterLastCheckedCommit() throws Exception {
    try (Git remote = initRemote()) {
      commitFiles(remote, "initial commit", "README.md");
      RevCommit firstNoteCommit = commitFiles(remote, "add first change note", "notes/one.yaml");

      handler.syncGitRepository(gitRepository);

      assertEquals(firstNoteCommit.getName(), gitRepository.getLastCheckedCommitHash());
      RevCommit secondNoteCommit = commitFiles(remote, "add second change note", "notes/two.yaml");

      handler.syncGitRepository(gitRepository);

      ArgumentCaptor<ChangeNote> captor = ArgumentCaptor.forClass(ChangeNote.class);
      verify(changeNoteService, times(2)).updateChangeNote(captor.capture());
      assertEquals(secondNoteCommit.getName(), captor.getAllValues().get(1).getGitCommitHash());
      assertEquals("notes/two.yaml", captor.getAllValues().get(1).getGitFilePath());
      assertEquals(secondNoteCommit.getName(), gitRepository.getLastCheckedCommitHash());
    }
  }

  @Test
  void noteFileCommittedDirectlyToMainCreatesOneChangeNote() throws Exception {
    try (Git remote = initRemote()) {
      commitFiles(remote, "initial commit", "README.md");
      RevCommit noteCommit = commitFiles(remote, "add change note", "notes/direct.yaml");

      handler.syncGitRepository(gitRepository);

      ArgumentCaptor<ChangeNote> captor = ArgumentCaptor.forClass(ChangeNote.class);
      verify(changeNoteService, times(1)).updateChangeNote(captor.capture());
      assertEquals(noteCommit.getName(), captor.getValue().getGitCommitHash());
      assertEquals("notes/direct.yaml", captor.getValue().getGitFilePath());
      assertEquals(gitRepository, captor.getValue().getGitRepository());
      assertNotNull(captor.getValue().getGitCommitTimestamp());
    }
  }

  @Test
  void commitsWithoutNoteFilesAdvanceLastCheckedCommit() throws Exception {
    try (Git remote = initRemote()) {
      commitFiles(remote, "initial commit", "README.md");
      RevCommit headCommit = commitFiles(remote, "unrelated change", "src/Main.java");

      handler.syncGitRepository(gitRepository);

      verify(changeNoteService, never()).updateChangeNote(any());
      assertEquals(headCommit.getName(), gitRepository.getLastCheckedCommitHash());
    }
  }

  @Test
  void noteFileInRootCommitCreatesOneChangeNote() throws Exception {
    try (Git remote = initRemote()) {
      RevCommit rootCommit = commitFiles(remote, "initial commit with change note", "README.md", "notes/initial.yaml");

      handler.syncGitRepository(gitRepository);

      ArgumentCaptor<ChangeNote> captor = ArgumentCaptor.forClass(ChangeNote.class);
      verify(changeNoteService, times(1)).updateChangeNote(captor.capture());
      assertEquals(rootCommit.getName(), captor.getValue().getGitCommitHash());
      assertEquals("notes/initial.yaml", captor.getValue().getGitFilePath());
    }
  }

  @Test
  void syncWithoutNewCommitsCreatesNothingAndKeepsLastCheckedCommit() throws Exception {
    try (Git remote = initRemote()) {
      commitFiles(remote, "initial commit", "README.md");
      RevCommit noteCommit = commitFiles(remote, "add change note", "notes/only.yaml");

      handler.syncGitRepository(gitRepository);
      handler.syncGitRepository(gitRepository);

      verify(changeNoteService, times(1)).updateChangeNote(any());
      assertEquals(noteCommit.getName(), gitRepository.getLastCheckedCommitHash());
    }
  }
}
