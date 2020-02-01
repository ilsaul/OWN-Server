package org.programmatori.domotica.own.server;

public enum ConnectionStatus {
	/** Send welcome message */
	START,

	/** I check what client want to do */
	MODE,

	/** I check if connection is secure */
	CHECK_IP,


	PASSWORD,
	WAIT_IDENT,
	CONNECTED,
	DISCONNECTED;
}
