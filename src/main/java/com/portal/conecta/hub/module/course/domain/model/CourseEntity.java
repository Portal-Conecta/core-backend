package com.portal.conecta.hub.module.course.domain.model;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.course.domain.exception.DeletedCourseException;
import com.portal.conecta.hub.module.course.domain.exception.InvalidCourseDataException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
	name = "courses",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_courses_name", columnNames = "name"),
		@UniqueConstraint(name = "uk_courses_code", columnNames = "code")
	}
)
public class CourseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "code", nullable = false, length = 50)
	private String code;

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

	@OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
	private Set<ClassEntity> classes = new LinkedHashSet<>();

	protected CourseEntity() {
	}

	public CourseEntity(String name, String code) {
		this.name = Objects.requireNonNull(name, "name must not be null");
		this.code = Objects.requireNonNull(code, "code must not be null");
	}

	public static CourseEntity create(String name, String code) {
		return new CourseEntity(name, code);
	}

	public CourseEntity update(String name, String code, UserEntity updatedBy) {
		if (name != null) this.name = name;
		if (code != null) this.code = code;
		this.updatedAt = Instant.now();
		this.updatedBy = updatedBy;

		return this;
	}

	public void validateNotDeleted() {
		if (this.deletedAt != null) {
			throw new DeletedCourseException("Course is deleted: " + this.id);
		}
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

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
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
		classes.forEach(classEntity -> classEntity.delete(deletedBy));
	}

	public Set<ClassEntity> getClasses() {
		return classes;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CourseEntity that)) {
			return false;
		}
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return CourseEntity.class.hashCode();
	}
}
