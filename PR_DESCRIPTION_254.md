# Titulo

#254 feat: permite pre-matricula de usuarios pendentes em turmas

# Pull Request

## Objetivo

Permitir que ADMIN e SENAI vinculem estudantes e docentes pendentes de ativacao a turmas, sem tratar essas contas como usuarios removidos.

Closes #254

---

## Mudancas principais

- Adiciona `AccountStatus` derivado no dominio de usuarios, com os estados `PENDING_ACTIVATION`, `ACTIVE` e `REMOVED`.
- Adiciona helpers em `UserEntity` para centralizar a semantica de ciclo de vida: `isRemoved()`, `isPendingActivation()` e `canAuthenticate()`.
- Ajusta a validacao de matricula em turmas para bloquear usuarios removidos, mantendo pendentes elegiveis quando `deletedAt == null`.
- Atualiza a listagem administrativa de membros de turma para retornar usuarios nao removidos, incluindo pendentes.
- Expande os contratos de membros de turma com `userName`, `active` e `accountStatus`, conforme aplicavel.
- Atualiza `POST /users/bulk` com `includePending`, mantendo `false` como comportamento padrao para retornar apenas usuarios ativos.
- Corrige a remocao logica para permitir desativar usuario pendente e continuar bloqueando usuario ja removido.

Fora do escopo: frontend, fluxo de ativacao, consultas de notificacao, endpoints de usuario fora dos contratos alterados e qualquer migration de banco.

---

## Como revisar

Ordem sugerida de revisao:

1. Revisar a semantica de ciclo de vida em `UserEntity`, `AccountStatus` e `DeactivateUserUseCase`.
2. Revisar a regra de negocio de membros de turma em `ClassMembershipValidator`, repository e use cases.
3. Revisar os DTOs/controllers e os testes de contrato.

Pontos que merecem mais atencao:

- Pendentes devem ser aceitos apenas em fluxos administrativos de pre-matricula.
- Usuarios removidos continuam bloqueados por `deletedAt != null`.
- Autenticacao, refresh, `/me`, notificacoes e promocao para representante seguem restritos a usuarios ativos e nao removidos.

Tipo de revisao esperada:

- [x] Regra de negocio
- [x] Arquitetura
- [x] Seguranca
- [ ] Performance
- [ ] Revisao rapida

---

## Como testar

Comando executado:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd test
```

Resultado:

- `BUILD SUCCESS`
- Testes: `744`
- Falhas: `0`
- Erros: `0`
- Ignorados: `20`

Observacao: os testes ignorados sao relacionados a dependencias indisponiveis no ambiente local, como Docker/Testcontainers/RabbitMQ.

---

## Contrato de API

### `GET /classes/{classId}/members`

Retorna membros nao removidos da turma, incluindo contas pendentes.

Item de resposta:

```json
{
  "id": "00000000-0000-0000-0000-000000000000",
  "name": "Nome do usuario",
  "classRole": "STUDENT",
  "active": false,
  "accountStatus": "PENDING_ACTIVATION"
}
```

### `POST /classes/{classId}/members`

Resposta:

```json
{
  "userId": "00000000-0000-0000-0000-000000000000",
  "userName": "Nome do usuario",
  "classId": "11111111-1111-1111-1111-111111111111",
  "classRole": "STUDENT",
  "active": false,
  "accountStatus": "PENDING_ACTIVATION"
}
```

### `POST /classes/{classId}/members/bulk`

`items` passa a usar o mesmo formato de `AddMemberResponse`.

### `POST /users/bulk`

Payload:

```json
{
  "ids": [
    "00000000-0000-0000-0000-000000000000"
  ],
  "includePending": true
}
```

Com `includePending` omitido ou `false`, o endpoint preserva o comportamento anterior e retorna apenas usuarios ativos. Com `true`, retorna usuarios nao removidos, incluindo pendentes.

Swagger/OpenAPI foi atualizado nos DTOs e controllers afetados.

---

## Impacto no backend

- [x] Controller/API
- [x] Service/regra de negocio
- [x] Repository/JPA
- [ ] Banco de dados/migration
- [x] Seguranca/autorizacao
- [ ] Configuracao
- [ ] Mensageria/eventos
- [ ] Cache

### Detalhes adicionais

Nao ha nova coluna nem migration. `accountStatus` e derivado de `active` e `deletedAt`:

- `deletedAt != null`: `REMOVED`
- `deletedAt == null && active == false`: `PENDING_ACTIVATION`
- `deletedAt == null && active == true`: `ACTIVE`

---

## Migracao e compatibilidade

- Nao existe migration.
- Ha mudanca de contrato nos endpoints administrativos de membros de turma, com campos adicionais na resposta.
- `POST /users/bulk` e retrocompativel quando `includePending` e omitido.
- Nao requer variavel de ambiente.
- Pode exigir ajuste no frontend apenas se ele consumir os novos campos ou quiser listar pendentes na administracao de turma.

---

## Seguranca e dados sensiveis

- A mudanca nao libera login para usuarios pendentes.
- A mudanca nao altera JWT, refresh token, `/me`, notificacoes ou promocao para representante.
- Nenhuma credencial, token, `.env` ou dado sensivel foi versionado.

---

## Riscos e rollback

Risco principal: consumidores que assumiam que a listagem de membros de turma retornava somente usuarios ativos podem passar a receber membros pendentes. O contrato agora explicita `active` e `accountStatus` para permitir tratamento correto na camada cliente.

Rollback: reverter os commits deste PR restaura o filtro anterior de usuarios ativos e remove os novos campos dos contratos administrativos.

---

## Checklist

- [x] Minha branch esta baseada na `develop`
- [x] O codigo compila sem erros
- [x] Rodei `.\mvnw.cmd test`
- [x] Nao subi arquivos desnecessarios (`.env`, `target/`, arquivos locais da IDE)
- [x] Endpoints novos ou alterados possuem contrato/payload descrito neste PR
- [x] Regras de permissao foram validadas no backend
- [x] Adicionei ou atualizei testes quando a mudanca envolve regra de negocio
- [x] Revisei meu proprio diff antes de solicitar review
- [x] Expliquei riscos, impactos ou decisoes tecnicas nao obvias
