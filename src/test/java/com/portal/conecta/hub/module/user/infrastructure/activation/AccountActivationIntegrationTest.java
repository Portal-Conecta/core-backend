package com.portal.conecta.hub.module.user.infrastructure.activation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false"
})
class AccountActivationIntegrationTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("token=([\\w-]+)");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    private String adminEmail;
    private static final String ADMIN_PASSWORD = "senha-admin-123";

    @BeforeEach
    void setUp() {
        adminEmail = "admin.seed+" + UUID.randomUUID() + "@portal-conecta.local";
        UserEntity admin = UserEntity.create(
                "Admin Seed", adminEmail, ADMIN_PASSWORD, TypeUser.ADMIN, null, passwordEncoder
        );
        userRepository.save(admin);
    }

    private String login(String email, String password) throws Exception {
        String loginBody = """
            {"email":"%s","password":"%s"}
            """.formatted(email, password);

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String extractToken(String emailBody) {
        Matcher matcher = TOKEN_PATTERN.matcher(emailBody);
        assertThat(matcher.find()).as("link de ativacao deve conter o parametro token").isTrue();
        return matcher.group(1);
    }

    @Test
    void deveCriarEntregarAtivarELogar() throws Exception {
        String adminAccessToken = login(adminEmail, ADMIN_PASSWORD);

        String studentEmail = "estudante." + UUID.randomUUID() + "@estudante.sesisenai.org.br";
        String createBody = """
            {"name":"Estudante Teste","email":"%s","typeUser":"STUDENT"}
            """.formatted(studentEmail);

        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        await().atMost(Duration.ofSeconds(5)).until(() -> greenMail.getReceivedMessages().length == 1);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages).hasSize(1);

        MimeMessage activationEmail = receivedMessages[0];
        assertThat(activationEmail.getAllRecipients()[0].toString()).isEqualTo(studentEmail);
        assertThat(activationEmail.getSubject()).isEqualTo("Ative sua conta no Portal Conecta");

        String body = GreenMailUtil.getBody(activationEmail);
        String rawToken = extractToken(body);

        String activateBody = """
            {"token":"%s","newPassword":"novaSenha123"}
            """.formatted(rawToken);

        mockMvc.perform(post("/auth/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activateBody))
                .andExpect(status().isNoContent());

        login(studentEmail, "novaSenha123");
    }

}