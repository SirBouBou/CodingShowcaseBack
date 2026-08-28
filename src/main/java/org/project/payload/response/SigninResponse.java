package org.project.payload.response;

public record SigninResponse (
        String accessCookie,
        String refreshCookie,
        UserInfoResponse userInfoResponse
) {}
