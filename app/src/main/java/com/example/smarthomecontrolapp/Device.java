package com.example.smarthomecontrolapp;

public class Device {
    private String deviceId;
    private String roomId;
    private String deviceName;
    private String type;
    private boolean status;
    private double powerConsumption;

    public Device() {}

    public Device(String deviceId, String roomId, String deviceName,
                  String type, boolean status, double powerConsumption) {

        this.deviceId = deviceId;
        this.roomId = roomId;
        this.deviceName = deviceName;
        this.type = type;
        this.status = status;
        this.powerConsumption = powerConsumption;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public double getPowerConsumption() {
        return powerConsumption;
    }

    public void setPowerConsumption(double powerConsumption) {
        this.powerConsumption = powerConsumption;
    }
}
