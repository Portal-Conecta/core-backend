# Plano de Issues: Testes de Integracao JPA

## Contexto

O sistema ja possui boa cobertura de testes unitarios para controllers, use cases, validators e entidades. A principal lacuna atual esta em testes de integracao com banco de dados, especialmente nos repositories que usam metodos derivados do Spring Data JPA e queries customizadas.

Estes testes devem validar a integracao entre:

- entidades JPA;
- repositories;
- queries derivadas e customizadas;
- constraints e relacionamentos;
- comportamento de registros removidos logicamente via `deletedAt`.

Nao e objetivo desta primeira rodada testar fluxos HTTP completos com `MockMvc` ou testes end-to-end com frontend. Esses podem vir depois, principalmente para login e `/me/courses`.

## Local sugerido

Criar os testes em:

```text
src/test/java/com/portal/conecta/hub/integration/repository/
  ClassMembershipRepositoryIntegrationTest.java
  UserRepositoryIntegrationTest.java
  CourseRepositoryIntegrationTest.java
  RoomRepositoryIntegrationTest.java
```

## Configuracao base sugerida

Usar:

```java
@DataJpaTest
@ActiveProfiles("test")
```

O profile `test` atual usa H2 em memoria com modo PostgreSQL e `ddl-auto: create-drop`. Isso e suficiente para validar mappings, queries JPA e filtros logicos da aplicacao nesta primeira etapa.

## Issue 1: Criar testes de integracao para `ClassMembershipRepository`

### Prioridade

Alta.

### Justificativa

Este repository concentra a maior parte do risco de integracao atual, pois cruza `UserEntity`, `ClassEntity`, `CourseEntity` e `ClassMembershipEntity`. Alem disso, possui queries customizadas com `@Query`, projection e filtros por `deletedAt`.

Repository alvo:

```text
src/main/java/com/portal/conecta/hub/module/classes/domain/port/ClassMembershipRepository.java
```

### Casos de teste

Implementar `ClassMembershipRepositoryIntegrationTest` cobrindo:

- `findCoursesByUserId` retorna os cursos e turmas vinculados ao usuario informado.
- `findCoursesByUserId` agrupa corretamente os dados esperados pela projection `UserCourseClassProjection`.
- `findCoursesByUserId` nao retorna cursos com `deletedAt` preenchido.
- `findCoursesByUserId` nao retorna turmas com `deletedAt` preenchido.
- `findCoursesByUserId` ordena por nome do curso e numero da turma.
- `existsByUserIdAndClassId` retorna `true` quando o usuario pertence a turma.
- `existsByUserIdAndClassId` retorna `false` quando nao existe vinculo.
- `countByUserIdAndClassRole` conta apenas memberships do usuario com o papel informado.
- `countByUserIdAndClassRole` ignora memberships ligados a turmas deletadas, conforme query atual.
- `countByClassIdAndClassRole` conta membros de uma turma por papel.

### Dados minimos para montar nos testes

- Um usuario aluno.
- Um usuario professor ou admin, se necessario para campos de auditoria.
- Dois cursos ativos.
- Uma turma por curso.
- Um membership do aluno em cada turma.
- Um curso deletado com turma e membership.
- Uma turma deletada com membership.

### Criterios de aceite

- O teste usa banco em memoria via `@DataJpaTest`.
- Nao usa mocks para repositories.
- As entidades sao persistidas e buscadas pelo repository real.
- As queries customizadas sao exercitadas diretamente.
- Os cenarios de curso deletado e turma deletada estao cobertos.

## Issue 2: Criar testes de integracao para `UserRepository`

### Prioridade

Alta.

### Justificativa

`UserRepository` e usado por autenticacao, criacao/listagem de usuarios e regras de unicidade de email. Tambem possui metodos para filtrar usuarios ativos, que dependem de `deletedAt`.

Repository alvo:

```text
src/main/java/com/portal/conecta/hub/module/user/domain/port/UserRepository.java
```

### Casos de teste

Implementar `UserRepositoryIntegrationTest` cobrindo:

- `findByEmail` retorna usuario quando o email existe.
- `findByEmail` retorna vazio quando o email nao existe.
- `existsByEmail` retorna `true` para email persistido.
- `existsByEmail` retorna `false` para email inexistente.
- `existsByEmailIgnoreCase` encontra email independente de caixa.
- `existsByEmailAndIdNot` retorna `true` quando outro usuario ja usa o email.
- `existsByEmailAndIdNot` retorna `false` quando o email pertence ao proprio usuario informado.
- `findByDeletedAtIsNull` retorna apenas usuarios ativos.
- `findByDeletedAtIsNullAndType` retorna apenas usuarios ativos do tipo informado.
- Usuario com `deletedAt` preenchido nao aparece nas listagens de ativos.

### Dados minimos para montar nos testes

- Um usuario `ADMIN` ativo.
- Um usuario `STUDENT` ativo.
- Um usuario `STUDENT` deletado.
- Emails com diferenca de caixa para validar `existsByEmailIgnoreCase`.

### Criterios de aceite

- O teste valida consultas derivadas do Spring Data JPA usando banco real de teste.
- O comportamento de filtro por `deletedAt` esta coberto.
- O comportamento de unicidade/logica de email esta coberto.
- Nao ha mocks para `UserRepository`.

## Issue 3: Criar testes de integracao para `CourseRepository`

### Prioridade

Media-alta.

### Justificativa

`CourseRepository` sustenta criacao, atualizacao, busca e listagem de cursos. Os metodos de existencia e filtros por `deletedAt` sao relevantes para regras de negocio de duplicidade e soft delete.

Repository alvo:

```text
src/main/java/com/portal/conecta/hub/module/course/domain/port/CourseRepository.java
```

### Casos de teste

Implementar `CourseRepositoryIntegrationTest` cobrindo:

- `existsByName` retorna `true` para nome persistido.
- `existsByName` retorna `false` para nome inexistente.
- `existsByCode` retorna `true` para codigo persistido.
- `existsByCode` retorna `false` para codigo inexistente.
- `existsByNameAndIdNot` retorna `true` quando outro curso usa o nome.
- `existsByNameAndIdNot` retorna `false` quando o nome pertence ao proprio curso informado.
- `existsByCodeAndIdNot` retorna `true` quando outro curso usa o codigo.
- `existsByCodeAndIdNot` retorna `false` quando o codigo pertence ao proprio curso informado.
- `findByIdAndDeletedAtIsNull` retorna curso ativo.
- `findByIdAndDeletedAtIsNull` retorna vazio para curso deletado.
- `findAllByDeletedAtIsNull` retorna apenas cursos ativos.

### Dados minimos para montar nos testes

- Dois cursos ativos.
- Um curso deletado.
- Nomes e codigos distintos para validar conflitos.

### Criterios de aceite

- O teste prova os metodos de existencia usados por criacao e atualizacao.
- O teste prova que cursos deletados nao aparecem nas buscas de ativos.
- Nao ha mocks para `CourseRepository`.

## Issue 4: Criar testes de integracao para `RoomRepository`

### Prioridade

Media.

### Justificativa

`RoomRepository` tem superficie menor, mas ainda possui regras importantes de unicidade por numero e filtros por `deletedAt`.

Repository alvo:

```text
src/main/java/com/portal/conecta/hub/module/room/domain/port/RoomRepository.java
```

### Casos de teste

Implementar `RoomRepositoryIntegrationTest` cobrindo:

- `existsByNumber` retorna `true` para numero persistido.
- `existsByNumber` retorna `false` para numero inexistente.
- `findByIdAndDeletedAtIsNull` retorna sala ativa.
- `findByIdAndDeletedAtIsNull` retorna vazio para sala deletada.
- `findAllByDeletedAtIsNull` retorna apenas salas ativas.

### Dados minimos para montar nos testes

- Duas salas ativas.
- Uma sala deletada.
- Numeros distintos para validar existencia.

### Criterios de aceite

- O teste valida unicidade/logica de numero da sala.
- O teste valida filtro de registros ativos por `deletedAt`.
- Nao ha mocks para `RoomRepository`.

## Sugestao de ordem de implementacao

1. `ClassMembershipRepositoryIntegrationTest`
2. `UserRepositoryIntegrationTest`
3. `CourseRepositoryIntegrationTest`
4. `RoomRepositoryIntegrationTest`

Essa ordem prioriza primeiro as queries com maior risco e maior numero de relacoes.

## Fora do escopo desta rodada

Estes itens podem virar uma segunda rodada de issues:

- Testes de integracao de API com `@SpringBootTest` e `MockMvc`.
- Fluxo real de `POST /auth/login` gerando JWT.
- Fluxo autenticado de `GET /me/courses`.
- Fluxo HTTP completo de criacao de curso, turma e membership.
- Testes com Testcontainers e PostgreSQL real.
- Teste de migrations Flyway com `flyway.enabled=true`.

## Observacao sobre nomenclatura

Estes testes devem ser chamados de testes de integracao porque validam componentes reais trabalhando juntos, especialmente repository + JPA + banco de teste. Eles nao devem ser chamados de testes end-to-end, pois nao exercitam o produto inteiro com frontend, rede real e ambiente completo.
