package com.portal.conecta.hub.module.room.domain.model;

public enum TypeRoom {
	CLASSROOM,
	LABORATORY,
	AUDITORIUM,
	OTHER;

	public String toApiValue() {
		return name().toLowerCase();
	}

	public static TypeRoom fromApiValue(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return TypeRoom.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}