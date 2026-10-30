package com.spendwise.repository;

import com.spendwise.entity.Budget;
import com.spendwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserAndMonthOrderByCategoryAsc(User user, String month);

    Optional<Budget> findByUserAndCategoryIgnoreCaseAndMonth(User user, String category, String month);

    boolean existsByUserAndCategoryIgnoreCaseAndMonth(User user, String category, String month);

    boolean existsByUserAndCategoryIgnoreCaseAndMonthAndIdNot(User user, String category, String month, Long id);

    @Modifying
    @Query("DELETE FROM Budget b WHERE b.user = :user AND b.isDemo = true")
    void deleteByUserAndIsDemoTrue(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM Budget b WHERE b.user = :user")
    void deleteByUser(@Param("user") User user);
}
