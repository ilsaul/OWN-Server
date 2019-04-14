package org.programmatori.domotica.own.engine.row;

public enum SCSGateState {

	STATE_INITIAL("Connecting Arduino ..."),
	STATE_ARDUINO_READY("Arduino Ready"),
	STATE_SCS_GATE_SET_SLOW_SPEED("Speed SCSGate to 38400 baud"),
	STATE_SCS_GATE_SET_VOLT("Volt SCSGate to +5V"),
	STATE_SCS_GATE_SET_ASCII("ASCII Mode in SCSGate"),
	STATE_SCS_GATE_SET_LOG("Log Mode in SCSGate to continuative Log"),
	STATE_SCS_GATE_READY("Connection SCSGate is Ready");

	private String description = null;

	private SCSGateState(String desc) {
        this.description = desc;
    }

    public String getDescription() {
        return this.description;
    }

	private static SCSGateState[] vals = values();

	public SCSGateState next() {
		return vals[(this.ordinal()+1) % vals.length];
	}
}
