package org.programmatori.domotica.own.sdk.component;

public enum LightState {
    OFF(0, "on"),
    ON(1,"off");

    private final int value;
    private final String name;

    // enum constructor - cannot be public or protected
    private LightState(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static LightState createByValue(int value) {
        switch (value) {
            case 0: return OFF;
            case 1: return ON;
        }

        throw new IndexOutOfBoundsException("Must be 0 or 1");
    }

    // getter method
    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
