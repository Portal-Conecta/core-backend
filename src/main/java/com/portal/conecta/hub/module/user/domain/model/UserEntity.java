package com.portal.conecta.hub.module.user.domain.model;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Agregado raiz de usuario no Core.
 *
 * <p>Concentra identidade, credenciais, auditoria e ciclo de vida da conta.
 * O campo {@link AccountStatus} e a fonte principal para diferenciar conta
 * pendente, ativa, desativada operacionalmente e marcada para exclusao futura.
 * O booleano {@code active} permanece apenas como compatibilidade temporaria.</p>
 */
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

	@Enumerated(EnumType.STRING)
	@Column(name = "account_status", nullable = false, length = 32)
	private AccountStatus accountStatus = AccountStatus.ACTIVE;

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
		this.name = requireText(name, "O nome e obrigatorio.");
		this.email = requireText(email, "O e-mail e obrigatorio.");
		this.passwordHash = requireText(passwordHash, "A senha e obrigatoria.");
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
		Objects.requireNonNull(passwordEncoder, "O codificador de senha nao pode ser nulo.");
		String validName = requireText(name, "O nome e obrigatorio.");
		String validEmail = requireText(email, "O e-mail e obrigatorio.");
		String validPassword = requireText(rawPassword, "A senha e obrigatoria.");
		TypeUser validType = requireType(type);
		String passwordHash = passwordEncoder.encode(validPassword);

		return new UserEntity(validName, validEmail, passwordHash, validType, createdBy);
	}

	public static UserEntity createPendingActivation(
			String name,
			String email,
			String unusablePasswordHash,
			TypeUser type,
			UserEntity createdBy
	) {
		String validName = requireText(name, "O nome e obrigatorio.");
		String validEmail = requireText(email, "O e-mail e obrigatorio.");
		String validPasswordHash = requireText(unusablePasswordHash, "A senha e obrigatoria.");
		TypeUser validType = requireType(type);

		UserEntity user = new UserEntity(validName, validEmail, validPasswordHash, validType, createdBy);
		user.active = false;
		user.accountStatus = AccountStatus.PENDING_ACTIVATION;
		return user;
	}

	public void activate(String rawPassword, UserEntity updatedBy, PasswordEncoder passwordEncoder) {
		Objects.requireNonNull(passwordEncoder, "O codificador de senha nao pode ser nulo.");
		if (this.accountStatus != AccountStatus.PENDING_ACTIVATION) {
			throw new InvalidUserDataException("Somente contas pendentes de ativacao podem ser ativadas.");
		}

		String validPassword = requireText(rawPassword, "A senha e obrigatoria.");
		this.passwordHash = passwordEncoder.encode(validPassword);
		this.active = true;
		this.accountStatus = AccountStatus.ACTIVE;
		this.deletedAt = null;
		this.deletedBy = null;
		this.updatedBy = updatedBy;
		this.updatedAt = Instant.now();
	}

	public void deactivate(UserEntity updatedBy) {
		if (this.accountStatus != AccountStatus.ACTIVE) {
			throw new InvalidUserDataException("Somente usuarios ativos podem ser desativados.");
		}

		this.active = false;
		this.accountStatus = AccountStatus.DISABLED;
		this.deletedAt = null;
		this.deletedBy = null;
		this.updatedBy = updatedBy;
		this.updatedAt = Instant.now();
	}

	public void reactivate(UserEntity updatedBy) {
		if (this.accountStatus != AccountStatus.DISABLED) {
			throw new InvalidUserDataException("Somente usuarios desativados podem ser reativados.");
		}

		this.active = true;
		this.accountStatus = AccountStatus.ACTIVE;
		this.deletedAt = null;
		this.deletedBy = null;
		this.updatedBy = updatedBy;
		this.updatedAt = Instant.now();
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
		return accountStatus == AccountStatus.ACTIVE;
	}

	public boolean isRemoved() {
		return accountStatus == AccountStatus.PENDING_DELETION;
	}

	public boolean isPendingActivation() {
		return accountStatus == AccountStatus.PENDING_ACTIVATION;
	}

	public boolean canAuthenticate() {
		return accountStatus == AccountStatus.ACTIVE;
	}

	public AccountStatus getAccountStatus() {
		return accountStatus;
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
		this.accountStatus = AccountStatus.PENDING_DELETION;
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

	public List<String> update(String name, String email, String avatarUrl, UserEntity updatedBy) {
		if (isRemoved()) {
			throw new InvalidUserDataException("Nao e possivel editar um usuario excluido.");
		}

		List<String> changed = new ArrayList<>();

		if (name != null && !name.isBlank() && !name.trim().equals(this.name)) {
			this.name = name.trim();
			changed.add("name");
		}
		if (email != null && !email.isBlank() && !email.trim().equalsIgnoreCase(this.email)) {
			this.email = email.trim();
			changed.add("email");
		}
		if (avatarUrl != null && !avatarUrl.isBlank() && !avatarUrl.trim().equals(this.avatarUrl)) {
			this.avatarUrl = avatarUrl.trim();
			changed.add("avatarUrl");
		}

		this.updatedBy = updatedBy;
		this.updatedAt = Instant.now();

		return changed;
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
			throw new InvalidUserDataException("Tipo de Usuario e obrigatorio.");
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
