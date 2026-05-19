# Guia de Contribuição - Portal Conecta

Este documento define as regras de contribuição que todos os membros devem seguir, independente do squad.

Em caso de dúvida, consulte a Scrum Master antes de realizar qualquer ação.

---

## Regras Gerais

- **Nunca** faça commit direto nas branches **main** ou **dev**;
- Todo código entra via Pull Request, nunca diretamente;
- PRs precisam de pelo menos 1 aprovação antes do merge.

---

## Git Flow - Estrutura das Branches

```
main        → código estável, versão de entrega
dev         → integração contínua, base para features
feature/    → desenvolvimento de funcionalidades
hotfix/     → correções urgentes em produção
release/    → preparação para entrega de sprint
```

### Fluxo padrão de trabalho

```
dev → feature/sua-branch → (PR aprovado) → dev → (fim de sprint) → main
```

---

## Nomenclatura de Branches

Use o padrão abaixo, sempre em letras minúsculas com hífen:

```
feature/42-autenticacao-email
feature/11-mapa-sala-aprendiz
hotfix/correcao-validacao-login
release/sprint-01
```

**Formato:** tipo/codigo-descricao-curta

| Tipo | Quando usar |
| --- | --- |
| feature/ | Nova funcionalidade |
| hotfix/ | Correção urgente em produção |
| release/ | Preparação de entrega |

---

## Padrão de Commits

Vamos seguir o padrão Conventional Commits:

```
tipo: descrição curta 
```

### Tipos permitidos

| Tipo | Quando usar |
| --- | --- |
| feat | Nova funcionalidade |
| fix | Correção de bug |
| docs | Documentação |
| refactor | Refatoração sem mudança de comportamento |
| style | Formatação, espaços, vírgulas (sem lógica) |
| test | Adição ou correção de testes |
| chore | Tarefas de configuração, dependências |

### Exemplos corretos

```
feat: adiciona filtro de comunicados por turma
fix: corrige bloqueio de login após 3 tentativas
docs: atualiza README com instruções de setup
refactor: extrai lógica de roles para service separado
```

### Exemplos incorretos (Evitem, por favor)

```
ajustes
corrigindo bug
wip
commit final
```

---

## Como Abrir um Pull Request

Antes de abrir um PR, atualize sua branch com a `dev` e garanta que o projeto continua compilando e passando nos testes.

```bash
git checkout dev
git pull origin dev
git checkout feature/sua-branch
git rebase dev
./mvnw test
git push -u origin feature/sua-branch
```

No Windows, use:

```bash
.\mvnw.cmd test
```

Depois disso:

1. Abra o PR da sua branch para a `dev`;
2. Use um título no padrão Conventional Commits, por exemplo: `feat: adiciona cadastro de turmas`;
3. Relacione a issue correspondente, por exemplo: `Closes #42`;
4. Aplique as labels corretas (squad, tipo e prioridade);
5. Solicite revisão do tech lead do seu squad;
6. Aguarde pelo menos 1 aprovação antes do merge.

Use `rebase` para deixar a branch atualizada com a `dev` sem criar commits extras de merge. Isso deixa o histórico mais limpo e facilita o code review, porque o PR mostra apenas os commits da sua mudança.

Se a branch já existe no remoto e você fez `rebase`, o Git pode recusar o `push` normal porque o histórico local foi reorganizado. Nesse caso, use `git push --force-with-lease origin feature/sua-branch` para atualizar a branch remota com segurança.

> Atenção: faça rebase apenas na sua própria branch de trabalho. Se outras pessoas também estão usando a mesma branch, alinhe com o squad antes. Nunca use `git push --force` simples, porque ele pode sobrescrever mudanças remotas de outra pessoa.

---

## Estrutura Esperada de um Pull Request

Todo PR deve ser pequeno, claro e revisável. Um bom PR resolve uma issue ou uma parte bem definida dela.

### Título

Use o mesmo padrão dos commits:

```text
feat: adiciona endpoint de cadastro de salas
fix: corrige validação de login
refactor: reorganiza service de usuários
```

### Descrição

Use a estrutura do arquivo `.github/pull_request_template.md` ao preencher a descrição do PR.

Quando o PR for aberto no GitHub, esse modelo será carregado automaticamente para facilitar o preenchimento.

Modelo usado no PR:

```md
## Objetivo

- Explique por que este PR existe e qual problema ele resolve.
- Mantenha o escopo ligado a uma issue ou a uma entrega bem definida.

## Mudanças principais

- Liste as mudanças mais importantes.
- Cite classes, endpoints, entidades ou regras de negócio afetadas quando fizer sentido.
- Informe se algo ficou fora do escopo e será tratado em outro PR.

## Como revisar

- Indique por onde o reviewer deve começar quando o PR alterar muitos arquivos.
- Informe se você espera uma revisão rápida, revisão de arquitetura, revisão de segurança ou revisão de regra de negócio.

## Como testar

- Liste os passos para validar a mudança.
- Informe comandos executados, como `.\mvnw.cmd test` ou `./mvnw test`.
- Inclua exemplos de requisição/resposta quando houver endpoint novo ou alterado.

## Contrato de API

- Informe método, rota, payload, resposta e códigos de erro quando houver endpoint novo ou alterado.
- Informe se o contrato afeta frontend ou outros serviços.
- Informe se a documentação Swagger/OpenAPI precisa ser atualizada.

## Impacto no backend

- Marque ou descreva as áreas afetadas: controller, service, repository, domain/model, segurança, configuração, banco de dados.
- Informe se houve alteração em autenticação, autorização, validação, transação, relacionamento JPA ou enum.
- Informe se há risco de quebrar compatibilidade com dados, integrações ou comportamento existente.

## Segurança e dados sensíveis

- Informe se o PR altera permissões, JWT, roles, dados pessoais, secrets, logs ou dependências.
- Confirme que nenhuma credencial, token, `.env` ou dado sensível foi versionado.

## Riscos e rollback

- Descreva riscos conhecidos, limitações ou pontos que merecem atenção.
- Informe como reverter ou desabilitar a mudança caso algo falhe.

## Checklist

- [ ] Minha branch está atualizada com a `dev`
- [ ] Minha branch foi enviada para o remoto após o rebase com `git push --force-with-lease`, quando necessário
- [ ] O código compila sem erros
- [ ] Rodei `.\mvnw.cmd test` ou `./mvnw test`
- [ ] Não subi arquivos desnecessários (`.env`, `target/`, arquivos locais da IDE)
- [ ] Endpoints novos ou alterados possuem contrato/payload descrito neste PR
- [ ] Regras de permissão foram validadas no backend
- [ ] Adicionei ou atualizei testes quando a mudança envolve regra de negócio
- [ ] Revisei meu próprio diff antes de solicitar review
- [ ] Expliquei riscos, impactos ou decisões técnicas não óbvias

## Issue relacionada

Closes #
```

### Tamanho e escopo

- Evite PRs grandes demais. Se a mudança envolver muitas responsabilidades, divida em PRs menores;
- Não misture feature, refatoração e formatação no mesmo PR sem necessidade;
- Não altere arquivos fora do escopo da issue;
- Não envie código comentado, testes quebrados ou mudanças temporárias;
- Explique decisões importantes quando a solução não for óbvia.

---

## Critérios para um PR ser aceito

Um PR só deve ser aprovado quando atender aos critérios abaixo:

- Resolve o problema descrito na issue;
- Mantém o escopo pequeno e coerente;
- Compila e passa nos testes com Maven;
- Segue os padrões do projeto Spring Boot;
- Mantém responsabilidades separadas entre controller, service, repository e domain/model;
- Valida regras de negócio e permissões no backend, mesmo que o frontend também valide;
- Não expõe dados sensíveis, secrets, tokens ou arquivos de ambiente;
- Atualiza ou cria testes quando houver regra de negócio, bug fix ou comportamento relevante;
- Atualiza documentação ou contrato de API quando endpoints, payloads, respostas ou erros mudarem.

---

## Boas Práticas para Facilitar o Code Review

- Abra PRs cedo, mas só peça revisão quando estiver pronto para análise;
- Deixe comentários no PR quando quiser chamar atenção para uma decisão técnica;
- Responda os comentários de review com clareza e marque como resolvido apenas depois de ajustar ou justificar;
- Prefira commits com mensagens claras em vez de `wip`, `ajustes` ou `final`;
- Se o PR alterar contrato de API, descreva o impacto para o frontend e para outros serviços;
- Se o PR alterar autenticação ou autorização, explique quais perfis podem acessar a funcionalidade;
- Se adicionar endpoint, inclua na descrição o método, rota, payload esperado e principais respostas;
- Se alterar entidade JPA, explique se muda relacionamento, obrigatoriedade, enum, tabela ou coluna.

---

## Checklist antes de abrir PR (Façam a verificação)

- [ ]  Minha branch está atualizada com a dev
- [ ]  O código compila sem erros
- [ ]  Rodei `.\mvnw.cmd test` ou `./mvnw test`
- [ ]  Não subi arquivos desnecessários (`.env`, `target/`, arquivos locais da IDE)
- [ ]  Os commits seguem o padrão Conventional Commits
- [ ]  O PR tem título, descrição, passos de teste e issue relacionada
- [ ]  Endpoints novos ou alterados possuem contrato/payload descrito no PR
- [ ]  Regras de permissão foram validadas no backend
- [ ]  Adicionei ou atualizei testes quando a mudança envolve regra de negócio
- [ ]  Apliquei as labels corretas na issue e no PR
- [ ]  A issue correspondente está como "Em revisão" no board

---

## Labels

Sempre aplique as labels corretas nas issues e PRs:

| Label | Significado |
| --- | --- |
| priority: high | Prioridade alta  |
| priority: medium | Prioridade média |
| priority: low | Prioridade baixa |
| bug | Algo não está funcionando |
| enhancement | Melhoria ou nova funcionalidade |
| blocked | Issue travada por dependência externa |
| squad: * | Squad responsável pela issue |

---

## Dúvidas?

Tech Lead Backend 78: Lucas
Scrum Master 78: Victória
Scrum Master 77: Melissa

No caso de dúvidas, sintam-se a vontade para perguntar!!
