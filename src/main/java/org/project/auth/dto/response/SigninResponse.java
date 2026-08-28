package org.project.auth.dto.response;

public record SigninResponse (
        String accessCookie,
        String refreshCookie,
        UserInfoResponse userInfoResponse
) {}
