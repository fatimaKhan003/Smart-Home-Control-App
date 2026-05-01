package com.example.smarthomecontrolapp;

public class Room {
    private String roomId;
    private String roomName;
    private double temperature;

    public Room() {}

    public Room(String roomId, String roomName, double temperature) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.temperature = temperature;
    }

    public String getRoomId() {
        return roomId;
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
