# Portal Conecta - Core Backend

API e logica de negocio do Portal Conecta.

## Ambientes

- `dev`: ambiente local padrao. Usa H2 em memoria para subir sem depender de PostgreSQL.
- `test`: ambiente dos testes automatizados. Usa H2 isolado e recria o schema a cada execucao.
- `prod`: ambiente de producao. Usa PostgreSQL configurado por variaveis de ambiente.

## Rodar localmente

```powershell
.\mvnw.cmd spring-boot:run
```

O profile `dev` e ativado por padrao. A aplicacao sobe em `http://localhost:8080`.

Credenciais locais temporarias:

- usuario: `admin@portal.test`
- senha: `123456`

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

## Rodar com Docker Compose

Copie o arquivo de exemplo e ajuste os segredos antes de transferir para outra maquina:

```powershell
Copy-Item .env.example .env
```

Depois suba API e PostgreSQL:

```powershell
docker compose up --build
```

A API fica em `http://localhost:8080` e o PostgreSQL em `localhost:5432`.
O compose usa o profile `dev` com PostgreSQL, configurado por variaveis de ambiente, para manter o desenvolvimento mais parecido com ambientes reais. Sem essas variaveis, o mesmo profile `dev` ainda consegue subir localmente com H2 em memoria pelo Maven.
Se o volume local ja tiver tabelas criadas antes do Flyway, o compose ativa `FLYWAY_BASELINE_ON_MIGRATE=true` para criar a tabela de historico e considerar o schema atual como baseline. Para recriar o banco do zero e executar as migrations desde `V1`, use `docker compose down -v` antes de subir novamente.

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

## Rodar testes

```powershell
.\mvnw.cmd test
```

## Rodar em producao

Configure as variaveis antes de iniciar:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:postgresql://localhost:5432/portal_conecta"
$env:DB_USERNAME="portal_conecta"
$env:DB_PASSWORD="troque-esta-senha"
$env:APP_SECURITY_USER="admin"
$env:APP_SECURITY_PASSWORD="troque-esta-senha"
.\mvnw.cmd spring-boot:run
```

Observacao: o profile `prod` usa `ddl-auto=validate` e nao ativa baseline automatico do Flyway. Em um banco de producao existente, execute um baseline de forma deliberada antes do primeiro deploy com migrations.
