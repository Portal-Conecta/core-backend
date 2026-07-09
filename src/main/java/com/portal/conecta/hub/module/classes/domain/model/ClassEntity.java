package com.portal.conecta.hub.module.classes.domain.model;

import com.portal.conecta.hub.module.classes.domain.exception.InvalidClassDataException;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Entidade de domínio que representa uma turma no sistema.
 *
 * <p>O nome da turma é gerado automaticamente a partir do código do curso
 * concatenado ao número da turma — não é fornecido pelo usuário.</p>
 *
 * <p>O ciclo de vida de uma turma envolve quatro estados distintos:</p>
 * <ul>
 *   <li><b>Ativa:</b> estado padrão após criação; participa dos fluxos normais.</li>
 *   <li><b>Desativada:</b> marcada como inativa via {@link #deactivate}; preservada na base.
 *       Reversível via {@link #reactivate}.</li>
 *   <li><b>Removida logicamente:</b> {@code deletedAt} preenchido via {@link #delete};
 *       invisível nos fluxos normais. Reversível via {@link #restore}.</li>
 *   <li><b>Excluída fisicamente:</b> fora do escopo desta entidade.</li>
 * </ul>
 *
 * <p>Desativação e exclusão lógica são operações independentes: {@code active} e
 * {@code deletedAt} controlam estados diferentes e não se substituem.</p>
 */

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

	@Column(name = "active", nullable = false)
	private boolean active;

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

	/**
	 * Cria uma nova turma com estado ativo.
	 *
	 * <p>O nome é gerado automaticamente como {@code course.code + number}.
	 * A turma é adicionada à coleção de turmas do curso no mesmo instante.</p>
	 *
	 * @param shift     turno da turma.
	 * @param number    número identificador da turma dentro do curso.
	 * @param course    curso ao qual a turma pertence.
	 * @param createdBy usuário responsável pela criação.
	 * @return nova instância de {@code ClassEntity} pronta para persistência.
	 */
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
		entity.active = true;
		entity.setCreatedBy(createdBy);
		return entity;
	}

	/**
	 * Realiza a exclusão lógica da turma preenchendo {@code deletedAt}.
	 *
	 * <p>Idempotente: se a turma já estiver removida logicamente, a operação é ignorada.</p>
	 *
	 * @param deletedBy usuário que executou a remoção.
	 */
	public void delete (UserEntity deletedBy){
		if (this.deletedAt != null){
			return;
		}
		this.deletedAt = Instant.now();
		this.deletedBy = deletedBy;
	}

	/**
	 * Restaura uma turma removida logicamente, limpando {@code deletedAt} e {@code deletedBy}.
	 *
	 * @param updatedBy usuário que executou a restauração.
	 * @throws InvalidClassDataException se a turma não estiver removida logicamente.
	 */
	public void restore(UserEntity updatedBy){
		if (this.deletedAt == null){
			throw new InvalidClassDataException("A turma já está ativa.");
		}
		this.deletedAt = null;
		this.deletedBy = null;
		this.updatedBy = updatedBy;
		this.updatedAt = Instant.now();
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

	public boolean isActive() {
		return this.active;
	}

	/**
	 * Desativa a turma, marcando {@code active} como {@code false}.
	 *
	 * <p>Não altera {@code deletedAt}. Reversível via {@link #reactivate}.</p>
	 *
	 * @param updatedBy usuário que executou a desativação.
	 * @throws InvalidClassDataException se a turma já estiver inativa.
	 */
	public void deactivate(UserEntity updatedBy) {
		if (!this.active) {
			throw new InvalidClassDataException("A turma já está inativa.");
		}
		this.active = false;
		this.updatedBy = updatedBy;
		this.updatedAt = Instant.now();
	}

	/**
	 * Reativa uma turma desativada, marcando {@code active} como {@code true}.
	 *
	 * @param updatedBy usuário que executou a reativação.
	 * @throws InvalidClassDataException se a turma já estiver ativa.
	 */
	public void reactivate(UserEntity updatedBy) {
		if(this.active) {
			throw new InvalidClassDataException("A turma já está ativa.");
		}
		this.active = true;
		this.updatedBy = updatedBy;
		this.updatedAt = Instant.now();
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
