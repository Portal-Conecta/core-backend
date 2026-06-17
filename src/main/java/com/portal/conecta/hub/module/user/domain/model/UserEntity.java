package com.portal.conecta.hub.module.user.domain.model;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
	name = "users",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_users_email", columnNames = "email")
	}
)
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "email", nullable = false, length = 180)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "avatar_url", length = 2048)
	private String avatarUrl;

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
	@Column(name = "type_user", nullable = false, length = 30)
	private TypeUser type;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private Set<ClassMembershipEntity> classMemberships = new LinkedHashSet<>();

	protected UserEntity() {
	}

	public UserEntity(String name, String email, String passwordHash, TypeUser typeUser) {
		this(name, email, passwordHash, typeUser, null);
	}

	public UserEntity(String name, String email, String passwordHash, TypeUser typeUser, UserEntity createdBy) {
		this.name = requireText(name, "O nome é obrigatório.");
		this.email = requireText(email, "O e-mail é obrigatório.");
		this.passwordHash = requireText(passwordHash, "A senha é obrigatória.");
		this.type = requireType(typeUser);
		this.createdBy = createdBy;
		this.updatedBy = createdBy;
	}

	public static UserEntity create(
			String name,
			String email,
			String rawPassword,
			TypeUser type,
			UserEntity createdBy,
			PasswordEncoder passwordEncoder
	) {
		Objects.requireNonNull(passwordEncoder, "O codificador de senha não pode ser nulo.");
		String validName = requireText(name, "O nome é obrigatório.");
		String validEmail = requireText(email, "O e-mail é obrigatório.");
		String validPassword = requireText(rawPassword, "A senha é obrigatória.");
		TypeUser validType = requireType(type);
		String passwordHash = passwordEncoder.encode(validPassword);

		return new UserEntity(validName, validEmail, passwordHash, validType, createdBy);
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

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public boolean isActive() {
		return active;
	}

	public String getAvatarUrl() {
		return avatarUrl;
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
		this.active = false;
		this.deletedAt = Instant.now();
		this.deletedBy = deletedBy;
	}

	public void promoteTo(TypeUser newType, UserEntity promotedBy) {
		this.type = newType;
		this.updatedBy = promotedBy;
		this.updatedAt = Instant.now();
	}

	public void demoteTo(TypeUser newType, UserEntity executor) {
		this.type = newType;
		this.updatedBy = executor;
		this.updatedAt = Instant.now();
	}

	public void update (String name, String email, String avatarUrl, UserEntity updatedBy) {

		if (this.deletedAt != null){
			throw new InvalidUserDataException("Não é possível editar um usuário excluído.");
		}
		if (name != null && !name.isBlank()){
			this.name = name.trim();
		}
		if (email != null && !email.isBlank()){
			this.email = email.trim();
		}
		if (avatarUrl != null && !avatarUrl.isBlank()){
			this.avatarUrl = avatarUrl.trim();
		}
		this.updatedBy = updatedBy;
		this.updatedAt = Instant.now();
	}

	public TypeUser getTypeUser() {
		return type;
	}

	public Set<ClassMembershipEntity> getClassMemberships() {
		return classMemberships;
	}

	private static String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new InvalidUserDataException(message);
		}

		return value.trim();
	}

	private static TypeUser requireType(TypeUser typeUser) {
		if (typeUser == null) {
			throw new InvalidUserDataException("Tipo de Usuário é obrigatório.");
		}

		return typeUser;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UserEntity that)) {
			return false;
		}
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return UserEntity.class.hashCode();
	}

}
