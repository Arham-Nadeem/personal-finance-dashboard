package com.personalfinance.dashboard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {
    private static final String DB_URL = "jdbc:sqlite:finance.db";

    public static void initializeDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS income (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "amount REAL NOT NULL CHECK(amount > 0)," +
                    "source TEXT NOT NULL," +
                    "date TEXT DEFAULT CURRENT_DATE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS expense (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "amount REAL NOT NULL CHECK(amount > 0)," +
                    "description TEXT," +
                    "category TEXT NOT NULL," +
                    "date TEXT DEFAULT CURRENT_DATE)");

            // Migration for older versions that did not store expense descriptions.
            if (!columnExists(conn, "expense", "description")) {
                stmt.execute("ALTER TABLE expense ADD COLUMN description TEXT");
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                    "key TEXT PRIMARY KEY," +
                    "value TEXT NOT NULL)");
        } catch (SQLException e) {
            throw new RuntimeException("Unable to initialize database: " + e.getMessage(), e);
        }
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) return true;
            }
        }
        return false;
    }

    public static void insertIncome(double amount, String source) {
        String sql = "INSERT INTO income (amount, source, date) VALUES (?, ?, CURRENT_DATE)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, source);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to save income: " + e.getMessage(), e);
        }
    }

    public static void insertExpense(double amount, String description, String category) {
        String sql = "INSERT INTO expense (amount, description, category, date) VALUES (?, ?, ?, CURRENT_DATE)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, description);
            ps.setString(3, category);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to save expense: " + e.getMessage(), e);
        }
    }

    public static List<TransactionRecord> getTransactions() {
        String sql = "SELECT id, 'Income' AS type, source AS description, '' AS category, amount, date FROM income " +
                "UNION ALL " +
                "SELECT id, 'Expense' AS type, COALESCE(description, '') AS description, category, amount, date FROM expense " +
                "ORDER BY date DESC, type ASC, id DESC";
        List<TransactionRecord> records = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                records.add(new TransactionRecord(
                        rs.getInt("id"), rs.getString("type"), rs.getString("description"),
                        rs.getString("category"), rs.getDouble("amount"), rs.getString("date")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to load transactions: " + e.getMessage(), e);
        }
        return records;
    }

    public static void deleteTransaction(TransactionRecord record) {
        String table = "Income".equals(record.getType()) ? "income" : "expense";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table + " WHERE id = ?")) {
            ps.setInt(1, record.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to delete transaction: " + e.getMessage(), e);
        }
    }

    public static double getTotalIncome() { return getScalar("SELECT COALESCE(SUM(amount), 0) FROM income"); }
    public static double getTotalExpenses() { return getScalar("SELECT COALESCE(SUM(amount), 0) FROM expense"); }

    private static double getScalar(String sql) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to calculate totals: " + e.getMessage(), e);
        }
    }

    public static List<PieSlice> getExpenseTotalsByCategory() {
        List<PieSlice> data = new ArrayList<>();
        String sql = "SELECT category, SUM(amount) AS total FROM expense GROUP BY category ORDER BY total DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) data.add(new PieSlice(rs.getString("category"), rs.getDouble("total")));
        } catch (SQLException e) {
            throw new RuntimeException("Unable to load category totals: " + e.getMessage(), e);
        }
        return data;
    }

    public static List<MonthlySummary> getMonthlySummaries() {
        String sql = "WITH months AS (" +
                "SELECT substr(date,1,7) month FROM income UNION SELECT substr(date,1,7) month FROM expense) " +
                "SELECT month, " +
                "COALESCE((SELECT SUM(amount) FROM income i WHERE substr(i.date,1,7)=months.month),0) income, " +
                "COALESCE((SELECT SUM(amount) FROM expense e WHERE substr(e.date,1,7)=months.month),0) expenses " +
                "FROM months ORDER BY month DESC LIMIT 6";
        List<MonthlySummary> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) data.add(0, new MonthlySummary(rs.getString("month"), rs.getDouble("income"), rs.getDouble("expenses")));
        } catch (SQLException e) {
            throw new RuntimeException("Unable to load monthly trends: " + e.getMessage(), e);
        }
        return data;
    }

    public static void setSavingsGoal(double goal) {
        String sql = "INSERT INTO settings(key, value) VALUES('savings_goal', ?) " +
                "ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Double.toString(goal));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to save goal: " + e.getMessage(), e);
        }
    }

    public static double getSavingsGoal() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("SELECT value FROM settings WHERE key='savings_goal'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Double.parseDouble(rs.getString(1)) : 0.0;
        } catch (SQLException | NumberFormatException e) {
            return 0.0;
        }
    }

    public static class PieSlice {
        private final String category;
        private final double total;
        public PieSlice(String category, double total) { this.category = category; this.total = total; }
        public String getCategory() { return category; }
        public double getTotal() { return total; }
    }
}
