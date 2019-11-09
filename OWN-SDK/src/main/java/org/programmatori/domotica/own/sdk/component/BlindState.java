package org.programmatori.domotica.own.sdk.component;

public enum BlindState {
    STOP(0, "stop"),
    DOWN(1, "down"),
    UP(2,"up");

    private final String name;
    private final int value;

    // enum constructor - cannot be public or protected
    private BlindState(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static BlindState createByValue(int value) {
        switch (value) {
            case 0: return STOP;
            case 1: return DOWN;
            case 2: return UP;
        }

        throw new IndexOutOfBoundsException("Must be 0, 1 or 2");
    }

    // getter method
    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
