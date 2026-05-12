package iu.piisj.eventmanager_sose2026.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "event_sessions")
public class EventSession implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(nullable = false, length = 128)
    private String speaker;

    @Column(name = "session_type", nullable = false, length = 50)
    private String sessionType;

    @Column(length = 80)
    private String room;

    @Column(name = "start_time", nullable = false, length = 30)
    private String startTime;

    @Column(name = "end_time", length = 30)
    private String endTime;

    @Column(length = 1000)
    private String description;

    protected EventSession() {
    }

    public EventSession(String title, String speaker, String sessionType, String room, String startTime, String endTime, String description) {
        this.title = title;
        this.speaker = speaker;
        this.sessionType = sessionType;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getTitle() {
        return title;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getSessionType() {
        return sessionType;
    }

    public String getRoom() {
        return room;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDescription() {
        return description;
    }
}
