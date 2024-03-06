package org.project.payload.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.project.models.Profile;

import java.net.Inet4Address;
import java.util.List;

@Data
public class UserInfoResponse {

    private Long id;
    private String username;
    private String email;
    @Setter(AccessLevel.NONE)
    private List<String> roles;

    private Profile profile;

    public UserInfoResponse(Long id, String username, String email, List<String> roles, Profile profile) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.profile = profile;
    }
}
