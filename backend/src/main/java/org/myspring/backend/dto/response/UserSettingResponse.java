package org.myspring.backend.dto.response;

import org.myspring.backend.model.UserSetting;

public record UserSettingResponse(String appTheme) {
    public static UserSettingResponse fromUserSetting(UserSetting userSetting) {
        return new UserSettingResponse(
                userSetting.getAppTheme()
        );
    }
}
