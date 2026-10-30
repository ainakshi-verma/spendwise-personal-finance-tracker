package com.spendwise.service;

import com.spendwise.dto.BudgetResponse;
import com.spendwise.dto.CategorySpendingDto;
import com.spendwise.dto.DashboardSummaryResponse;
import com.spendwise.dto.TransactionResponse;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.exception.BadRequestException;
import com.spendwise.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService;

    public DashboardService(TransactionRepository transactionRepository, BudgetService budgetService) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
    }

    public DashboardSummaryResponse getDashboardSummary(User user, String month) {
        String targetMonth = resolveMonth(month);
        YearMonth ym = YearMonth.parse(targetMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setMonth(targetMonth);

        // Monthly Income and Expenses for this User
        BigDecimal totalIncome = transactionRepository.sumAmountByUserAndTypeAndDateRange(
            user, TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpenses = transactionRepository.sumAmountByUserAndTypeAndDateRange(
            user, TransactionType.EXPENSE, startDate, endDate);

        response.setTotalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO);
        response.setTotalExpenses(totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        response.setNetSavings(response.getTotalIncome().subtract(response.getTotalExpenses()));

        // All-Time Balance Calculation for this User
        BigDecimal allTimeIncome = transactionRepository.sumAmountByUserAndType(user, TransactionType.INCOME);
        BigDecimal allTimeExpenses = transactionRepository.sumAmountByUserAndType(user, TransactionType.EXPENSE);
        BigDecimal safeAllTimeIncome = allTimeIncome != null ? allTimeIncome : BigDecimal.ZERO;
        BigDecimal safeAllTimeExpenses = allTimeExpenses != null ? allTimeExpenses : BigDecimal.ZERO;
        response.setAllTimeBalance(safeAllTimeIncome.subtract(safeAllTimeExpenses));

        // Category Breakdown for the Month for this User
        List<CategorySpendingDto> categories = transactionRepository.findCategorySpendingBetween(user, startDate, endDate);
        if (response.getTotalExpenses().compareTo(BigDecimal.ZERO) > 0) {
            for (CategorySpendingDto cat : categories) {
                double pct = cat.getAmount()
                    .divide(response.getTotalExpenses(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
                cat.setPercentage(pct);
            }
        }
        response.setCategoryBreakdown(categories);

        // Recent Transactions (top 5 for this User)
        List<TransactionResponse> recent = transactionRepository.findTop5ByUserOrderByTransactionDateDescCreatedAtDesc(user)
            .stream()
            .map(TransactionResponse::fromEntity)
            .collect(Collectors.toList());
        response.setRecentTransactions(recent);

        // Count of transactions for the user in this month
        long count = transactionRepository.findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(user, startDate, endDate).size();
        response.setTransactionCount(count);

        // Budget Summaries for Month for this User
        List<BudgetResponse> budgets = budgetService.getBudgetsForMonth(user, targetMonth);
        response.setBudgetSummaries(budgets);

        BigDecimal totalBudgetLimit = BigDecimal.ZERO;
        BigDecimal totalBudgetSpent = BigDecimal.ZERO;

        for (BudgetResponse b : budgets) {
            totalBudgetLimit = totalBudgetLimit.add(b.getLimitAmount());
            totalBudgetSpent = totalBudgetSpent.add(b.getActualSpent());
        }

        response.setTotalBudgetLimit(totalBudgetLimit);
        response.setTotalBudgetSpent(totalBudgetSpent);
        response.setRemainingBudget(totalBudgetLimit.subtract(totalBudgetSpent));

        return response;
    }

    public List<TransactionResponse> getRecentTransactions(User user) {
        return transactionRepository.findTop5ByUserOrderByTransactionDateDescCreatedAtDesc(user)
            .stream()
            .map(TransactionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    private String resolveMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            return YearMonth.now().toString();
        }
        if (!month.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            throw new BadRequestException("Month must be in YYYY-MM format (e.g. 2026-10)");
        }
        return month.trim();
    }
}
