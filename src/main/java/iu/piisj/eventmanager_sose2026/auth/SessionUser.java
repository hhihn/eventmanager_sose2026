package iu.piisj.eventmanager_sose2026.auth;

import iu.piisj.eventmanager_sose2026.user.UserRole;

import java.io.Serializable;

public class SessionUser implements Serializable {

    private final Long id;
    private final String username;
    private final UserRole role;

    public SessionUser(Long id, String username, UserRole role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}
