package org.programmatori.domotica.own.plugin.system;

public enum GatewayModel {
	MH_SERVER(2),
	MH2000(4),
	F452(6),
	F452V(7),
	MH_SERVER_2(11),
	H4684(13),
	OWN_SERVER(99);

	private final int modelId;

	GatewayModel(int id) {
		modelId = id;
	}

	public static GatewayModel createById(int id) {
		for (GatewayModel fId : values()) {
			if (fId.getModelId() == id) {
				return fId;
			}
		}

		throw new IllegalStateException("Unexpected value: " + id);
	}

	public int getModelId() {
		return modelId;
	}
}
