package com.personalfinance.dashboard;

public class Income {
    private final int id;
    private final String source;
    private final double amount;
    private final String date;

    public Income(int id, String source, double amount, String date) {
        this.id = id;
        this.source = source;
        this.amount = amount;
        this.date = date;
    }

    public int getId() { return id; }
    public String getSource() { return source; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
}
