package iu.piisj.eventmanager_sose2026.event;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class EventController implements Serializable {

    @Inject
    private EventService eventService;

    private List<Event> events;

    @PostConstruct
    public void init() {
        events = eventService.getEvents();
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<String> getAvailableStatuses() {
        return eventService.getAvailableStatuses();
    }

    public String save() {
        eventService.saveEvents(events);
        return null;
    }
}
