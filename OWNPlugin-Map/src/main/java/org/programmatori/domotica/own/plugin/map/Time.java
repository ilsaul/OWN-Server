package org.programmatori.domotica.own.plugin.map;

public enum Time {
	HOUR(60, "hour"),
	MINUTE(60, "min"),
	SECOND(1000, "sec");


	private final int underUnit;
	private final String shortName;

	Time(int underUnit, String shortName) {
		this.underUnit = underUnit;
		this.shortName = shortName;
	}

	public static Time createFromShortName(String shortName) {
		for (Time time: values()) {
			if (time.getShortName().equals(shortName)) {
				return time;
			}
		}

		throw new IllegalStateException("Unexpected value: " + shortName);
	}

	public int getUnderUnit() {
		return underUnit;
	}

	public String getShortName() {
		return shortName;
	}
}
