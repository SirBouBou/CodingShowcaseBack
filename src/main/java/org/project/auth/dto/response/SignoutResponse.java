package org.project.auth.dto.response;

public record SignoutResponse(
        String AccessCookie,
        String RefreshCookie
)
{}
