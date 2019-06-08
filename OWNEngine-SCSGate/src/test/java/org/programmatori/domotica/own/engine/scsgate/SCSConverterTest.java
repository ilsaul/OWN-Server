package org.programmatori.domotica.own.engine.scsgate;

import org.joou.UByte;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.programmatori.domotica.own.engine.util.ArrayUtils;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.utils.StringIterator;

import java.util.ArrayList;
import java.util.List;

public class SCSConverterTest {
	private SCSConverter converter = new SCSConverter();

	private void bytesToSCS(String bytesMsg, String scsMsg, String assertMsg)  {
		try {
			UByte[] bytes = ArrayUtils.stringToArray(bytesMsg);
			SCSMsg expected = new SCSMsg(scsMsg);
			SCSMsg actual = converter.convertToSCS(bytes);
			Assert.assertEquals(assertMsg, expected, actual);
		} catch (MessageFormatException e) {
			Assert.fail("Can't throw exception");
		}
	}

	@Test
	public void convertToSCS() {
		// Normal Light
		bytesToSCS("a8:b8:24:12:00:8e:a3", "*1*1*24##","Normal light off message");
		bytesToSCS("a8:b8:24:12:01:8f:a3", "*1*0*24##","Normal light on message");

		// Other Bus
		bytesToSCS("a8:e4:01:00:00:24:ca:12:01:18:a3", "*1*0*24#4#1##","Other Bus light on message");

		// Normal Blind
		bytesToSCS("a8:b8:62:12:08:c0:a3", "*2*1*62##","Normal Blind Up message");
		bytesToSCS("a8:b8:62:12:09:c1:a3", "*2*2*62##","Normal Blind Down message");
		bytesToSCS("a8:b8:62:12:0a:c2:a3", "*2*0*62##","Normal Blind Stop message");

		// GENeral Command
		bytesToSCS("a8:b5:09:12:08:a6:a3", "*2*1*##","General Blind Up message");
		bytesToSCS("a8:b5:09:12:09:a7:a3", "*2*2*##","General Blind Down message");
		bytesToSCS("a8:b5:09:12:0a:a4:a3", "*2*0*##","General Blind Stop message");

		// GRoup Command
		bytesToSCS("a8:b3:07:12:01:a7:a3", "*1*0*07##","Area Light on message");

		// Status General
		bytesToSCS("a8:b1:00:15:00:a4:a3","*#1*0##","General Status");
		//SCSToBytes("*#2*0##","a8:b1:00:15:00:a4:a3","General Status");

		// Request Status
		bytesToSCS("a8:24:ca:15:00:fb:a3", "*#1*24##","Command Light XXX message");
		bytesToSCS("a8:62:ca:15:00:bd:a3", "*#2*62##","Command Blind XXX message");



		bytesToSCS("01:a5", "*#*1###","ACK message");
	}

	@Test
	public void convertFromSCS() {
		// Status Light
		SCSToBytes("*1*1*24##","a8:b8:24:12:00:8e:a3","Status light off");
		SCSToBytes("*1*0*24##","a8:b8:24:12:01:8f:a3","Status light on");

		// Status Blind
		SCSToBytes("*2*1*62##","a8:b8:62:12:08:c0:a3","Status Blind Up");
		SCSToBytes("*2*2*62##","a8:b8:62:12:09:c1:a3","Status Blind Down");
		SCSToBytes("*2*0*62##","a8:b8:62:12:0a:c2:a3","Status Blind Stop");

		// Other Bus
		SCSToBytes("*1*0*24#4#1##","a8:e4:01:00:00:24:ca:12:01:18:a3","Other Bus light on message");

		// GENeral Command
		SCSToBytes("*2*1*##","a8:b5:09:12:08:a6:a3","General Blind Up message");

		// GRoup Command
		SCSToBytes("*1*0*07##","a8:b3:07:12:01:a7:a3","Area Light on message");

		// Request Status
		//SCSToBytes("*#1*24##","a8:24:ca:15:00:fb:a3","Command Light XXX message");

		// Command
		//SCSToBytes("*1*1*24##","a8:24:ca:15:00:fb:a3","Command Light on");
		//SCSToBytes("*1*0*24##","a8:24:ca:12:01:fd:a3","Command Light off");


		// Status General
		SCSToBytes("*#1*0##","a8:b1:00:15:00:a4:a3","General Status");
		//SCSToBytes("*#2*0##","a8:b1:00:15:00:a4:a3","General Status");

	}

	private void SCSToBytes(String scsMsg, String bytesMsg, String assertMsg) {
		try {
			SCSMsg msg = new SCSMsg(scsMsg);
			UByte[] expected = ArrayUtils.stringToArray(bytesMsg);
			UByte[] actual = converter.convertFromSCS(msg);
			Assert.assertArrayEquals(assertMsg, expected, actual);

		} catch (MessageFormatException e) {
			Assert.fail("Can't throw exception");
		}
	}
}