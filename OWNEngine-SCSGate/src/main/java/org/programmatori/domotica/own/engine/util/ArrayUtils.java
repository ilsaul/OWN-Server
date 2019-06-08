package org.programmatori.domotica.own.engine.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joou.UByte;
import org.joou.Unsigned;
import org.programmatori.domotica.own.sdk.utils.StringIterator;

public class ArrayUtils {

	public static UByte[] subArray(UByte[] values, int beginPosition, int endPosition) {

		UByte[] ret = new UByte[endPosition - beginPosition];

		for (int i = beginPosition; i < endPosition && i < values.length; i++) {
			ret[i-beginPosition] = values[i];
		}

		return ret;
	}

	public static UByte logicalXOR(UByte x, UByte y) {
		return Unsigned.ubyte(x.intValue() ^ y.intValue());
	}

	private static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(List<UByte> bytes) {
	    char[] hexChars = new char[bytes.size() * 2 + bytes.size() -1];
	    for ( int j = 0; j < bytes.size(); j++ ) {
	        int v = bytes.get(j).shortValue() & 0xFF;
	        int start = (j * 2) + j;

	        hexChars[start] = hexArray[v >>> 4];
	        hexChars[start + 1] = hexArray[v & 0x0F];

	        if (hexChars.length > start + 2)
	        	hexChars[start + 2] = ':';
	    }
	    return new String(hexChars).toLowerCase();
	}

	public static String byteToHex(UByte bValue) {
		String s = "";
		int v = bValue.shortValue() & 0xFF;
		 s += hexArray[v >>> 4];
	     s += hexArray[v & 0x0F];

	     return s;
	}

	public static UByte hexToByte(String sValue) {
		byte data = (byte) ((Character.digit(sValue.charAt(0), 16) << 4)
				+ Character.digit(sValue.charAt(1), 16));

		return UByte.valueOf(data);
	}

	public static List<UByte> asList(UByte[] array) {
		return Arrays.asList(array);
	}

	public static UByte[] stringToArray(String bytesMsg) {
		StringIterator iter = new StringIterator(bytesMsg, ':');

		List<UByte> list = new ArrayList<>();

		while (iter.hasNext()) {
			String sByte = iter.nextString();
			UByte uByte = ArrayUtils.hexToByte(sByte);
			list.add(uByte);
		}

		UByte[] uBytes = new UByte[list.size()];

		return list.toArray(uBytes);
	}
}
