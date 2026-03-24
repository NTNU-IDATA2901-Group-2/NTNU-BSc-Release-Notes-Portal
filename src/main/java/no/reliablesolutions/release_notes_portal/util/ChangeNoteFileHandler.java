package no.reliablesolutions.release_notes_portal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import lombok.AllArgsConstructor;
import no.reliablesolutions.release_notes_portal.domain.entity.ChangeNote;
import no.reliablesolutions.release_notes_portal.domain.entity.Customer;
import no.reliablesolutions.release_notes_portal.domain.entity.Feature;
import no.reliablesolutions.release_notes_portal.domain.entity.Product;
import no.reliablesolutions.release_notes_portal.domain.entity.Scope;
import no.reliablesolutions.release_notes_portal.exception.InvalidChangeNoteYamlException;
import no.reliablesolutions.release_notes_portal.service.CustomerService;
import no.reliablesolutions.release_notes_portal.service.FeatureService;
import no.reliablesolutions.release_notes_portal.service.ProductService;
import no.reliablesolutions.release_notes_portal.service.ScopeService;

/**
 * Utility class for handling change note files. This class is responsible for parsing change note YAML files and creating ChangeNote entities from the data in the files.
 */
@Component
@AllArgsConstructor
public class ChangeNoteFileHandler {
  
  private final Yaml yaml = new Yaml();
  private final Logger logger = LoggerFactory.getLogger(ChangeNoteFileHandler.class);
  
  private final ScopeService scopeService;
  private final ProductService productService;
  private final FeatureService featureService;
  private final CustomerService customerService;
  
  private static final String REFERENCE_FIELD = "reference";
  private static final String SCOPE_FIELD = "scope";
  private static final String PRODUCT_FIELD = "product";
  private static final String FEATURE_FIELD = "feature";
  private static final String CUSTOMER_FIELD = "customer";
  private static final String CHANGE_FIELD = "change";
  private static final String TECHNICAL_CHANGE_FIELD = "technical-change";
  private static final String UPGRADE_REQUIREMENTS_FIELD = "upgrade-requirements";
  
  /**
   * Parses a change note YAML file and creates a ChangeNote entity from the data in the file.
   * 
   * The YAML file must be formatted correctly, using the follwing fields:
   * <ul>
   * <li>reference (optional): a string reference for the change note, e.g. a JIRA ticket number</li>
   * <li>scope (required): the name of the scope for the change note, must correspond to an existing scope
   * <li>product (optional): the name of the product for the change note, must correspond to an existing product if provided</li>
   * <li>feature (optional): the name of the feature for the change note, must correspond to an existing feature if provided</li>
   * <li>customer (optional): the name of the customer for the change note, must correspond to an existing customer if provided</li>
   * <li>change (optional): a description of the change</li>
   * <li>technical-change (optional): technical notes about the change, to be viewed by developers</li>
   * <li>upgrade-requirements (optional): notes about upgrade requirements for the change</li>
   * </ul>
   * @throws InvalidChangeNoteYamlException
   */
  public ChangeNote getChangeNoteFromFile(File changeNoteFile) throws InvalidChangeNoteYamlException {
    logger.info("Parsing change note file at {}", changeNoteFile.getPath());
    ChangeNote changeNote = new ChangeNote();
    try (InputStream inputStream = new FileInputStream(changeNoteFile);) {
      Map<String, Object> changeNoteData = yaml.load(inputStream);
      
      if (changeNoteData == null) {
        throw new InvalidChangeNoteYamlException("YAML file is empty or has invalid format");
      }

      changeNote.setReference((String) changeNoteData.getOrDefault(REFERENCE_FIELD, null)); // optional
      String scope = (String) changeNoteData.get(SCOPE_FIELD);
      if (scope == null) {
        changeNote.setScope(null);
      } else {
        List<Scope> scopes = scopeService.getScopeByName(scope);
        if (scopes.isEmpty()) {
          changeNote.setScope(null);
        } else {
          if (scopes.size() > 1) {
            logger.warn("Multiple scopes found with name '{}', using the first one with id {}", scope, scopes.get(0).getId());
          }
          changeNote.setScope(scopes.get(0));
        }
      }
      
      String product = (String) changeNoteData.getOrDefault(PRODUCT_FIELD, null); // optional
      if (product == null) {
        changeNote.setProduct(null);
      } else {
        List<Product> products = productService.getProductByName(product);
        if (products.isEmpty()) {
          changeNote.setProduct(null);
        } else {
          if (products.size() > 1) {
            logger.warn("Multiple products found with name '{}', using the first one with id {}", product, products.get(0).getId());
          }
          changeNote.setProduct(products.get(0));
        }
      }
      
      String feature = (String) changeNoteData.getOrDefault(FEATURE_FIELD, null); // optional
      if (feature == null) {
        changeNote.setFeature(null);
      } else {
        List<Feature> features = featureService.getFeatureByName(feature);
        if (features.isEmpty()) {
          changeNote.setFeature(null);
        } else {
          if (features.size() > 1) {
            logger.warn("Multiple features found with name '{}', using the first one with id {}", feature, features.get(0).getId());
          }
          changeNote.setFeature(features.get(0));
        }
      }
      
      String customer = (String) changeNoteData.getOrDefault(CUSTOMER_FIELD, null); // optional
      if (customer == null) {
        changeNote.setCustomer(null);
      } else {
        List<Customer> customers = customerService.getCustomerByName(customer);
        if (customers.isEmpty()) {
          changeNote.setCustomer(null);
        } else {
          if (customers.size() > 1) {
            logger.warn("Multiple customers found with name '{}', using the first one with id {}", customer, customers.get(0).getId());
          }
          changeNote.setCustomer(customers.get(0));
        }
      }

      changeNote.setDescription((String) changeNoteData.getOrDefault(CHANGE_FIELD, null)); // optional
      changeNote.setDeveloperNotes((String) changeNoteData.getOrDefault(TECHNICAL_CHANGE_FIELD, null)); // optional
      changeNote.setUpgradeNotes((String) changeNoteData.getOrDefault(UPGRADE_REQUIREMENTS_FIELD, null)); // optional
      
    } catch (FileNotFoundException e) {
      logger.error("Could not locate file {}", changeNoteFile.getName(), e);
    } catch (IOException e) {
      logger.error("Error reading file {}", changeNoteFile.getName(), e);
    } catch (ClassCastException e) {
      throw new InvalidChangeNoteYamlException("Invalid data type for one or more fields in the YAML file: " + e.getMessage());
    }

    return changeNote;
  }
}
