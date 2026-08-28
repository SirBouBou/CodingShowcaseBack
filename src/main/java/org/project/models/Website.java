package org.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="websites")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Website {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 20)
    private String name;

    @Size(max = 100)
    private String description;

    public Website(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
