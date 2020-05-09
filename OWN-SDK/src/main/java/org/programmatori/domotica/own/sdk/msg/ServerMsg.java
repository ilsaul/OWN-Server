package org.programmatori.domotica.own.sdk.msg;

public enum ServerMsg {
	MSG_NACK("*#*0##"),
	MSG_ACK("*#*1##"),

	/** Command not operative / not know */
	MSG_NOP("*#*2##"),
	/** Command manage but receiver device don't replay (not exist) */
	MSG_RET("*#*3##"),
	/** Command non execute because message collision on bus */
	MSG_COLL("*#*4##"),
	/** Command not execute because impossible access to bus */
	MSG_NO_BUS("*#*5##"),
	/** Command not execute, because interface already busy in transmission */
	MSG_BUSY("*#*6##"),
	/** Procedure multi frame not execute complete */
	MSG_PROC("*#*7##");

	private final String msgString;

	ServerMsg(String msgString) {
		this.msgString = msgString;
	}

	public SCSMsg getMsg() {
		return new SCSMsg(msgString);
	}

	public String getMsgString() {
		return msgString;
	}
}
