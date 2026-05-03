package com.example.smarthomecontrolapp.models;

public class Room {
    private String roomId;
    private String roomName;
    private double temperature;
    private int deviceCount;

    public Room() {}

    public Room(String roomId, String roomName, double temperature, int count) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.temperature = temperature;
        this.deviceCount=count;
    }

    public String getRoomId() {
        return roomId;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
