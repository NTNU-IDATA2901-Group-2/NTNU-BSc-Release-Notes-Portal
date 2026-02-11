package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.ChangeNote;
import com.example.demo.domain.repository.ChangeNoteRepository;
import com.example.demo.domain.repository.CustomerRepository;
import com.example.demo.domain.repository.FeatureRepository;
import com.example.demo.domain.repository.ProductRepository;
import com.example.demo.domain.repository.ScopeRepository;
import com.example.demo.dto.CreateChangeNoteDTO;
import com.example.demo.exception.CustomerNotFoundException;
import com.example.demo.exception.FeatureNotFoundException;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.exception.ScopeNotFoundException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChangeNoteService {
  private final ChangeNoteRepository changeNoteRepository;
  private final ProductRepository productRepository;
  private final ScopeRepository scopeRepository;
  private final FeatureRepository featureRepository;
  private final CustomerRepository customerRepository;

  public long createChangeNote(CreateChangeNoteDTO changeNoteDTO) {
    ChangeNote changeNote = new ChangeNote();

    changeNote.setReference(changeNoteDTO.reference());
    changeNote.setDescription(changeNoteDTO.description());
    changeNote.setDeveloperNotes(changeNoteDTO.developerNotes());
    changeNote.setUpgradeNotes(changeNoteDTO.upgradeNotes());
    changeNote.setChangeSource(changeNoteDTO.changeSource());

    if (changeNoteDTO.productId() != null) {
      changeNote.setProduct(productRepository.findById(changeNoteDTO.productId()).orElseThrow(() -> new ProductNotFoundException(changeNoteDTO.productId())));
    }

    if (changeNoteDTO.scopeId() != null) {
      changeNote.setScope(scopeRepository.findById(changeNoteDTO.scopeId()).orElseThrow(() -> new ScopeNotFoundException(changeNoteDTO.scopeId())));
    }

    if (changeNoteDTO.featureId() != null) {
      changeNote.setFeature(featureRepository.findById(changeNoteDTO.featureId()).orElseThrow(() -> new FeatureNotFoundException(changeNoteDTO.featureId())));
    }

    if (changeNoteDTO.customerId() != null) {
      changeNote.setCustomer(customerRepository.findById(changeNoteDTO.customerId()).orElseThrow(() -> new CustomerNotFoundException(changeNoteDTO.customerId())));
    }

    return changeNoteRepository.save(changeNote).getId();
  }
}
