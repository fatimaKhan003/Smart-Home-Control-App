package com.example.smarthomecontrolapp;

public enum DeviceType {
    SMART_TV("Smart TV"),
    SMART_FRIDGE("Smart Fridge"),
    LIGHTING("Lighting"),
    AIR_CONDITION("Air Condition"),
    BLINDS("Blinds"),
    MUSIC_SYSTEM("Music System");


    private final String displayName;

    DeviceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String[] getAllDisplayNames() {
        DeviceType[] types = values();
        String[] displayNames = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            displayNames[i] = types[i].displayName;
        }
        return displayNames;
    }

    public static DeviceType fromString(String text) {
        for (DeviceType b : DeviceType.values()) {
            if (b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return LIGHTING; // Default
    }
}
