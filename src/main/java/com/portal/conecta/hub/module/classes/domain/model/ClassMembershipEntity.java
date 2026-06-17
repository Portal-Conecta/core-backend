package com.portal.conecta.hub.module.classes.domain.model;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
	name = "user_classes",
	indexes = {
		@Index(name = "idx_user_classes_user_id", columnList = "user_id"),
		@Index(name = "idx_user_classes_class_id", columnList = "class_id")
	}
)
public class ClassMembershipEntity {

	@EmbeddedId
	private ClassMembershipId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("userId")
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("classId")
	@JoinColumn(name = "class_id", nullable = false)
	private ClassEntity classEntity;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_class", nullable = false, length = 30)
	private ClassRole classRole;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected ClassMembershipEntity() {
	}

	public ClassMembershipEntity(UserEntity user, ClassEntity classEntity, ClassRole classRole) {
		this.user = Objects.requireNonNull(user, "user não pode ser nulo");
		this.classEntity = Objects.requireNonNull(classEntity, "classEntity não pode ser nulo");
		this.classRole = Objects.requireNonNull(classRole, "classRole não pode ser nulo");
		this.id = buildIdWhenAvailable(user, classEntity);
		user.getClassMemberships().add(this);
		classEntity.getClassMemberships().add(this);
	}

	@PrePersist
	private void prePersist() {
		if (id == null || id.getUserId() == null || id.getClassId() == null) {
			id = new ClassMembershipId(requiredId(user.getId(), "user"), requiredId(classEntity.getId(), "classEntity"));
		}
		createdAt = Instant.now();
	}

	private static ClassMembershipId buildIdWhenAvailable(UserEntity user, ClassEntity classEntity) {
		if (user.getId() == null || classEntity.getId() == null) {
			return new ClassMembershipId();
		}
		return new ClassMembershipId(user.getId(), classEntity.getId());
	}

	private static UUID requiredId(UUID id, String entityName) {
		if (id == null) {
			throw new IllegalStateException(entityName + " deve ter um id antes de persistir a matrícula da turma");
		}
		return id;
	}

	public ClassMembershipId getId() {
		return id;
	}

	public UserEntity getUser() {
		return user;
	}

	public ClassEntity getClassEntity() {
		return classEntity;
	}

	public ClassRole getClassRole() {
		return classRole;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void promoteToRepresentative(UserEntity executor) {
		this.classRole = ClassRole.REPRESENTATIVE;
		this.user.promoteTo(TypeUser.REPRESENTATIVE, executor);
	}

	public void demoteToStudent(UserEntity executor) {
		this.classRole = ClassRole.STUDENT;
		this.user.demoteTo(TypeUser.STUDENT,executor);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ClassMembershipEntity that)) {
			return false;
		}
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return ClassMembershipEntity.class.hashCode();
	}

	public boolean isActive() {
		return user.isActive() && user.getDeletedAt() == null && !classEntity.isDeleted();
	}
}
