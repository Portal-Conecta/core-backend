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

- usuario: `dev`
- senha: `dev`

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
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

## Fluxo para novas funcionalidades

Antes de abrir merge para `main`, rode pelo menos:

```powershell
.\mvnw.cmd test
```

Para funcionalidades com banco, prefira adicionar testes de repository/service usando o profile `test`.
