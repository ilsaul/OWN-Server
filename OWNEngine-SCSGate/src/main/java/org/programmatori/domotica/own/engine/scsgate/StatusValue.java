package org.programmatori.domotica.own.engine.scsgate;

import org.joou.UByte;

/**
 * Decoding Byte to What
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com
 * @since 26/05/2019
 */
public enum StatusValue {
	ON(UByte.valueOf(0),1,1),
	OFF(UByte.valueOf(1),0,1),

	UP(UByte.valueOf(8),1,2),
	DOWN(UByte.valueOf(9),2,2),
	//STOP(UByte.valueOf(10),0,2),

	STOP_ADVANCED(UByte.valueOf(10), 0, 2),
	UP_ADVANCED(UByte.valueOf(11),3, 2), //FixMe: 3 IS NOT CONFIRM
	DOWN_ADVANCED(UByte.valueOf(12),4, 2); //FixMe: 4 IS NOT CONFIRM

	private UByte byteValue;
	private int who;
	private int what;

	StatusValue(UByte byteValue, int what, int who) {
		this.byteValue = byteValue;
		this.who = who;
		this.what = what;
	}

	public static StatusValue findByByte(UByte byteValue) {
		for (StatusValue val: StatusValue.values()) {
			if (val.getByteValue() == byteValue) {
				return val;
			}
		}

		return null;
	}

	public static StatusValue findBySCS(int who, int what) {
		for (StatusValue val: StatusValue.values()) {
			if (val.getWho() == who && val.getWhat() == what) {
				return val;
			}
		}

		return null;
	}

	public UByte getByteValue() {
		return byteValue;
	}

	public int getWho() {
		return who;
	}

	public String  getWhoString() {
		return Integer.toString(who);
	}

	public int getWhat() {
		return what;
	}

	public String getWhatString() {
		return Integer.toString(what);
	}
}
