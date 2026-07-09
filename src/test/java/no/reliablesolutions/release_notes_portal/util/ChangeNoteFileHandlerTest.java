package no.reliablesolutions.release_notes_portal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.domain.entity.Product;
import no.reliablesolutions.release_notes_portal.exception.InvalidChangeNoteYamlException;
import no.reliablesolutions.release_notes_portal.service.CustomerService;
import no.reliablesolutions.release_notes_portal.service.FeatureService;
import no.reliablesolutions.release_notes_portal.service.ProductService;
import no.reliablesolutions.release_notes_portal.service.ScopeService;

class ChangeNoteFileHandlerTest {

  @TempDir
  Path tempDir;

  private ScopeService scopeService;
  private ProductService productService;
  private FeatureService featureService;
  private CustomerService customerService;
  private ChangeNoteFileHandler changeNoteFileHandler;

  @BeforeEach
  void setUp() {
    scopeService = mock(ScopeService.class);
    productService = mock(ProductService.class);
    featureService = mock(FeatureService.class);
    customerService = mock(CustomerService.class);
    when(scopeService.getScopeByName(anyString())).thenReturn(List.of());
    when(productService.getProductByName(anyString())).thenReturn(List.of());
    when(featureService.getFeatureByName(anyString())).thenReturn(List.of());
    when(customerService.getCustomerByName(anyString())).thenReturn(List.of());
    changeNoteFileHandler = new ChangeNoteFileHandler(scopeService, productService, featureService, customerService);
  }

  private File writeChangeNoteFile(String content) throws IOException {
    Path file = tempDir.resolve("change-note.yaml");
    Files.writeString(file, content);
    return file.toFile();
  }

  @Test
  void unmatchedProductIsKeptInDescription() throws IOException, InvalidChangeNoteYamlException {
    File file = writeChangeNoteFile("""
        product: PMS
        change: Fixed a bug
        """);

    ChangeNote changeNote = changeNoteFileHandler.getChangeNoteFromFile(file);

    assertNull(changeNote.getProduct());
    assertEquals("Fixed a bug\n\n{{Product: PMS}}", changeNote.getDescription());
  }

  @Test
  void matchedProductIsNotKeptInDescription() throws IOException, InvalidChangeNoteYamlException {
    Product product = new Product();
    product.setName("PMS");
    when(productService.getProductByName("PMS")).thenReturn(List.of(product));
    File file = writeChangeNoteFile("""
        product: PMS
        change: Fixed a bug
        """);

    ChangeNote changeNote = changeNoteFileHandler.getChangeNoteFromFile(file);

    assertEquals(product, changeNote.getProduct());
    assertEquals("Fixed a bug", changeNote.getDescription());
  }

  @Test
  void multipleUnmatchedTagsAreKeptInDescription() throws IOException, InvalidChangeNoteYamlException {
    File file = writeChangeNoteFile("""
        scope: Backend
        product: PMS
        customer: Acme
        change: Fixed a bug
        """);

    ChangeNote changeNote = changeNoteFileHandler.getChangeNoteFromFile(file);

    assertEquals("Fixed a bug\n\n{{Scope: Backend}} {{Product: PMS}} {{Customer: Acme}}", changeNote.getDescription());
  }

  @Test
  void unmatchedTagBecomesDescriptionWhenChangeIsMissing() throws IOException, InvalidChangeNoteYamlException {
    File file = writeChangeNoteFile("""
        product: PMS
        """);

    ChangeNote changeNote = changeNoteFileHandler.getChangeNoteFromFile(file);

    assertEquals("{{Product: PMS}}", changeNote.getDescription());
  }

  @Test
  void descriptionIsUnchangedWithoutTags() throws IOException, InvalidChangeNoteYamlException {
    File file = writeChangeNoteFile("""
        change: Fixed a bug
        """);

    ChangeNote changeNote = changeNoteFileHandler.getChangeNoteFromFile(file);

    assertEquals("Fixed a bug", changeNote.getDescription());
  }

  @Test
  void unknownKeyIsKeptInDescription() throws IOException, InvalidChangeNoteYamlException {
    File file = writeChangeNoteFile("""
        change: Fixed a bug
        severity: high
        """);

    ChangeNote changeNote = changeNoteFileHandler.getChangeNoteFromFile(file);

    assertEquals("Fixed a bug\n\n{{severity: high}}", changeNote.getDescription());
  }

  @Test
  void unmatchedTagAndUnknownKeyAreBothKeptInDescription() throws IOException, InvalidChangeNoteYamlException {
    File file = writeChangeNoteFile("""
        product: PMS
        change: Fixed a bug
        severity: high
        release: 1.2.3
        """);

    ChangeNote changeNote = changeNoteFileHandler.getChangeNoteFromFile(file);

    assertEquals("Fixed a bug\n\n{{Product: PMS}} {{severity: high}} {{release: 1.2.3}}", changeNote.getDescription());
  }
}
