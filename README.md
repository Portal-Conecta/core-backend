# Portal Conecta - Core Backend

API e logica de negocio do Portal Conecta.

## Ambientes

- `dev`: ambiente local padrao. Usa H2 em memoria para subir sem depender de PostgreSQL.
- `test`: ambiente dos testes automatizados. Usa H2 isolado e recria o schema a cada execucao.
- `prod`: ambiente de producao. Usa PostgreSQL configurado por variaveis de ambiente.

## Autenticacao Maven para portal-logging

O Core depende da biblioteca privada `com.portal.conecta:portal-logging`, publicada no GitHub Packages. Para baixar essa dependencia localmente ou durante o build Docker, configure estas variaveis no `.env` local:

- `MAVEN_USERNAME`: seu usuario do GitHub.
- `MAVEN_PASSWORD`: um GitHub Personal Access Token com permissao `read:packages` e acesso ao pacote `Portal-Conecta/portal-logging`.

Crie o token no GitHub em **Settings > Developer settings > Personal access tokens**. Para classic token, marque `read:packages`. Se a organizacao exigir SSO, autorize o token para a organizacao `Portal-Conecta`.

Crie o `.env` a partir do exemplo e preencha `MAVEN_USERNAME` e `MAVEN_PASSWORD`:

```powershell
Copy-Item .env.example .env
```

Exemplo:

```env
MAVEN_USERNAME=seu-usuario-github
MAVEN_PASSWORD=<token-com-read-packages>
```

O Docker Compose carrega o `.env` automaticamente, entao o build ja usa essas credenciais:

```powershell
docker compose build api
docker compose up --build
```

Para os compose da raiz do workspace, crie tambem o `.env` na raiz a partir de `.env.example`, ou informe explicitamente este arquivo:

```powershell
docker compose --env-file .\core-backend\.env -f .\docker-compose.gateway-core.yml build core
docker compose --env-file .\core-backend\.env -f .\docker-compose.all.yml build core
```

Para Maven local, carregue o `.env` na sessao antes de rodar o wrapper. O Maven usa automaticamente `.mvn/settings.xml` por causa do arquivo `.mvn/maven.config`.

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

Nao commite o `.env`, tokens reais, `settings.xml` com credenciais fixas, comandos com token real ou qualquer outro arquivo contendo segredo. O build Docker recebe `MAVEN_PASSWORD` como BuildKit secret para evitar gravar o token nas camadas da imagem.

## Rodar localmente

Use JDK 21. Confira antes de rodar o Maven:

```powershell
java -version
.\mvnw.cmd -version
```

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
