package org.project.payload.response;

public record SignoutResponse(
        String AccessCookie,
        String RefreshCookie
)
{}
