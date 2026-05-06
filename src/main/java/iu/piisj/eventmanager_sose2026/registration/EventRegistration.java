package iu.piisj.eventmanager_sose2026.registration;

import iu.piisj.eventmanager_sose2026.event.Event;
import iu.piisj.eventmanager_sose2026.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "event_registrations",
        uniqueConstraints = @UniqueConstraint(
                name="uk_event_registration_event_user",
                columnNames = {"event_id", "user_id"} // stellt sicher, dass es das Paar nur einmal geben darf, d.h.
                // ein user kann sich zum selben event höchstens einmal registrieren
        )
)

public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    public EventRegistration() {
    }

    public EventRegistration(Event event, User user) {
        this.event = event;
        this.user = user;
        this.registeredAt = LocalDateTime.now();
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
