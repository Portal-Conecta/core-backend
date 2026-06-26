package com.portal.conecta.hub.module.course.domain.model;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.course.domain.exception.DeletedCourseException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

/**
 * Agregado principal que representa um Curso no domínio.
 * <p>
 * Invariantes e Ciclo de Vida:
 * - O {@code name} (nome comercial/descritivo) e o {@code code} (identificador em sistemas/ERPs externos)
 * são chaves de negócio e possuem garantia de unicidade na plataforma.
 * - Utiliza padrão de exclusão lógica (soft delete) através da propriedade {@code deletedAt}.
 * - Mantém o histórico e rastreabilidade de quem executou ações de criação, atualização e exclusão.
 */
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
		this.name = Objects.requireNonNull(name, "Nome não pode ser nulo");
		this.code = Objects.requireNonNull(code, "Código não pode ser nulo");
	}

	public static CourseEntity create(String name, String code) {
		return new CourseEntity(name, code.trim());
	}

	public List<String> update(String name, String code, UserEntity updatedBy) {
		List<String> changed = new ArrayList<>();

		if (name != null && !name.equals(this.name)) {
			this.name = name;
			changed.add("name");
		}
		if (code != null && !code.equals(this.code)) {
			this.code = code;
			changed.add("code");
		}

		this.updatedAt = Instant.now();
		this.updatedBy = updatedBy;

		return changed;
	}

	public void validateNotDeleted() {
		if (this.deletedAt != null) {
			throw new DeletedCourseException("Curso excluído: " + this.id);
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
