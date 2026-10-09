package com.personalfinance.dashboard;

public class MonthlySummary {
    private final String month;
    private final double income;
    private final double expenses;

    public MonthlySummary(String month, double income, double expenses) {
        this.month = month;
        this.income = income;
        this.expenses = expenses;
    }

    public String getMonth() { return month; }
    public double getIncome() { return income; }
    public double getExpenses() { return expenses; }
}
