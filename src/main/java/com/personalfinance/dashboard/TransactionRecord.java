package com.personalfinance.dashboard;

public class TransactionRecord {
    private final int id;
    private final String type;
    private final String description;
    private final String category;
    private final double amount;
    private final String date;

    public TransactionRecord(int id, String type, String description, String category, double amount, String date) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
}
