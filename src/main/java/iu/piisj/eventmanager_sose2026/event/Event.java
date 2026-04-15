package iu.piisj.eventmanager_sose2026.event;

import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class Event {

    // Primärschlüssel: einmalige Identifikationsschlüssel für die Zeilen dieser Tabelle
    // fungiert später auch als Fremdschlüssel in anderen Tabellen
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String date;
    private String state;

    protected Event(){
        // benötigt für die JPA
    }

    public Event(String name, String location, String date, String state) {
        this.name = name;
        this.location = location;
        this.date = date;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
