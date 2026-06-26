package com.portal.conecta.hub.module.me.application.use_case;

import com.portal.conecta.hub.module.me.presentation.controller.dto.MyProfileResponse;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMeUseCase {

    private final RequestContextProvider requestContextProvider;
    private final UserRepository userRepository;

    public MyProfileResponse execute() {
        RequestContext context = requestContextProvider.getRequestContext();

        return userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(context.userId())
                .map(MyProfileResponse::from)
                .orElseThrow(UserNotFoundException::new);
    }
}