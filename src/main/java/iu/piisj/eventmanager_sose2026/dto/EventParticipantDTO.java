package iu.piisj.eventmanager_sose2026.dto;

import java.time.LocalDateTime;

public class EventParticipantDTO {

    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final LocalDateTime registeredAt;

    public EventParticipantDTO(String username, String email, String firstName, String lastName, LocalDateTime registeredAt) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registeredAt = registeredAt;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}
