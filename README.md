# Personal Finance Dashboard

A desktop personal finance application built with **Java, JavaFX, SQLite, and Maven**. The dashboard helps users record income and expenses, understand spending patterns, monitor net savings, set a savings goal, and export their transaction history.

## Features

- Persistent SQLite storage for income, expenses, and user settings
- Income and expense entry with input validation
- Expense categories and interactive spending breakdown
- Summary cards for total income, total expenses, and net savings
- Savings-goal tracking with a progress indicator
- Monthly income-vs-expense cash-flow chart
- Searchable-style transaction history table with formatted currency
- Delete transactions with confirmation
- Export transaction history to CSV
- Automatic database migration for older versions of the project
- Clean JavaFX interface styled with CSS

## Tech Stack

- **Java 11**
- **JavaFX 11**
- **SQLite** via JDBC
- **FXML / CSS**
- **Maven**

## Project Structure

```text
src/main/java/com/personalfinance/dashboard/
├── Main.java
├── DashboardController.java
├── DBHelper.java
├── Income.java
├── Expense.java
├── TransactionRecord.java
└── MonthlySummary.java

src/main/resources/
├── com/personalfinance/dashboard/dashboard-view.fxml
└── style.css
```

## Run Locally

### Requirements

- JDK 11+
- Internet access the first time Maven downloads dependencies

### Windows

```bash
mvnw.cmd clean javafx:run
```

### macOS / Linux

```bash
./mvnw clean javafx:run
```

The application creates `finance.db` locally on first run. The database is intentionally excluded from version control so personal financial data is never committed to GitHub.

## Portfolio Highlights

This project demonstrates:

- Object-oriented Java application design
- Relational data persistence with parameterized SQL queries
- Database schema migration and aggregation queries
- Event-driven JavaFX development with FXML controllers
- Financial KPI calculation and visualization
- CSV export and user-input validation

## Future Improvements

- Date-range and category filtering
- Budget limits by category
- Recurring transactions
- Automated tests for database and controller logic
- Packaging as a native desktop installer
