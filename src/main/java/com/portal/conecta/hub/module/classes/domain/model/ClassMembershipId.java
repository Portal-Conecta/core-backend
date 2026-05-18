package com.portal.conecta.hub.module.classes.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ClassMembershipId implements Serializable {

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "class_id", nullable = false)
	private UUID classId;

	protected ClassMembershipId() {
	}

	public ClassMembershipId(UUID userId, UUID classId) {
		this.userId = Objects.requireNonNull(userId, "userId must not be null");
		this.classId = Objects.requireNonNull(classId, "classId must not be null");
	}

	public UUID getUserId() {
		return userId;
	}

	public UUID getClassId() {
		return classId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ClassMembershipId that)) {
			return false;
		}
		return Objects.equals(userId, that.userId) && Objects.equals(classId, that.classId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, classId);
	}
}
