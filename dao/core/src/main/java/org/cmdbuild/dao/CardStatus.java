package org.cmdbuild.dao;

public enum CardStatus {
	ACTIVE("A"), UPDATED("U"), INACTIVE("N"), INACTIVE_USER("D");

	private final String value;

	CardStatus(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public boolean isActive() {
		return ACTIVE.equals(this);
	}

	public static CardStatus fromString(String value) {
		for (CardStatus status : CardStatus.values()) {
			if (status.value.equals(value))
				return status;
		}
		return CardStatus.INACTIVE;
	}
}
