package com.example.smarthomecontrolapp;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String name;
    private double electricityRate;
    private double savingsTarget;
    private Map<String, Double> deviceSavingsTargets;

    public User() {
        this.deviceSavingsTargets = new HashMap<>();
    }

    public User(String name, double electricityRate, double savingsTarget) {
        this.name = name;
        this.electricityRate = electricityRate;
        this.savingsTarget = savingsTarget;
        this.deviceSavingsTargets = new HashMap<>();
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

    public Map<String, Double> getDeviceSavingsTargets() {
        if (deviceSavingsTargets == null) deviceSavingsTargets = new HashMap<>();
        return deviceSavingsTargets;
    }

    public void setDeviceSavingsTargets(Map<String, Double> deviceSavingsTargets) {
        this.deviceSavingsTargets = deviceSavingsTargets;
    }

    public double getDeviceSavingsTarget(String deviceType) {
        if (deviceSavingsTargets == null || !deviceSavingsTargets.containsKey(deviceType)) {
            return 50.0; // Default fallback
        }
        return deviceSavingsTargets.get(deviceType);
    }
}
