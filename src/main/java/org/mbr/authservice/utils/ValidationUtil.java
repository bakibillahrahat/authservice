package org.mbr.authservice.utils;

import org.mbr.authservice.model.UserInfoDto;

import java.util.function.UnaryOperator;

public class ValidationUtil {
    public static void ValidateUserAttributes(UserInfoDto userInfoDto) {
        if (userInfoDto.getUsername().length() >= 10) {
            throw new IllegalArgumentException("Username cannot be more than 10 characters long");
        }
        if (userInfoDto.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

    }
}
