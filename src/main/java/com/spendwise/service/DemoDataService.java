package com.spendwise.service;

import com.spendwise.entity.Budget;
import com.spendwise.entity.Transaction;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DemoDataService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public DemoDataService(TransactionRepository transactionRepository, BudgetRepository budgetRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
    }

    public int seedDemoData(User user) {
        // Idempotency: If demo data is already loaded for this user, do NOT insert duplicate records
        if (transactionRepository.countByUserAndIsDemoTrue(user) > 0) {
            return 0; // Already loaded
        }

        YearMonth currentYm = YearMonth.now();
        YearMonth prevYm = currentYm.minusMonths(1);
        YearMonth prev2Ym = currentYm.minusMonths(2);

        List<Transaction> transactions = new ArrayList<>();

        // Month - 2 Transactions (2 months ago)
        transactions.add(createDemoTx(user, TransactionType.INCOME, "15000.00", "Allowance", "Monthly college stipend from parents", prev2Ym.atDay(1), "BANK_TRANSFER"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "3200.00", "Food", "College canteen & mess dues", prev2Ym.atDay(4), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "1450.00", "Education", "Semester textbooks & engineering calculators", prev2Ym.atDay(7), "CARD"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "800.00", "Transport", "Monthly bus pass & metro recharge", prev2Ym.atDay(10), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "600.00", "Entertainment", "Movie outing with friends", prev2Ym.atDay(15), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "500.00", "Bills", "Mobile plan & hostel WiFi", prev2Ym.atDay(20), "UPI"));

        // Month - 1 Transactions (last month)
        transactions.add(createDemoTx(user, TransactionType.INCOME, "15000.00", "Allowance", "Monthly college allowance", prevYm.atDay(1), "BANK_TRANSFER"));
        transactions.add(createDemoTx(user, TransactionType.INCOME, "4500.00", "Freelance", "React/HTML website bugfix freelance gig", prevYm.atDay(12), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "2950.00", "Food", "Campus cafe, snacks & groceries", prevYm.atDay(3), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "750.00", "Education", "Lab journals and stationery supplies", prevYm.atDay(8), "CASH"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "650.00", "Transport", "Metro smart card auto-topup", prevYm.atDay(14), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "1200.00", "Shopping", "Technical conference t-shirt & backpack", prevYm.atDay(18), "CARD"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "550.00", "Bills", "Broadband internet recharge", prevYm.atDay(21), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "400.00", "Health", "Vitamins & medical prescription", prevYm.atDay(25), "CASH"));

        // Current Month Transactions
        transactions.add(createDemoTx(user, TransactionType.INCOME, "15000.00", "Allowance", "Monthly parental allowance", currentYm.atDay(1), "BANK_TRANSFER"));
        transactions.add(createDemoTx(user, TransactionType.INCOME, "6000.00", "Freelance", "Spring Boot REST API mini-project freelance work", currentYm.atDay(5), "BANK_TRANSFER"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "2100.00", "Food", "Weekly grocery & mess meals", currentYm.atDay(2), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "950.00", "Education", "Computer Networks reference book", currentYm.atDay(6), "CARD"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "520.00", "Transport", "City bus commute tickets", currentYm.atDay(9), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "450.00", "Entertainment", "Weekend coffee & team meetup", currentYm.atDay(12), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "600.00", "Bills", "Cellular data recharge", currentYm.atDay(16), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "380.00", "Health", "Eye checkup & eye drops", currentYm.atDay(20), "UPI"));
        transactions.add(createDemoTx(user, TransactionType.EXPENSE, "850.00", "Food", "Dinner with project team members", currentYm.atDay(22), "UPI"));

        transactionRepository.saveAll(transactions);

        // Seed Category Budgets for Current Month with isDemo = true
        String currMonthStr = currentYm.toString();
        seedBudgetIfNotExists(user, "Food", currMonthStr, new BigDecimal("3500.00"));
        seedBudgetIfNotExists(user, "Education", currMonthStr, new BigDecimal("1500.00"));
        seedBudgetIfNotExists(user, "Transport", currMonthStr, new BigDecimal("1000.00"));
        seedBudgetIfNotExists(user, "Entertainment", currMonthStr, new BigDecimal("800.00"));
        seedBudgetIfNotExists(user, "Bills", currMonthStr, new BigDecimal("1000.00"));

        return transactions.size();
    }

    /**
     * Clears ONLY demo data (records with is_demo = true), preserving user-created records
     */
    public void clearDemoData(User user) {
        transactionRepository.deleteByUserAndIsDemoTrue(user);
        budgetRepository.deleteByUserAndIsDemoTrue(user);
    }

    /**
     * Completely resets all data for the user
     */
    public void clearAllData(User user) {
        transactionRepository.deleteByUser(user);
        budgetRepository.deleteByUser(user);
    }

    @Transactional(readOnly = true)
    public boolean isDemoDataLoaded(User user) {
        return transactionRepository.countByUserAndIsDemoTrue(user) > 0;
    }

    private void seedBudgetIfNotExists(User user, String category, String month, BigDecimal limit) {
        if (!budgetRepository.existsByUserAndCategoryIgnoreCaseAndMonth(user, category, month)) {
            Budget budget = new Budget();
            budget.setUser(user);
            budget.setCategory(category);
            budget.setMonth(month);
            budget.setLimitAmount(limit);
            budget.setDemo(true);
            budgetRepository.save(budget);
        }
    }

    private Transaction createDemoTx(User user, TransactionType type, String amountStr, String category,
                                     String description, LocalDate date, String paymentMethod) {
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setType(type);
        tx.setAmount(new BigDecimal(amountStr));
        tx.setCategory(category);
        tx.setDescription(description);
        tx.setTransactionDate(date);
        tx.setPaymentMethod(paymentMethod);
        tx.setDemo(true);
        return tx;
    }
}
