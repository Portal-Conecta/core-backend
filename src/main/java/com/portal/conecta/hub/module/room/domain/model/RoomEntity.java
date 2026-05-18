package com.portal.conecta.hub.module.room.domain.model;

import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "rooms")
public class RoomEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "number", nullable = false)
	private Integer number;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private UserEntity createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private UserEntity updatedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "deleted_by")
	private UserEntity deletedBy;

	@Enumerated(EnumType.STRING)
	@Column(name = "type_room", nullable = false, length = 30)
	private TypeRoom typeRoom;

	protected RoomEntity() {
	}

	public RoomEntity(Integer number, TypeRoom typeRoom) {
		this.number = Objects.requireNonNull(number, "number must not be null");
		this.typeRoom = Objects.requireNonNull(typeRoom, "typeRoom must not be null");
	}

	@PrePersist
	private void prePersist() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	public UUID getId() {
		return id;
	}

	public Integer getNumber() {
		return number;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	public UserEntity getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(UserEntity createdBy) {
		this.createdBy = createdBy;
	}

	public UserEntity getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(UserEntity updatedBy) {
		this.updatedBy = updatedBy;
	}

	public UserEntity getDeletedBy() {
		return deletedBy;
	}

	public void delete(UserEntity deletedBy) {
		this.deletedAt = Instant.now();
		this.deletedBy = deletedBy;
	}

	public TypeRoom getTypeRoom() {
		return typeRoom;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RoomEntity that)) {
			return false;
		}
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return RoomEntity.class.hashCode();
	}
}
