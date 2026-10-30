package com.spendwise.service;

import com.spendwise.dto.TransactionRequest;
import com.spendwise.dto.TransactionResponse;
import com.spendwise.entity.Transaction;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.exception.BadRequestException;
import com.spendwise.exception.ResourceNotFoundException;
import com.spendwise.repository.TransactionRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        validateTransactionRequest(request);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setCategory(request.getCategory().trim());
        transaction.setDescription(request.getDescription().trim());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().trim() : "OTHER");
        transaction.setDemo(false); // User-created records are real personal transactions

        Transaction saved = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(saved);
    }

    public TransactionResponse updateTransaction(Long id, TransactionRequest request, User user) {
        validateTransactionRequest(request);

        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Enforce user ownership isolation
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found with id: " + id);
        }

        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setCategory(request.getCategory().trim());
        transaction.setDescription(request.getDescription().trim());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().trim() : "OTHER");

        Transaction updated = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(updated);
    }

    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Enforce user ownership isolation
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found with id: " + id);
        }

        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Enforce user ownership isolation
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found with id: " + id);
        }

        return TransactionResponse.fromEntity(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(User user, TransactionType type, String category,
                                                    LocalDate startDate, LocalDate endDate,
                                                    String search, Pageable pageable) {
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // User isolation predicate
            predicates.add(cb.equal(root.get("user"), user));

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("description")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return transactionRepository.findAll(spec, pageable).map(TransactionResponse::fromEntity);
    }

    private void validateTransactionRequest(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transaction amount must be greater than zero");
        }
        if (request.getTransactionDate() == null) {
            throw new BadRequestException("Transaction date cannot be null");
        }
    }
}
