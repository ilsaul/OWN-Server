package org.programmatori.domotica.own.sdk.component;

public enum Who {
    LIGHT(1, "light"),
    BLIND(2, "blind");

    private final String name;
    private final int value;

    // enum constructor - cannot be public or protected
    private Who(int value, String name)
    {
        this.value = value;
        this.name = name;
    }

    // getter method
    public int getValue()
    {
        return value;
    }

    public String getName() {
        return name;
    }
}
