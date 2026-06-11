package com.portal.conecta.hub.module.classes.domain.model;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
	name = "classes",
	indexes = {
		@Index(name = "idx_classes_course_id", columnList = "course_id")
	}
)
public class ClassEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(name = "shift", nullable = false, length = 30)
	private Shift shift;

	@Column(name = "number", nullable = false)
	private Integer number;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

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

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	private CourseEntity course;

	@OneToMany(mappedBy = "classEntity", fetch = FetchType.LAZY)
	private Set<ClassMembershipEntity> classMemberships = new LinkedHashSet<>();

	protected ClassEntity() {
	}

	public ClassEntity(Shift shift, Integer number, String name, CourseEntity course) {
		this.shift = Objects.requireNonNull(shift, "shift não pode ser nulo");
		this.number = Objects.requireNonNull(number, "number não pode ser nulo");
		this.name = Objects.requireNonNull(name, "name não pode ser nulo");
		this.course = Objects.requireNonNull(course, "course não pode ser nulo");
		course.getClasses().add(this);
	}

	public static ClassEntity create(
			Shift shift,
			Integer number,
			CourseEntity course,
			UserEntity createdBy
	) {
		Shift validShift = Objects.requireNonNull(shift, "shift é obrigatório");
		Integer validNumber = Objects.requireNonNull(number, "number é obrigatório");
		CourseEntity validCourse = Objects.requireNonNull(course, "course é obrigatório");

		String generatedName = validCourse.getCode() + validNumber;

		ClassEntity entity = new ClassEntity(validShift, validNumber, generatedName, validCourse);
		entity.setCreatedBy(createdBy);
		return entity;
	}

	public void delete (UserEntity deletedBy){
		if (this.deletedAt != null){
			return;
		}
		this.deletedAt = Instant.now();
		this.deletedBy = deletedBy;
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
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

	public Shift getShift() {
		return shift;
	}

	public Integer getNumber() {
		return number;
	}

	public String getName() {
		return name;
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

	public CourseEntity getCourse() {
		return course;
	}

	public Set<ClassMembershipEntity> getClassMemberships() {
		return classMemberships;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ClassEntity that)) {
			return false;
		}
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return ClassEntity.class.hashCode();
	}
}
