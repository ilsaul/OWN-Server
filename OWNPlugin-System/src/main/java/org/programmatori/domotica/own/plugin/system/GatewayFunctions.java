package org.programmatori.domotica.own.plugin.system;

public enum GatewayFunctions {
	/** Manage the time of the Gateway. */
	TIME(0),
	/** Get the Date of the Gateway. */
	DATE(1),
	/** Get the IP of the Gateway. */
	IP(10),
	/** Get the NetMask of the Gateway. */
	NETMASK(11),
	/** Get the Mac Address of the Gateway. */
	MAC_ADDRESS(12),
	/** Get the Model of the Gateway. */
	SERVER_MODEL(15),
	/** Get the Firmware version of the Gateway. */
	FIRMWARE_VERSION(16),
	/** Get the Starting time of the Gateway. */
	STARTUP_TIME(19),
	/** Get the current Time and Date of the Gateway. */
	TIME_DATE(22),
	/** Get the Kernel version of the Gateway. */
	KERNEL_VERSION(23),
	/** Get the Distributin version of the Gateway. */
	DISTRIBUTION_VERSION(24);
;
	private final int functionId;

	GatewayFunctions(int id) {
		functionId = id;
	}

	public static GatewayFunctions createFromId(int id) {
		for (GatewayFunctions fId : values()) {
			if (fId.getFunctionId() == id) {
				return fId;
			}
		}

		throw new IllegalStateException("Unexpected value: " + id);
	}

	public int getFunctionId() {
		return functionId;
	}
}
