package com.portal.conecta.hub.module.user.domain.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserEmailPolicyTest {

    @Mock
    private UserRepository userRepository;

    private UserEmailPolicy userEmailPolicy;

    @BeforeEach
    void setUp() {
        userEmailPolicy = new UserEmailPolicy(userRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "' STUDENT@ESTUDANTE.SESISENAI.ORG.BR ', student@estudante.sesisenai.org.br",
            "employee@weg.net, employee@weg.net"
    })
    void validateForCreationNormalizesAllowedAvailableEmail(String email, String expectedEmail) {
        when(userRepository.existsByEmailIgnoreCase(expectedEmail)).thenReturn(false);

        String normalizedEmail = userEmailPolicy.validateForCreation(email);

        assertEquals(expectedEmail, normalizedEmail);
        verify(userRepository).existsByEmailIgnoreCase(expectedEmail);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void validateForCreationRejectsMissingEmail(String email) {
        assertThrows(InvalidUserDataException.class, () -> userEmailPolicy.validateForCreation(email));

        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "student",
            "student@weg",
            "@weg.net"
    })
    void validateForCreationRejectsInvalidEmailFormat(String email) {
        assertThrows(InvalidUserDataException.class, () -> userEmailPolicy.validateForCreation(email));

        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "student@example.com",
            "teacher@portal.test"
    })
    void validateForCreationRejectsEmailOutsideAllowedDomains(String email) {
        assertThrows(InvalidUserDataException.class, () -> userEmailPolicy.validateForCreation(email));

        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "DUPLICATE@WEG.NET, duplicate@weg.net",
            "DUPLICATE@ESTUDANTE.SESISENAI.ORG.BR, duplicate@estudante.sesisenai.org.br"
    })
    void validateForCreationRejectsDuplicatedEmail(String email, String normalizedEmail) {
        when(userRepository.existsByEmailIgnoreCase(normalizedEmail)).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> userEmailPolicy.validateForCreation(email));

        verify(userRepository).existsByEmailIgnoreCase(normalizedEmail);
    }
}
