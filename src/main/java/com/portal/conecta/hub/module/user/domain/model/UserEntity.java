package com.portal.conecta.hub.module.user.domain.model;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.*;

/**
 * Agregado raiz do módulo de usuários do Hub Core.
 *
 * <p>Representa um usuário cadastrado no sistema com identidade, credencial,
 * tipo e rastreabilidade de auditoria ({@code createdBy}, {@code updatedBy}, {@code deletedBy}).
 *
 * <p>Instâncias devem ser criadas pelo factory method {@link #create}, que valida
 * os campos obrigatórios e codifica a senha antes de persistir.
 * O construtor protegido existe apenas para uso do JPA.
 *
 * <p>Soft delete: a exclusão não remove o registro do banco — marca {@code active = false},
 * registra {@code deletedAt} e {@code deletedBy}. Usuários excluídos não podem ser editados.
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

    /**
     * Cria um novo usuário com senha codificada.
     *
     * <p>Valida que nome, e-mail, senha e tipo não são nulos ou em branco antes de persistir.
     * A senha recebida em texto plano é codificada pelo {@code passwordEncoder} fornecido;
     * o texto original não é retido.
     *
     * @param name            nome do usuário.
     * @param email           e-mail do usuário.
     * @param rawPassword     senha em texto plano; será codificada antes de armazenar.
     * @param type            tipo do usuário, que determina permissões no sistema.
     * @param createdBy       usuário responsável pela criação; pode ser {@code null} para seed inicial.
     * @param passwordEncoder codificador de senha; não pode ser {@code null}.
     * @return nova instância de {@link UserEntity} pronta para persistência.
     * @throws InvalidUserDataException se nome, e-mail, senha ou tipo forem nulos ou em branco.
     */
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

	/**
	 * Creates a user that still needs to activate the account and define a password.
	 *
	 * <p>The password hash must already be unusable for login purposes, because
	 * the real password is only defined during account activation.</p>
	 *
	 * @param name user name
	 * @param email normalized user e-mail
	 * @param unusablePasswordHash encoded placeholder password
	 * @param type user type
	 * @param createdBy authenticated user that created the account
	 * @return inactive user pending activation
	 */
	public static UserEntity createPendingActivation(
			String name,
			String email,
			String unusablePasswordHash,
			TypeUser type,
			UserEntity createdBy
	) {
		String validName = requireText(name, "O nome Ã© obrigatÃ³rio.");
		String validEmail = requireText(email, "O e-mail Ã© obrigatÃ³rio.");
		String validPasswordHash = requireText(unusablePasswordHash, "A senha Ã© obrigatÃ³ria.");
		TypeUser validType = requireType(type);

		UserEntity user = new UserEntity(validName, validEmail, validPasswordHash, validType, createdBy);
		user.active = false;
		return user;
	}

	/**
	 * Activates the user account and stores the password chosen by the user.
	 *
	 * @param rawPassword password chosen during activation
	 * @param updatedBy user recorded as the update author
	 * @param passwordEncoder encoder used to hash the chosen password
	 * @throws InvalidUserDataException when the user was removed or the password is invalid
	 */
	public void activate(String rawPassword, UserEntity updatedBy, PasswordEncoder passwordEncoder) {
		Objects.requireNonNull(passwordEncoder, "O codificador de senha nÃ£o pode ser nulo.");
		if (this.deletedAt != null) {
			throw new InvalidUserDataException("NÃ£o Ã© possÃ­vel ativar um usuÃ¡rio removido.");
		}

		String validPassword = requireText(rawPassword, "A senha Ã© obrigatÃ³ria.");
		this.passwordHash = passwordEncoder.encode(validPassword);
		this.active = true;
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

    /**
     * Desativa o usuário via soft delete.
     *
     * <p>Marca {@code active = false}, registra {@code deletedAt} e {@code deletedBy}.
     * O registro permanece no banco e pode ser identificado por {@code deletedAt != null}.
     *
     * @param deletedBy usuário executor da exclusão; pode ser {@code null} para exclusão programática.
     */
	public void delete(UserEntity deletedBy) {
		this.active = false;
		this.deletedAt = Instant.now();
		this.deletedBy = deletedBy;
	}

    /**
     * Promove o usuário para um novo tipo.
     *
     * @param newType     novo tipo a ser atribuído.
     * @param promotedBy  usuário executor da promoção.
     */
	public void promoteTo(TypeUser newType, UserEntity promotedBy) {
		this.type = newType;
		this.updatedBy = promotedBy;
		this.updatedAt = Instant.now();
	}

    /**
     * Rebaixa o usuário para um novo tipo.
     *
     * @param newType  novo tipo a ser atribuído.
     * @param executor usuário executor do rebaixamento.
     */
	public void demoteTo(TypeUser newType, UserEntity executor) {
		this.type = newType;
		this.updatedBy = executor;
		this.updatedAt = Instant.now();
	}

    /**
     * Atualiza nome, e-mail e avatar do usuário.
     *
     * <p>Apenas campos não nulos, não em branco e diferentes do valor atual são alterados.
     * Registra o executor em {@code updatedBy} e atualiza {@code updatedAt} sempre,
     * independentemente de quantos campos mudaram.
     *
     * @param name       novo nome; ignorado se nulo, em branco ou igual ao atual.
     * @param email      novo e-mail; ignorado se nulo, em branco ou igual ao atual (comparação case-insensitive).
     * @param avatarUrl  nova URL de avatar; ignorada se nula, em branco ou igual à atual.
     * @param updatedBy  usuário executor da atualização.
     * @return lista com os nomes dos campos efetivamente alterados; vazia se nenhum campo mudou.
     * @throws InvalidUserDataException se o usuário já estiver excluído.
     */
	public List<String> update(String name, String email, String avatarUrl, UserEntity updatedBy) {
		if (this.deletedAt != null) {
			throw new InvalidUserDataException("Não é possível editar um usuário excluído.");
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
