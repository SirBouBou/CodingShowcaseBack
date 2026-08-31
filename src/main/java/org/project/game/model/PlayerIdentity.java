package org.project.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class PlayerIdentity {
    private final PlayerId id;
    private String displayName;
}
