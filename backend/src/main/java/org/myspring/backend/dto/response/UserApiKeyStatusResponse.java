package org.myspring.backend.dto.response;

import org.myspring.backend.model.UserSetting;

public record UserApiKeyStatusResponse(boolean isKeyAvailable) {
    public static UserApiKeyStatusResponse fromUserSetting(UserSetting userSetting) {
        return new UserApiKeyStatusResponse(
                userSetting.getApiKey() != null && !userSetting.getApiKey().isEmpty()
        );
    }
}
