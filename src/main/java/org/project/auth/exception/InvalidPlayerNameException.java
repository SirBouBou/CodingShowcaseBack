package org.project.auth.exception;

import lombok.Getter;

@Getter
public class InvalidPlayerNameException extends RuntimeException{
    public InvalidPlayerNameException(String message) {
        super(message);
    }
}
