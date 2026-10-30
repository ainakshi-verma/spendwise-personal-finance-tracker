package com.spendwise.repository;

import com.spendwise.dto.CategorySpendingDto;
import com.spendwise.entity.Transaction;
import com.spendwise.entity.TransactionType;
import com.spendwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findTop5ByUserOrderByTransactionDateDescCreatedAtDesc(User user);

    List<Transaction> findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndTypeAndDateRange(@Param("user") User user,
                                                  @Param("type") TransactionType type,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.type = :type")
    BigDecimal sumAmountByUserAndType(@Param("user") User user,
                                      @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.type = :type AND LOWER(t.category) = LOWER(:category) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndTypeAndCategoryAndDateRange(@Param("user") User user,
                                                             @Param("type") TransactionType type,
                                                             @Param("category") String category,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT new com.spendwise.dto.CategorySpendingDto(t.category, SUM(t.amount), COUNT(t)) " +
           "FROM Transaction t " +
           "WHERE t.user = :user AND t.type = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category " +
           "ORDER BY SUM(t.amount) DESC")
    List<CategorySpendingDto> findCategorySpendingBetween(@Param("user") User user,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    long countByUserAndIsDemoTrue(User user);

    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.user = :user AND t.isDemo = true")
    void deleteByUserAndIsDemoTrue(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.user = :user")
    void deleteByUser(@Param("user") User user);
}
