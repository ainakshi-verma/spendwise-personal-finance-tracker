package com.spendwise.service;

import com.spendwise.dto.CategorySpendingDto;
import com.spendwise.dto.MonthlyReportResponse;
import com.spendwise.dto.MonthlyTrendDto;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public MonthlyReportResponse getMonthlyReport(User user, String month) {
        String targetMonthStr = resolveMonth(month);
        YearMonth targetMonth = YearMonth.parse(targetMonthStr);
        LocalDate startCurrent = targetMonth.atDay(1);
        LocalDate endCurrent = targetMonth.atEndOfMonth();

        YearMonth prevMonth = targetMonth.minusMonths(1);
        LocalDate startPrev = prevMonth.atDay(1);
        LocalDate endPrev = prevMonth.atEndOfMonth();

        MonthlyReportResponse response = new MonthlyReportResponse();
        response.setMonth(targetMonthStr);
        response.setStartDate(startCurrent);
        response.setEndDate(endCurrent);

        // Current Month Metrics for User
        BigDecimal totalIncome = transactionRepository.sumAmountByUserAndTypeAndDateRange(
            user, TransactionType.INCOME, startCurrent, endCurrent);
        BigDecimal totalExpenses = transactionRepository.sumAmountByUserAndTypeAndDateRange(
            user, TransactionType.EXPENSE, startCurrent, endCurrent);

        response.setTotalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO);
        response.setTotalExpenses(totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        BigDecimal netSavings = response.getTotalIncome().subtract(response.getTotalExpenses());
        response.setNetSavings(netSavings);

        if (response.getTotalIncome().compareTo(BigDecimal.ZERO) > 0) {
            double rate = netSavings.divide(response.getTotalIncome(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
            response.setSavingsRate(rate);
        } else {
            response.setSavingsRate(0.0);
        }

        // Previous Month Metrics for User
        response.setPreviousMonth(prevMonth.toString());
        BigDecimal prevIncome = transactionRepository.sumAmountByUserAndTypeAndDateRange(
            user, TransactionType.INCOME, startPrev, endPrev);
        BigDecimal prevExpenses = transactionRepository.sumAmountByUserAndTypeAndDateRange(
            user, TransactionType.EXPENSE, startPrev, endPrev);

        BigDecimal safePrevIncome = prevIncome != null ? prevIncome : BigDecimal.ZERO;
        BigDecimal safePrevExpenses = prevExpenses != null ? prevExpenses : BigDecimal.ZERO;

        response.setPrevTotalIncome(safePrevIncome);
        response.setPrevTotalExpenses(safePrevExpenses);
        response.setPrevNetSavings(safePrevIncome.subtract(safePrevExpenses));

        // Has previous data if either income or expense was recorded
        boolean hasPrevData = safePrevIncome.compareTo(BigDecimal.ZERO) > 0 || safePrevExpenses.compareTo(BigDecimal.ZERO) > 0;
        response.setHasPreviousMonthData(hasPrevData);

        if (hasPrevData) {
            BigDecimal incomeChange = response.getTotalIncome().subtract(safePrevIncome);
            BigDecimal expenseChange = response.getTotalExpenses().subtract(safePrevExpenses);
            response.setIncomeChange(incomeChange);
            response.setExpenseChange(expenseChange);

            if (safePrevIncome.compareTo(BigDecimal.ZERO) > 0) {
                double incPct = incomeChange.divide(safePrevIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
                response.setIncomeChangePct(incPct);
            }

            if (safePrevExpenses.compareTo(BigDecimal.ZERO) > 0) {
                double expPct = expenseChange.divide(safePrevExpenses, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
                response.setExpenseChangePct(expPct);
            }
        }

        // Category Breakdown for User
        List<CategorySpendingDto> categories = transactionRepository.findCategorySpendingBetween(user, startCurrent, endCurrent);
        if (response.getTotalExpenses().compareTo(BigDecimal.ZERO) > 0) {
            for (CategorySpendingDto cat : categories) {
                double pct = cat.getAmount().divide(response.getTotalExpenses(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
                cat.setPercentage(pct);
            }
        }
        response.setCategoryBreakdown(categories);

        // Highest spending category
        if (!categories.isEmpty()) {
            CategorySpendingDto highest = categories.get(0);
            response.setHighestSpendingCategory(highest.getCategory());
            response.setHighestSpendingAmount(highest.getAmount());
        }

        return response;
    }

    public List<CategorySpendingDto> getCategoryBreakdown(User user, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        List<CategorySpendingDto> categories = transactionRepository.findCategorySpendingBetween(user, startDate, endDate);
        BigDecimal total = categories.stream()
            .map(CategorySpendingDto::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) > 0) {
            for (CategorySpendingDto cat : categories) {
                double pct = cat.getAmount().divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
                cat.setPercentage(pct);
            }
        }

        return categories;
    }

    public List<MonthlyTrendDto> getMonthlyTrends(User user, int months) {
        if (months < 1 || months > 24) {
            months = 6;
        }

        List<MonthlyTrendDto> trends = new ArrayList<>();
        YearMonth current = YearMonth.now();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = months - 1; i >= 0; i--) {
            YearMonth targetMonth = current.minusMonths(i);
            LocalDate start = targetMonth.atDay(1);
            LocalDate end = targetMonth.atEndOfMonth();

            BigDecimal income = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                user, TransactionType.INCOME, start, end);
            BigDecimal expense = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                user, TransactionType.EXPENSE, start, end);

            trends.add(new MonthlyTrendDto(
                targetMonth.toString(),
                targetMonth.format(labelFormatter),
                income != null ? income : BigDecimal.ZERO,
                expense != null ? expense : BigDecimal.ZERO
            ));
        }

        return trends;
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
