package com.personalfinance.dashboard;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DashboardController {
    @FXML private TextField incomeSourceField;
    @FXML private TextField incomeAmountField;
    @FXML private TextField expenseDescriptionField;
    @FXML private TextField expenseAmountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField savingsGoalField;

    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netSavingsLabel;
    @FXML private Label savingsProgressLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressBar savingsProgressBar;

    @FXML private PieChart expensePieChart;
    @FXML private BarChart<String, Number> monthlyBarChart;

    @FXML private TableView<TransactionRecord> transactionTable;
    @FXML private TableColumn<TransactionRecord, String> dateColumn;
    @FXML private TableColumn<TransactionRecord, String> typeColumn;
    @FXML private TableColumn<TransactionRecord, String> descriptionColumn;
    @FXML private TableColumn<TransactionRecord, String> categoryColumn;
    @FXML private TableColumn<TransactionRecord, Number> amountColumn;

    private final ObservableList<TransactionRecord> transactions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Housing", "Food", "Transportation", "Utilities", "Entertainment", "Healthcare", "Shopping", "Other"));
        categoryComboBox.setValue("Other");

        dateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDate()));
        typeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));
        descriptionColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        categoryColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));
        amountColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getAmount()));
        amountColumn.setCellFactory(col -> new TableCell<TransactionRecord, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%,.2f", item.doubleValue()));
            }
        });

        transactionTable.setItems(transactions);
        savingsGoalField.setText(DBHelper.getSavingsGoal() > 0 ? String.format("%.2f", DBHelper.getSavingsGoal()) : "");
        refreshDashboard();
    }

    @FXML
    public void handleAddIncome() {
        String source = incomeSourceField.getText().trim();
        if (source.isEmpty()) {
            showError("Please enter an income source.");
            return;
        }
        Double amount = parsePositiveAmount(incomeAmountField.getText(), "income amount");
        if (amount == null) return;

        try {
            DBHelper.insertIncome(amount, source);
            incomeSourceField.clear();
            incomeAmountField.clear();
            setStatus("Income added successfully.");
            refreshDashboard();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleAddExpense() {
        String description = expenseDescriptionField.getText().trim();
        if (description.isEmpty()) {
            showError("Please enter an expense description.");
            return;
        }
        Double amount = parsePositiveAmount(expenseAmountField.getText(), "expense amount");
        if (amount == null) return;

        try {
            DBHelper.insertExpense(amount, description, categoryComboBox.getValue());
            expenseDescriptionField.clear();
            expenseAmountField.clear();
            setStatus("Expense added successfully.");
            refreshDashboard();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleSetGoal() {
        Double goal = parsePositiveAmount(savingsGoalField.getText(), "savings goal");
        if (goal == null) return;
        try {
            DBHelper.setSavingsGoal(goal);
            setStatus("Savings goal updated.");
            refreshSummary();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    public void handleDeleteSelected() {
        TransactionRecord selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a transaction to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this " + selected.getType().toLowerCase() + " transaction?", ButtonType.YES, ButtonType.NO);
        confirmation.setHeaderText("Confirm deletion");
        if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                DBHelper.deleteTransaction(selected);
                setStatus("Transaction deleted.");
                refreshDashboard();
            } catch (RuntimeException e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    public void handleExportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export transactions");
        chooser.setInitialFileName("personal-finance-transactions.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File file = chooser.showSaveDialog(transactionTable.getScene().getWindow());
        if (file == null) return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Date,Type,Description,Category,Amount\n");
            for (TransactionRecord record : transactions) {
                writer.write(csv(record.getDate()) + "," + csv(record.getType()) + "," + csv(record.getDescription()) + "," +
                        csv(record.getCategory()) + "," + String.format("%.2f", record.getAmount()) + "\n");
            }
            setStatus("Transactions exported to CSV.");
        } catch (IOException e) {
            showError("Unable to export CSV: " + e.getMessage());
        }
    }

    private void refreshDashboard() {
        try {
            transactions.setAll(DBHelper.getTransactions());
            refreshSummary();
            refreshPieChart();
            refreshMonthlyChart();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void refreshSummary() {
        double income = DBHelper.getTotalIncome();
        double expenses = DBHelper.getTotalExpenses();
        double savings = income - expenses;
        double goal = DBHelper.getSavingsGoal();
        double progress = goal > 0 ? savings / goal : 0;

        totalIncomeLabel.setText(String.format("$%,.2f", income));
        totalExpensesLabel.setText(String.format("$%,.2f", expenses));
        netSavingsLabel.setText(String.format("$%,.2f", savings));
        savingsProgressBar.setProgress(goal > 0 ? Math.max(0, Math.min(progress, 1.0)) : 0);
        savingsProgressLabel.setText(goal > 0
                ? String.format("$%,.2f of $%,.2f (%.0f%%)", savings, goal, progress * 100)
                : "Set a savings goal to track progress");
    }

    private void refreshPieChart() {
        ObservableList<PieChart.Data> slices = FXCollections.observableArrayList();
        for (DBHelper.PieSlice slice : DBHelper.getExpenseTotalsByCategory()) {
            slices.add(new PieChart.Data(slice.getCategory(), slice.getTotal()));
        }
        expensePieChart.setData(slices);
    }

    private void refreshMonthlyChart() {
        monthlyBarChart.getData().clear();
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");

        for (MonthlySummary summary : DBHelper.getMonthlySummaries()) {
            incomeSeries.getData().add(new XYChart.Data<>(summary.getMonth(), summary.getIncome()));
            expenseSeries.getData().add(new XYChart.Data<>(summary.getMonth(), summary.getExpenses()));
        }
        monthlyBarChart.getData().addAll(incomeSeries, expenseSeries);
    }

    private Double parsePositiveAmount(String raw, String fieldName) {
        try {
            double value = Double.parseDouble(raw.trim());
            if (value <= 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException e) {
            showError("Please enter a positive number for the " + fieldName + ".");
            return null;
        }
    }

    private String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Something needs attention");
        alert.showAndWait();
    }
}
