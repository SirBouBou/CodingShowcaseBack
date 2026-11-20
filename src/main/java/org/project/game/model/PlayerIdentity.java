package org.project.game.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PlayerIdentity {
    @EqualsAndHashCode.Include
    private final PlayerId id;
    private String displayName;
}
