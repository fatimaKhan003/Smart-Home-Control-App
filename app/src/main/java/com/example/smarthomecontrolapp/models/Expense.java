package com.example.smarthomecontrolapp.models;

public class Expense {
    private String month;
    private double amount;
    private double consumption;

    public Expense() {}

    public Expense(String month, double amount, double consumption) {
        this.month = month;
        this.amount = amount;
        this.consumption = consumption;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }
}
