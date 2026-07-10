package no.reliablesolutions.release_notes_portal.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import no.reliablesolutions.release_notes_portal.domain.entity.GitRepository;
import no.reliablesolutions.release_notes_portal.domain.entity.ReleaseNote;
import no.reliablesolutions.release_notes_portal.runner.ChangeNotesSyncHandler;

class ReleaseNoteSyncHandlerTest {

  @TempDir
  Path tempDir;

  private ReleaseNoteSyncHandler handler;
  private Path remoteDir;
  private Path cloneDir;
  private GitRepository gitRepository;
  private ReleaseNote releaseNote;

  @BeforeEach
  void setUp() {
    handler = new ReleaseNoteSyncHandler("release_notes", mock(ChangeNotesSyncHandler.class));

    remoteDir = tempDir.resolve("remote");
    cloneDir = tempDir.resolve("clone");
    // Override the local path so the test clone stays inside the temp directory instead of git_repositories/<name>
    gitRepository = new GitRepository() {
      @Override
      public String getLocalPath() {
        return cloneDir.toString();
      }
    };
    gitRepository.setName("release-note-sync-test-repo");
    gitRepository.setUrl(remoteDir.toUri().toString());
    gitRepository.setChangeNoteDirectory("notes");
    gitRepository.setPat("dummy-pat"); // required by the push gate; the local file transport ignores credentials

    releaseNote = new ReleaseNote();
    releaseNote.setId(1L);
    releaseNote.setTag("v1.0.0");
    releaseNote.setChangeNotes(List.of());
  }

  private Git initRemoteAndClone() throws Exception {
    Git remote = Git.init().setDirectory(remoteDir.toFile()).setInitialBranch("main").call();
    Files.writeString(remoteDir.resolve("README.md"), "readme");
    remote.add().addFilepattern("README.md").call();
    remote.commit().setMessage("initial commit")
        .setAuthor("Test", "test@example.com").setCommitter("Test", "test@example.com").call();
    Git.cloneRepository().setURI(remoteDir.toUri().toString()).setDirectory(cloneDir.toFile()).call().close();
    return remote;
  }

  @Test
  void successfulSyncPushesReleaseNoteFileToPortalBranch() throws Exception {
    try (Git remote = initRemoteAndClone()) {

      boolean success = handler.syncReleaseNoteToGit(releaseNote, List.of(gitRepository));

      assertTrue(success);
      ObjectId branchId = remote.getRepository().resolve("refs/heads/release-notes-portal-1");
      assertNotNull(branchId);
      try (RevWalk revWalk = new RevWalk(remote.getRepository())) {
        RevCommit branchCommit = revWalk.parseCommit(branchId);
        try (TreeWalk treeWalk = TreeWalk.forPath(remote.getRepository(), "release_notes/release_note_1.yml", branchCommit.getTree())) {
          assertNotNull(treeWalk);
        }
      }
    }
  }

  @Test
  void rejectedPushFailsSync() throws Exception {
    try (Git remote = initRemoteAndClone()) {
      // a diverging release-notes-portal-1 branch on the remote makes the push a rejected non-fast-forward
      remote.checkout().setCreateBranch(true).setName("release-notes-portal-1").call();
      Files.writeString(remoteDir.resolve("unrelated.txt"), "unrelated");
      remote.add().addFilepattern("unrelated.txt").call();
      remote.commit().setMessage("diverging commit")
          .setAuthor("Test", "test@example.com").setCommitter("Test", "test@example.com").call();
      remote.checkout().setName("main").call();

      boolean success = handler.syncReleaseNoteToGit(releaseNote, List.of(gitRepository));

      assertFalse(success);
    }
  }
}
