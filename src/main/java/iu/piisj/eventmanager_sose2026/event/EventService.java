package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.repository.EventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

import static java.util.Collections.emptyList;

@RequestScoped
public class EventService {

    public EventService() {}

    @Inject
    private EventRepository eventRepository;

    public List<Event> getEvents() {
        return emptyList();// return eventRepository.findAll();
    }

    public List<String> getAvailableStatuses() {
        return List.of("Geplant", "Offen", "Abgeschlossen");
    }

    public void addEvent() {
        Event newEvent = new Event("", "", "", "Geplant");
        // eventRepository.save(newEvent);
    }

    public void saveEvent(Event newEvent) {
        eventRepository.save(newEvent);
        FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Erfolg! Die Veranstaltung wurde erfolgreich angelegt.",
                newEvent.toString()
        );

        FacesContext.getCurrentInstance().addMessage(null, message);

    }


}
