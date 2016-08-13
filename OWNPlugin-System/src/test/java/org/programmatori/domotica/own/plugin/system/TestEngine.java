package org.programmatori.domotica.own.plugin.system;

import java.lang.Thread.State;
import java.util.LinkedList;

import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.sdk.server.engine.Monitor;
import org.programmatori.domotica.own.sdk.server.engine.Sender;

public class TestEngine implements EngineManager {
	public LinkedList<SCSMsg> msgs;

	public TestEngine() {
		msgs = new LinkedList<SCSMsg>();
	}

	@Override
	public void sendCommand(SCSMsg msg, Sender client) {
		msgs.add(msg);
	}

	@Override
	public void addMonitor(Monitor monitor) {
		// stub
	}

	@Override
	public void removeMonitor(Monitor monitor) {
		// stub
	}

	@Override
	public void start() {
		// stub
	}

	@Override
	public State getState() {
		// stub
		return null;
	}
}
