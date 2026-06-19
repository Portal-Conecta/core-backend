package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.model.NotificationScopeType;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRepository;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessNotificationRequestUseCaseTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserNotificationRepository userNotificationRepository;
    @Mock private NotificationRecipientPort recipientPort;
    @Mock private JsonMapper jsonMapper;

    @InjectMocks
    private ProcessNotificationRequestUseCase useCase;

    private ProcessNotificationRequestCommand validCommand;
    private NotificationEntity savedNotification;

    }