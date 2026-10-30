-- ===================================================================
-- SpendWise Personal Finance Tracker - MySQL Schema
-- Database: finance_tracker
-- ===================================================================

CREATE DATABASE IF NOT EXISTS finance_tracker;
USE finance_tracker;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    transaction_date DATE NOT NULL,
    payment_method VARCHAR(50) DEFAULT 'OTHER',
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_trans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_trans_user (user_id),
    INDEX idx_trans_date (transaction_date),
    INDEX idx_trans_type (type),
    INDEX idx_trans_category (category),
    INDEX idx_trans_demo (is_demo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Budgets Table (Unique constraint for user + category + month)
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    budget_month VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    limit_amount DECIMAL(12, 2) NOT NULL,
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_category_month UNIQUE (user_id, category, budget_month),
    INDEX idx_budget_user (user_id),
    INDEX idx_budget_month (budget_month),
    INDEX idx_budget_demo (is_demo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
