# Portal Conecta - Core Backend

O Core Backend é o serviço central do Portal Conecta. Ele concentra autenticação, usuários, cursos, turmas, vínculos acadêmicos, salas, notificações e regras centrais de autorização compartilhadas entre os módulos da plataforma.

Ele funciona como fonte oficial dos dados centrais usados por outros serviços e pelo frontend, normalmente por meio do API Gateway. Regras específicas de módulos como Checklist, Mapa de Sala e Comunicados não devem ser implementadas no Core; esses serviços consomem dados e contratos do Core quando precisam de identidade, estrutura acadêmica, permissões ou notificações.

## Sumário

- [Visão geral](#visão-geral)
- [Responsabilidades do Core](#responsabilidades-do-core)
- [Stack técnica](#stack-técnica)
- [Arquitetura do projeto](#arquitetura-do-projeto)
- [Módulos de domínio](#módulos-de-domínio)
- [Autenticação e autorização](#autenticação-e-autorização)
- [Perfis de acesso](#perfis-de-acesso)
- [API e documentação OpenAPI](#api-e-documentação-openapi)
- [Ambientes e profiles](#ambientes-e-profiles)
- [Autenticação Maven para portal-logging](#autenticação-maven-para-portal-logging)
- [Como rodar localmente](#como-rodar-localmente)
- [Como rodar com Docker Compose](#como-rodar-com-docker-compose)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Banco de dados e migrations](#banco-de-dados-e-migrations)
- [Mensageria](#mensageria)
- [Testes](#testes)
- [Observabilidade e health checks](#observabilidade-e-health-checks)
- [Segurança operacional](#segurança-operacional)
- [Troubleshooting](#troubleshooting)
- [Links úteis](#links-úteis)

## Visão geral

O Hub Core é o núcleo compartilhado do Portal Conecta. Ele responde por dados e regras que precisam ser consistentes em toda a plataforma:

- autenticação;
- usuários;
- perfis globais;
- cursos;
- turmas;
- vínculos acadêmicos;
- papéis contextuais em turma;
- salas;
- notificações;
- autorização central;
- contratos de integração consumidos por outros serviços.

A ideia central é evitar que cada serviço mantenha sua própria versão de usuário, turma, curso, sala ou permissão. O Core é a fonte oficial desses dados.

Fluxo conceitual:

```text
Frontend
   |
API Gateway
   |
Core Backend
   |
Banco de dados / RabbitMQ / contratos de integração
```

## Responsabilidades do Core

| Responsabilidade | Pertence ao Core? | Observação |
| --- | --- | --- |
| Autenticação e emissão de JWT | Sim | Login, refresh token, logout e ativação de conta. |
| Usuários e perfis globais | Sim | Cadastro, consulta, atualização, ativação e desativação. |
| Cursos | Sim | Cadastro, consulta e manutenção central de cursos. |
| Turmas | Sim | Cadastro, consulta, status e vínculo com curso. |
| Membros de turma | Sim | Associação de alunos/docentes e papéis contextuais. |
| Salas | Sim | Cadastro oficial de salas físicas. |
| Notificações | Sim | Consulta, leitura, descarte e processamento de notificações do usuário. |
| Contratos de integração | Sim | Endpoints e eventos consumidos por outros serviços. |
| Regras internas de Checklist | Não | Pertencem ao `checklist-backend`. |
| Layout detalhado do Mapa de Sala | Não | Pertence ao `mapa-sala-backend`. |
| Criação e gestão de Comunicados | Não | Pertence ao `comunicados-backend`. |
| Telas e componentes de UI | Não | Pertencem ao frontend. |

## Stack técnica

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Bean Validation
- PostgreSQL
- H2 para desenvolvimento e testes
- Flyway
- JWT com JJWT
- RabbitMQ
- Spring Mail
- Springdoc OpenAPI / Swagger UI
- Actuator
- Micrometer, Prometheus e OpenTelemetry
- Docker e Docker Compose
- JUnit 5, Spring Test e Testcontainers
- GitHub Actions para CI

## Arquitetura do projeto

O projeto é organizado por módulos de domínio. Cada módulo concentra suas próprias regras, casos de uso e camada HTTP.

Estrutura conceitual:

```text
src/main/java/com/portal/conecta/hub
├── HubApplication.java
├── module
│   ├── auth
│   ├── me
│   ├── user
│   ├── course
│   ├── classes
│   ├── room
│   └── notification
└── shared
    ├── config
    ├── context
    ├── exception
    ├── migration
    ├── observability
    └── security
```

Camadas esperadas:

| Camada | Função |
| --- | --- |
| `presentation` | Controllers, DTOs de request/response e documentação OpenAPI. |
| `application` | Use cases, commands, queries e resultados. |
| `domain` | Entidades, regras de negócio, validadores, portas e exceções de domínio. |
| `infrastructure` | Persistência, mensageria, segurança e integrações técnicas. |
| `shared` | Código transversal: segurança, exceções, contexto, configuração e observabilidade. |

Regra prática: regra de negócio deve ficar no domínio ou no caso de uso, não duplicada no controller.

## Módulos de domínio

| Módulo | Responsabilidade | Rotas principais |
| --- | --- | --- |
| Auth | Login, refresh token, logout e ativação de conta | `/auth` |
| Me | Dados do usuário autenticado e vínculos acadêmicos próprios | `/me` |
| Users | Cadastro, edição, desativação e consultas de usuários | `/users` |
| Courses | Cadastro e consulta de cursos | `/courses` |
| Classes | Turmas, vínculos de membros e representantes | `/classes` |
| Rooms | Cadastro e consulta de salas físicas | `/rooms` |
| Notification | Consulta, leitura, descarte e processamento de notificações | `/notifications` |

## Autenticação e autorização

O Core usa autenticação stateless baseada em JWT.

Rotas públicas principais:

- `POST /auth/login`
- `POST /auth/refresh`
- endpoints Swagger/OpenAPI
- endpoints Actuator de health e info

Todas as demais rotas exigem token JWT válido.

Uso do token:

```http
Authorization: Bearer <access-token>
```

Diferença entre status de segurança:

| Status | Significado |
| --- | --- |
| `401 Unauthorized` | A requisição não possui autenticação válida. |
| `403 Forbidden` | O usuário está autenticado, mas não possui permissão para a ação. |

A autorização fina por perfil deve ser aplicada nos use cases e validadores de domínio. Controllers devem receber a requisição, validar o contrato HTTP e delegar a execução.

## Perfis de acesso

Perfis globais do Core:

| Perfil | Responsabilidade |
| --- | --- |
| `ADMIN` | Administração geral e criação de perfis sensíveis. |
| `SENAI` | Gestão acadêmica: cursos, turmas e vínculos. |
| `WEG` | Gestão de salas. |
| `TEACHER` | Docente vinculado a turmas. |
| `REPRESENTATIVE` | Aluno representante em contexto de turma. |
| `STUDENT` | Aluno regular. |

Resumo da matriz de permissões:

| Área | Regra principal |
| --- | --- |
| Usuários sensíveis | Apenas `ADMIN` cria usuários `WEG`, `SENAI` e `ADMIN`. |
| Cursos | `ADMIN` e `SENAI` criam/editam; apenas `ADMIN` deleta. |
| Turmas | `ADMIN` e `SENAI` criam/editam; apenas `ADMIN` deleta; `SENAI` desativa. |
| Vínculos | `SENAI` associa aluno/professor à turma. |
| Salas | `ADMIN` e `WEG` criam/editam/removem/restauram. |
| Perfis operacionais | `TEACHER`, `REPRESENTATIVE` e `STUDENT` não executam ações administrativas globais. |

Observação: `TypeUser.REPRESENTATIVE` é o perfil global do usuário. `ClassRole.REPRESENTATIVE` é o papel contextual do usuário dentro de uma turma. O representante não deve ser tratado como permissão administrativa ampla.

## API e documentação OpenAPI

Com a aplicação rodando localmente:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Principais grupos de rotas:

- `/auth`
- `/me`
- `/users`
- `/courses`
- `/classes`
- `/rooms`
- `/notifications`

A documentação OpenAPI deve refletir os contratos reais dos controllers. Quando uma regra de permissão mudar, a descrição do endpoint afetado também deve ser revisada.

## Ambientes e profiles

| Profile | Uso | Banco | Observação |
| --- | --- | --- | --- |
| `dev` | Desenvolvimento local | H2 por padrão ou PostgreSQL via env | Profile padrão. |
| `test` | Testes automatizados | H2 em memória | Schema recriado nos testes. |
| `prod` | Produção | PostgreSQL | Exige variáveis de ambiente. |

A aplicação usa `dev` como profile padrão.

## Autenticação Maven para portal-logging

O Core depende da biblioteca privada `com.portal.conecta:portal-logging`, publicada no GitHub Packages. Para baixar essa dependência localmente ou durante o build Docker, configure estas variáveis no `.env` local:

- `MAVEN_USERNAME`: seu usuário do GitHub.
- `MAVEN_PASSWORD`: um GitHub Personal Access Token com permissão `read:packages` e acesso ao pacote `Portal-Conecta/portal-logging`.

Crie o token no GitHub em **Settings > Developer settings > Personal access tokens**. Para classic token, marque `read:packages`. Se a organização exigir SSO, autorize o token para a organização `Portal-Conecta`.

Crie o `.env` a partir do exemplo e preencha `MAVEN_USERNAME` e `MAVEN_PASSWORD`:

```powershell
Copy-Item .env.example .env
```

Exemplo:

```env
MAVEN_USERNAME=seu-usuario-github
MAVEN_PASSWORD=<token-com-read-packages>
```

O Docker Compose carrega o `.env` automaticamente, então o build já usa essas credenciais:

```powershell
docker compose build api
docker compose up --build
```

Para Maven local, carregue o `.env` na sessão antes de rodar o wrapper. O Maven usa automaticamente `.mvn/settings.xml` por causa do arquivo `.mvn/maven.config`.

PowerShell:

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -and $_ -notmatch '^\s*#') {
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), 'Process')
  }
}
.\mvnw.cmd -B dependency:go-offline
.\mvnw.cmd test
```

Bash:

```bash
set -a
. ./.env
set +a
./mvnw -B dependency:go-offline
./mvnw test
```

Não commite o `.env`, tokens reais, `settings.xml` com credenciais fixas, comandos com token real ou qualquer outro arquivo contendo segredo. O build Docker recebe `MAVEN_PASSWORD` como BuildKit secret para evitar gravar o token nas camadas da imagem.

## Como rodar localmente

Use JDK 21. Confira antes de rodar o Maven:

```powershell
java -version
.\mvnw.cmd -version
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

O profile `dev` é ativado por padrão. Sem variáveis de banco, a aplicação usa H2 em memória e sobe em:

```text
http://localhost:8080
```

Credenciais locais temporárias, quando a massa de desenvolvimento estiver disponível:

```text
usuário: admin@portal.test
senha: 123456
```

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

ou:

```bash
curl http://localhost:8080/actuator/health
```

H2 Console em `dev`:

```text
http://localhost:8080/h2-console
```

## Como rodar com Docker Compose

Copie o arquivo de exemplo e ajuste os segredos antes de transferir para outra máquina:

```powershell
Copy-Item .env.example .env
```

ou:

```bash
cp .env.example .env
```

Depois suba API, PostgreSQL e RabbitMQ:

```powershell
docker compose up --build
```

A API fica disponível em:

```text
http://localhost:8080
```

Serviços locais:

| Serviço | Endereço padrão |
| --- | --- |
| API | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |
| RabbitMQ | `localhost:5672` |
| RabbitMQ Management | `http://localhost:15672` |

O Compose usa o profile `dev` com PostgreSQL, configurado por variáveis de ambiente, para manter o desenvolvimento mais parecido com ambientes reais. Sem essas variáveis, o mesmo profile `dev` ainda consegue subir localmente com H2 em memória pelo Maven.

Se o volume local já tiver tabelas criadas antes do Flyway, o Compose ativa `FLYWAY_BASELINE_ON_MIGRATE=true` para criar a tabela de histórico e considerar o schema atual como baseline. Para recriar o banco do zero e executar as migrations desde `V1`, use `docker compose down -v` antes de subir novamente.

Para acompanhar logs:

```powershell
docker compose logs -f api
```

Para parar mantendo os dados:

```powershell
docker compose down
```

Para parar e apagar o volume do banco:

```powershell
docker compose down -v
```

## Variáveis de ambiente

| Variável | Obrigatória em produção? | Padrão local | Uso |
| --- | --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Sim | `dev` | Define `dev`, `test` ou `prod`. |
| `SERVER_PORT` | Não | `8080` | Porta HTTP da aplicação. |
| `MAVEN_USERNAME` | Para build privado | - | Usuário GitHub Packages. |
| `MAVEN_PASSWORD` | Para build privado | - | Token GitHub Packages com `read:packages`. |
| `POSTGRES_DB` | Para Compose | `portal_conecta` | Nome do banco criado no PostgreSQL local. |
| `POSTGRES_USER` | Para Compose | `portal_conecta` | Usuário do PostgreSQL local. |
| `POSTGRES_PASSWORD` | Para Compose | `portal_conecta_dev_password` | Senha do PostgreSQL local. |
| `POSTGRES_PORT` | Não | `5432` | Porta exposta do PostgreSQL local. |
| `DB_URL` | Sim em `prod` | H2 no Maven/dev | JDBC URL usada pela aplicação. |
| `DB_USERNAME` | Sim em `prod` | `sa` no H2 | Usuário do banco usado pela aplicação. |
| `DB_PASSWORD` | Sim em `prod` | vazio no H2 | Senha do banco usado pela aplicação. |
| `DB_DRIVER` | Não | `org.h2.Driver` em dev | Driver JDBC. |
| `FLYWAY_BASELINE_ON_MIGRATE` | Depende do ambiente | `false` no Maven/dev, `true` no Compose | Permite baseline em banco existente. |
| `JWT_SECRET` | Sim | segredo de desenvolvimento | Segredo usado para assinar tokens JWT. |
| `JWT_ACCESS_EXPIRATION` | Sim | `900000` | Expiração do access token, em milissegundos. |
| `JWT_REFRESH_EXPIRATION` | Sim | `604800000` | Expiração do refresh token, em milissegundos. |
| `RABBITMQ_ENABLED` | Não | `true` | Habilita infraestrutura de mensageria. |
| `RABBITMQ_HOST` | Não | `localhost` | Host do RabbitMQ. |
| `RABBITMQ_PORT` | Não | `5672` | Porta AMQP. |
| `RABBITMQ_MANAGEMENT_PORT` | Não | `15672` | Porta da UI de gerenciamento no Compose. |
| `RABBITMQ_USERNAME` | Não | `guest` | Usuário RabbitMQ. |
| `RABBITMQ_PASSWORD` | Não | `guest` | Senha RabbitMQ. |
| `SPRING_MAIL_HOST` | Para envio real | vazio | Host SMTP para ativação de conta. |
| `SPRING_MAIL_PORT` | Para envio real | `587` | Porta SMTP. |
| `SPRING_MAIL_USERNAME` | Para envio real | vazio | Usuário SMTP. |
| `SPRING_MAIL_PASSWORD` | Para envio real | vazio | Senha SMTP. |
| `ACCOUNT_ACTIVATION_BASE_URL` | Sim em ambientes reais | `http://localhost:3002/ativar-conta` | URL base enviada no e-mail de ativação. |
| `ACCOUNT_ACTIVATION_MAIL_FROM` | Sim em ambientes reais | `no-reply@portal-conecta.local` | Remetente do e-mail de ativação. |
| `PORTAL_ENVIRONMENT` | Não | `local` | Ambiente incluído nos logs estruturados. |

Não use valores de exemplo em produção.

## Banco de dados e migrations

Resumo por ambiente:

| Ambiente | Banco | Schema |
| --- | --- | --- |
| `dev` via Maven | H2 em memória por padrão | Flyway + `ddl-auto=validate` |
| `dev` via Docker Compose | PostgreSQL | Flyway + `ddl-auto=validate` |
| `test` | H2 em memória | Schema isolado para testes |
| `prod` | PostgreSQL | Flyway + `ddl-auto=validate` |

As migrations ficam em:

```text
src/main/resources/db/migration
```

Regras para migrations:

- criar nova migration para alteração incremental de schema;
- não editar migration já aplicada em ambiente compartilhado;
- validar migrations localmente antes do PR;
- usar baseline de forma deliberada quando houver banco existente;
- em produção, não depender de `ddl-auto` para criar schema.

Se o volume local do PostgreSQL estiver inconsistente, recrie:

```powershell
docker compose down -v
docker compose up --build
```

## Mensageria

O Core usa RabbitMQ para processar solicitações de notificação e publicar eventos de domínio consumidos por outros serviços.

Recursos principais:

| Recurso | Valor padrão |
| --- | --- |
| Notifications exchange | `notifications.exchange` |
| Notifications queue | `notifications.dispatch.q` |
| Notifications DLQ | `notifications.dispatch.dlq` |
| Notifications routing key | `notification.requested` |
| Course events exchange | `course-events.exchange` |
| Course created routing key | `course.created` |
| Course updated routing key | `course.updated` |
| Course deleted routing key | `course.deleted` |
| Class events exchange | `class-events.exchange` |
| Class created routing key | `class.created` |
| Class deleted routing key | `class.deleted` |

O Docker Compose sobe RabbitMQ com management UI em:

```text
http://localhost:15672
```

Credenciais locais padrão:

```text
usuário: guest
senha: guest
```

## Testes

Rodar testes localmente:

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

Rodar build limpo com testes:

```bash
./mvnw clean test
```

A CI do repositório executa `mvn clean test` em pushes e pull requests para `main` e `develop`.

Mudanças em regras de permissão devem ter testes cobrindo:

- cenário permitido;
- cenário negado;
- retorno esperado quando o usuário não possui permissão.

## Observabilidade e health checks

Endpoints disponíveis localmente:

```text
http://localhost:8080/actuator/health
http://localhost:8080/actuator/health/readiness
http://localhost:8080/actuator/info
http://localhost:8080/actuator/prometheus
```

Uso esperado:

| Endpoint | Uso |
| --- | --- |
| `/actuator/health` | Verificação geral da aplicação. |
| `/actuator/health/readiness` | Health check de prontidão, usado pelo Docker Compose. |
| `/actuator/info` | Informações expostas pelo Actuator. |
| `/actuator/prometheus` | Métricas no formato Prometheus. |

O Core também possui:

- logs estruturados via `portal-logging`;
- correlation ID para rastrear requisições;
- métricas Micrometer expostas para Prometheus;
- tracing via OpenTelemetry/OTLP.

Não exponha dados sensíveis em endpoints de observabilidade.

## Segurança operacional

Regras obrigatórias:

- não commitar `.env`;
- não usar `JWT_SECRET` de exemplo em produção;
- não logar senha;
- não logar access token completo;
- não logar refresh token;
- não logar header `Authorization`;
- não logar cookies de sessão;
- revisar testes sempre que alterar regra de permissão;
- manter Swagger/OpenAPI alinhado com a autorização real;
- usar `403 Forbidden` para usuário autenticado sem permissão;
- usar `401 Unauthorized` para autenticação ausente ou inválida.

## Troubleshooting

### Porta 8080 ocupada

Altere `SERVER_PORT` no `.env` ou finalize o processo que ocupa a porta.

Verificar processo na porta 8080 no Windows:

```powershell
netstat -ano | findstr :8080
```

### Banco local inconsistente

Se estiver usando Docker Compose:

```powershell
docker compose down -v
docker compose up --build
```

### Erro de migration em banco existente

Verifique `FLYWAY_BASELINE_ON_MIGRATE`.

Em banco de produção, faça baseline de forma deliberada antes do primeiro deploy com migrations.

### Token inválido ou expirado

Faça login novamente:

```http
POST /auth/login
```

ou renove a sessão:

```http
POST /auth/refresh
```

### 401 Unauthorized

Verifique se o header foi enviado:

```http
Authorization: Bearer <access-token>
```

### 403 Forbidden

O usuário está autenticado, mas o perfil não tem permissão para a ação.

Verifique:

- perfil global do usuário;
- validador de permissão do módulo;
- caso de uso executado;
- matriz de perfis de acesso.

### Swagger não abre

Confirme se a aplicação está rodando e acesse:

```text
http://localhost:8080/swagger-ui/index.html
```

### RabbitMQ indisponível

Verifique se o container está saudável:

```powershell
docker compose ps
docker compose logs -f rabbitmq
```

Confirme também as variáveis `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME` e `RABBITMQ_PASSWORD`.

### Health check falhando no Docker Compose

Verifique logs da API:

```powershell
docker compose logs -f api
```

Verifique se PostgreSQL e RabbitMQ estão saudáveis:

```powershell
docker compose ps
```

### Erro ao baixar portal-logging

Verifique:

- `MAVEN_USERNAME`;
- `MAVEN_PASSWORD`;
- permissão `read:packages`;
- autorização SSO do token para a organização `Portal-Conecta`, se aplicável;
- acesso ao pacote `Portal-Conecta/portal-logging`.

## Links úteis

Com a aplicação rodando localmente:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`
- Readiness: `http://localhost:8080/actuator/health/readiness`
- Prometheus: `http://localhost:8080/actuator/prometheus`
- H2 Console em `dev`: `http://localhost:8080/h2-console`
- RabbitMQ Management via Compose: `http://localhost:15672`

Documentos relacionados no projeto:

- [`CONTRIBUTING.md`](CONTRIBUTING.md)
- [`docs/swagger-documentation-guide.md`](docs/swagger-documentation-guide.md)
- [`docs/observability_log_contract.md`](docs/observability_log_contract.md)
- [`docs/account-activation-email.md`](docs/account-activation-email.md)
