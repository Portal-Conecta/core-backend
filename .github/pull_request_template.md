# Pull Request

## Objetivo

- Explique por que este PR existe e qual problema ele resolve.
- O PR deve possuir um único objetivo claro.
- Relacione a mudança com uma issue, tarefa ou entrega definida.

---

## Mudanças principais

- Liste as mudanças mais importantes.
- Cite classes, endpoints, entidades ou regras de negócio afetadas quando fizer sentido.
- Informe o que ficou fora do escopo e será tratado em outro PR.

---

## Como revisar

- Ordem sugerida de revisão:
    1.
    2.
    3.

- Pontos que merecem mais atenção:
- Tipo de revisão esperada:
    - [ ] Regra de negócio
    - [ ] Arquitetura
    - [ ] Segurança
    - [ ] Performance
    - [ ] Revisão rápida

---

## Como testar

- Liste os passos para validar a mudança.
- Informe comandos executados:
    - `.\mvnw.cmd test`
    - `./mvnw test`
- Inclua exemplos de requisição/resposta quando houver endpoint novo ou alterado.

---

## Contrato de API (se aplicável)

- Informe método, rota, payload, resposta e códigos de erro.
- Informe se o contrato impacta frontend ou outros serviços.
- Informe se a documentação Swagger/OpenAPI precisa ser atualizada.

---

## Impacto no backend

- [ ] Controller/API
- [ ] Service/regra de negócio
- [ ] Repository/JPA
- [ ] Banco de dados/migration
- [ ] Segurança/autorização
- [ ] Configuração
- [ ] Mensageria/eventos
- [ ] Cache

### Detalhes adicionais

- Informe alterações em autenticação, autorização, validação, transação, relacionamento JPA ou enums.
- Informe riscos de quebra de compatibilidade com dados, integrações ou comportamento existente.

---

## Migração e compatibilidade

- Existe migration? Qual?
- Existe mudança incompatível?
- Requer atualização de variável de ambiente?
- Requer ajuste em outro serviço?

---

## Segurança e dados sensíveis

- Informe alterações relacionadas a:
    - permissões
    - JWT
    - roles
    - dados pessoais
    - logs
    - dependências
    - secrets

- Confirme que nenhuma credencial, token, `.env` ou dado sensível foi versionado.

---

## Riscos e rollback

- Descreva riscos conhecidos, limitações ou pontos de atenção.
- Informe como reverter ou desabilitar a mudança caso algo falhe.

---

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

---

## Issue relacionada

Closes #
