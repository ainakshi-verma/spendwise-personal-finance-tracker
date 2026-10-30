package com.spendwise.service;

import com.spendwise.dto.BudgetRequest;
import com.spendwise.dto.BudgetResponse;
import com.spendwise.entity.Budget;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.exception.BadRequestException;
import com.spendwise.exception.DuplicateResourceException;
import com.spendwise.exception.ResourceNotFoundException;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository, TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
    }

    public BudgetResponse createBudget(BudgetRequest request, User user) {
        validateBudgetRequest(request);

        String category = request.getCategory().trim();
        String month = request.getMonth().trim();

        if (budgetRepository.existsByUserAndCategoryIgnoreCaseAndMonth(user, category, month)) {
            throw new DuplicateResourceException(
                String.format("A budget for category '%s' already exists for %s", category, month));
        }

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setMonth(month);
        budget.setLimitAmount(request.getLimitAmount());
        budget.setDemo(false);

        Budget saved = budgetRepository.save(budget);
        return buildBudgetResponse(saved, user);
    }

    public BudgetResponse updateBudget(Long id, BudgetRequest request, User user) {
        validateBudgetRequest(request);

        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        // Enforce user ownership isolation
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Budget not found with id: " + id);
        }

        String category = request.getCategory().trim();
        String month = request.getMonth().trim();

        if (budgetRepository.existsByUserAndCategoryIgnoreCaseAndMonthAndIdNot(user, category, month, id)) {
            throw new DuplicateResourceException(
                String.format("Another budget for category '%s' already exists for %s", category, month));
        }

        budget.setCategory(category);
        budget.setMonth(month);
        budget.setLimitAmount(request.getLimitAmount());

        Budget updated = budgetRepository.save(budget);
        return buildBudgetResponse(updated, user);
    }

    public void deleteBudget(Long id, User user) {
        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        // Enforce user ownership isolation
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Budget not found with id: " + id);
        }

        budgetRepository.delete(budget);
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id, User user) {
        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Budget not found with id: " + id);
        }

        return buildBudgetResponse(budget, user);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsForMonth(User user, String month) {
        validateMonth(month);
        List<Budget> budgets = budgetRepository.findByUserAndMonthOrderByCategoryAsc(user, month);
        return budgets.stream()
            .map(b -> buildBudgetResponse(b, user))
            .collect(Collectors.toList());
    }

    private BudgetResponse buildBudgetResponse(Budget budget, User user) {
        YearMonth ym = YearMonth.parse(budget.getMonth());
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        BigDecimal actualSpent = transactionRepository.sumAmountByUserAndTypeAndCategoryAndDateRange(
            user,
            TransactionType.EXPENSE,
            budget.getCategory(),
            startDate,
            endDate
        );

        return new BudgetResponse(
            budget.getId(),
            budget.getCategory(),
            budget.getMonth(),
            budget.getLimitAmount(),
            actualSpent != null ? actualSpent : BigDecimal.ZERO,
            budget.isDemo()
        );
    }

    private void validateBudgetRequest(BudgetRequest request) {
        if (request.getLimitAmount() == null || request.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Budget limit must be greater than zero");
        }
        validateMonth(request.getMonth());
    }

    private void validateMonth(String month) {
        if (month == null || !month.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            throw new BadRequestException("Month must be in YYYY-MM format (e.g., 2026-10)");
        }
    }
}
