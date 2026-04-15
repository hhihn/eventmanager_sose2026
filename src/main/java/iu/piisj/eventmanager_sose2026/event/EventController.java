package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.dto.EventDTO;
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

    private EventDTO newEvent = new EventDTO();

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

    private Event mapToEntity(EventDTO dto) {
        return new Event(
                dto.getName(),
                dto.getLocation(),
                dto.getDate(),
                dto.getState()
        );
    }

    public void saveEvent() {
        Event eventEntity = mapToEntity(newEvent);
        eventService.saveEvent(eventEntity);
        // Formular zurücksetzen
        newEvent = new EventDTO();
    }

    public EventDTO getNewEvent() {
        return newEvent;
    }

}
