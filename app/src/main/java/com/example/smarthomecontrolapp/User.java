package com.example.smarthomecontrolapp;

public class User {
    private String name;
    private double electricityRate;
    private double savingsTarget;

    public User() {}

    public User(String name, double electricityRate, double savingsTarget) {
        this.name = name;
        this.electricityRate = electricityRate;
        this.savingsTarget = savingsTarget;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getElectricityRate() {
        return electricityRate;
    }

    public void setElectricityRate(double electricityRate) {
        this.electricityRate = electricityRate;
    }

    public double getSavingsTarget() {
        return savingsTarget;
    }

    public void setSavingsTarget(double savingsTarget) {
        this.savingsTarget = savingsTarget;
    }
}
