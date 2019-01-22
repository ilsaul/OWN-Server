package org.programmatori.domotica.own.sdk.msg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This logger Must use olny for log information that arrive on the bus.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1, 13/01/2019
 */
public class MessageBusLog {
	//Logger log = LoggerFactory.getLogger("org.programmatori.domotica.own.message");
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageBusLog.class);

	public MessageBusLog() {
	}

	/**
	 * Log Message SCSMsg
	 */
	public void log(SCSMsg msg, boolean isSend, Long id) {
		String direction = (isSend? "TX MSG:" : "RX MSG:");

		LOGGER.info("{} - {}{}", id, direction, msg.toString());
	}

	/**
	 * Log basic Message
	 */
	public void log(String msg) {
		LOGGER.info("{}", msg);
	}

}
