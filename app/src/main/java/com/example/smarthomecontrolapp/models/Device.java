package com.example.smarthomecontrolapp.models;

import com.google.firebase.database.Exclude;

public class Device {
    private String deviceId;
    private String roomId;
    private String deviceName;
    private String type;
    private boolean status;
    private double powerConsumption; // in kW
    private int count;
    private long lastStatusChangeTimestamp;
    private long totalOnTimeMillis;
    private double budget;

    public Device() {
        // Default constructor for Firebase
    }

    public Device(String deviceId, String roomId, String deviceName,
                  String type, boolean status, double powerConsumption, int count) {
        this.deviceId = deviceId;
        this.roomId = roomId;
        this.deviceName = deviceName;
        this.type = type;
        this.status = status;
        this.powerConsumption = powerConsumption;
        this.count = count;
        this.lastStatusChangeTimestamp = System.currentTimeMillis();
        this.totalOnTimeMillis = 0;
        this.budget = 50.0;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isStatus() { return status; }
    
    public void setStatus(boolean status) {
        this.status = status;
    }

    @Exclude
    public void toggleStatus(boolean isChecked) {
        long now = System.currentTimeMillis();
        if (this.status != isChecked) {
            if (this.status) { // Turning OFF: Accumulate the time from the session that just ended
                this.totalOnTimeMillis += (now - this.lastStatusChangeTimestamp);
            }
            // Always update the timestamp when status changes
            this.lastStatusChangeTimestamp = now;
            this.status = isChecked;
        }
    }

    public double getPowerConsumption() { return powerConsumption; }
    public void setPowerConsumption(double powerConsumption) { this.powerConsumption = powerConsumption; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public long getLastStatusChangeTimestamp() { 
        return lastStatusChangeTimestamp; 
    }
    public void setLastStatusChangeTimestamp(long lastStatusChangeTimestamp) { 
        this.lastStatusChangeTimestamp = lastStatusChangeTimestamp; 
    }

    public long getTotalOnTimeMillis() { return totalOnTimeMillis; }
    public void setTotalOnTimeMillis(long totalOnTimeMillis) { this.totalOnTimeMillis = totalOnTimeMillis; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    @Exclude
    public double calculateExpenditure(double rate) {
        long effectiveOnTime = totalOnTimeMillis;
        if (status) {
            long now = System.currentTimeMillis();
            // Safety check: if timestamp is in the future or 0, treat it as starting now
            if (lastStatusChangeTimestamp > 0 && lastStatusChangeTimestamp < now) {
                effectiveOnTime += (now - lastStatusChangeTimestamp);
            }
        }
        double hours = effectiveOnTime / (1000.0 * 60.0 * 60.0);
        return hours * powerConsumption * rate * (count > 0 ? count : 1);
    }
}
