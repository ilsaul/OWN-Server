package org.programmatori.domotica.own.server.clients;

public enum ClientSessionType {

	/** Before Connection */
	NONE(-1),

	/** If client want to send command and receive the replay for the command */
	MODE_COMMAND(0),

	/** If client want to receive any message from the bus */
	MODE_MONITOR(1),

	/** Not exist in SCS this mode but I add for testing purpose. It's a mix of MODE_COMMAND and MODE_MONITOR */
	MODE_TEST(2);

	private final int id;

	ClientSessionType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
