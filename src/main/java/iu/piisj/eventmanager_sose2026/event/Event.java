package iu.piisj.eventmanager_sose2026.event;

import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String date;
    private String state;

    protected Event() {
        // JPA
    }

    public Event(String name, String location, String date, String state) {
        this.name = name;
        this.location = location;
        this.date = date;
        this.state = state;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getState() { return state; }

    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setDate(String date) { this.date = date; }
    public void setState(String state) { this.state = state; }
}
