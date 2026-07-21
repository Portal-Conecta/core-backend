# Configuração de e-mail e validação da ativação de conta

Este documento cobre a issue #229: como configurar o cliente de e-mail do
Core (SMTP fake/local e SMTP real de homologação) e como validar
manualmente o fluxo de ativação de conta de ponta a ponta.

## Como funciona

1. `POST /users` cria o usuário **inativo** e gera um token de ativação de
   uso único (válido por 24h), publicando um evento de criação de usuário.
2. Após o **commit** da transação, o Core tenta enviar o e-mail de ativação.
3. Sem `JavaMailSender` configurado (ou seja, sem `SPRING_MAIL_HOST`
   definido), o Core apenas registra um `warning` e segue normalmente — a
   criação do usuário não é afetada.
4. Se o SMTP estiver configurado mas o envio falhar, o erro é capturado e
   também vira um `warning` — o usuário e o token já foram persistidos antes
   do envio, então nada é desfeito.
5. `POST /auth/activate` recebe o `token` (do link recebido por e-mail) e a
   nova senha, ativa a conta e permite login em seguida via
   `POST /auth/login`.

## Variáveis de ambiente

### SMTP (opcional)

| Variável | Descrição |
|---|---|
| `SPRING_MAIL_HOST` | Host do servidor SMTP |
| `SPRING_MAIL_PORT` | Porta do servidor SMTP |
| `SPRING_MAIL_USERNAME` | Usuário de autenticação SMTP |
| `SPRING_MAIL_PASSWORD` | Senha/token de autenticação SMTP |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` | `true`/`false` — exige autenticação |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | `true`/`false` — habilita STARTTLS |

Essas variáveis são propriedades padrão do Spring Boot (`spring.mail.*`) e
já são reconhecidas automaticamente, sem precisar de configuração extra no
`application.yaml`. Se `SPRING_MAIL_HOST` não estiver definida, o Spring não
cria o cliente de e-mail, e é isso que faz o Core funcionar normalmente sem
SMTP configurado.

### Fluxo de ativação

| Variável | Padrão | Descrição |
|---|---|---|
| `ACCOUNT_ACTIVATION_BASE_URL` | `http://localhost:3002/ativar-conta` | URL base do front-end para onde o link de ativação aponta. O token é anexado como `?token=...` |
| `ACCOUNT_ACTIVATION_MAIL_FROM` | `no-reply@portal-conecta.local` | Remetente usado no e-mail de ativação |

## Configurando SMTP local/fake

### Opção A — MailHog

```bash
docker run -d --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

No seu `.env`:

```
SPRING_MAIL_HOST=localhost
SPRING_MAIL_PORT=1025
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=false
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=false
```

UI para conferir e-mails recebidos: http://localhost:8025

### Opção B — Mailpit

```bash
docker run -d --name mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit
```

Mesmas variáveis de ambiente da opção A. UI: http://localhost:8025

### Opção C — GreenMail (usado nos testes automatizados)

Não requer container: é uma dependência de teste (`greenmail-junit5`) que
sobe um servidor SMTP em memória durante a execução dos testes. Veja
`AccountActivationIntegrationTest` para o exemplo completo — é lá que o
roteiro abaixo é validado automaticamente a cada execução dos testes.

## Configurando SMTP real de homologação

Exemplo com Gmail (senha de app gerada em
`myaccount.google.com/apppasswords`):

```
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=seu-email@gmail.com
SPRING_MAIL_PASSWORD=<senha-de-app>
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
```

Qualquer provedor SMTP compatível (SES, SendGrid SMTP, etc.) funciona da
mesma forma — basta ajustar host/porta/credenciais.

## Roteiro de validação manual (ponta a ponta)

1. Suba um SMTP fake local (MailHog ou Mailpit, ver acima).
2. Configure o `.env` com as variáveis `SPRING_MAIL_*`,
   `ACCOUNT_ACTIVATION_BASE_URL` e `ACCOUNT_ACTIVATION_MAIL_FROM`.
3. Suba o Core: `docker compose up -d` (ou `mvn spring-boot:run` com as
   mesmas variáveis exportadas no shell).
4. Autentique-se com um usuário `ADMIN`/`SENAI`/`WEG` existente via
   `POST /auth/login` e use o `accessToken` retornado nos passos seguintes.
5. Crie um usuário:
   ```
   POST /users
   Authorization: Bearer <accessToken>
   {
     "name": "Novo Usuário",
     "email": "novo.usuario@estudante.sesisenai.org.br",
     "typeUser": "STUDENT"
   }
   ```
6. Confira que o e-mail chegou no SMTP fake (UI do MailHog/Mailpit em
   http://localhost:8025).
7. Copie o valor do parâmetro `token` do link recebido no e-mail.
8. Ative a conta:
   ```
   POST /auth/activate
   {
     "token": "<token-copiado>",
     "newPassword": "novaSenha123"
   }
   ```
9. Faça login com a nova senha:
   ```
   POST /auth/login
   {
     "email": "novo.usuario@estudante.sesisenai.org.br",
     "password": "novaSenha123"
   }
   ```
10. Um `200 OK` com `accessToken`/`refreshToken` confirma que o fluxo
    completo funcionou.

## Testes automatizados

| Classe | Cobertura |
|---|---|
| `AccountActivationEmailServiceTest` | Montagem do e-mail (remetente, destinatário, assunto, corpo HTML, link com base URL + token); ausência de `JavaMailSender` não lança exceção; falha de envio é propagada |
| `UserCreatedEventListenerTest` | Listener chama o serviço de e-mail com os dados do evento; falha no envio é capturada e não quebra o fluxo pós-commit |
| `ApplicationEventAccountActivationNotificationAdapterTest` | Publica `UserCreatedEvent` com usuário, token bruto e expiração |
| `AccountActivationIntegrationTest` | Fluxo integrado com SMTP fake (GreenMail): criação de usuário → e-mail recebido → ativação com token → login |
