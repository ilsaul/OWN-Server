package org.programmatori.domotica.own.engine.row;

/**
 * Decoding Byte to What
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com
 * @since 26/05/2019
 */
public enum StatusValue {
	ON((byte)0, "1", "1"),
	OFF((byte)1, "0", "1"),

	UP((byte)8, "1", "2"),
	DOWN((byte)9, "2", "2"),
	STOP((byte)10,"0", "2");

	//STOP_ADVANCED((byte)10,"0", "2");
	//UP_ADVANCED((byte)10,"0", "2");
	//DOWN_ADVANCED((byte)10,"0", "2");

	private byte value;
	private String who;
	private String what;

	StatusValue(byte value, String what, String who) {
		this.value = value;
		this.who = who;
		this.what = what;
	}

	public static StatusValue getStatusByValue(byte byteValue) {
		for (StatusValue val: StatusValue.values()) {
			if (val.getValue() == byteValue) {
				return val;
			}
		}

		return null;
	}

	public byte getValue() {
		return value;
	}

	public String getWho() {
		return who;
	}

	public String getWhat() {
		return what;
	}
}
