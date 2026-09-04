package org.project.auth.exception;

import lombok.Getter;

@Getter
public class GuestNotFoundException extends RuntimeException {
    public GuestNotFoundException(String message) {
        super(message);
    }
}
