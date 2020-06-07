package org.programmatori.domotica.own.server.utils;

/**
 * This represent the step the use need to follow for connect
 */
public enum ConnectionState {
	/** Send welcome message */
	START,

	/** I check what client want to do */
	MODE,

	/** Depend on config */
	PASSWORD,

	WAIT_IDENT,
	CONNECTED,
	DISCONNECTED;
}
