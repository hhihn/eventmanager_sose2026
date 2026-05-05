package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.auth.AuthController;
import iu.piisj.eventmanager_sose2026.dto.EventDTO;
import iu.piisj.eventmanager_sose2026.registration.EventRegistrationResult;
import iu.piisj.eventmanager_sose2026.registration.EventRegistrationService;
import iu.piisj.eventmanager_sose2026.user.UserRole;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// diese Annotation macht den Bean (Controller) in der View sichtbar. Dies passiert über die beans.xml dessen
// Einstellungen
@Named
@ViewScoped
public class EventController implements Serializable {

    @Inject
    private EventService eventService;

    @Inject
    private EventRegistrationService eventRegistrationService;

    @Inject
    private AuthController authController;

    private List<Event> events;

    private Set<Long> registeredEventIds = new HashSet<>();

    private EventDTO newEvent = new EventDTO();

    @PostConstruct
    public void init() {
        events = eventService.getEvents();
        registeredEventIds = eventRegistrationService.getRegisteredEventIds(authController.getCurrentUser());
    }

    private Event mapDTOToEvent(EventDTO dto) {
        return new Event(
                dto.getName(),
                dto.getLocation(),
                dto.getDate(),
                dto.getState()
        );
    }

    public void saveEvent(){
        Event eventEntity = mapDTOToEvent(newEvent);
        eventService.saveEvent(eventEntity);
        // Formular zurücksetzen, bzw. die EventDTO zurücksetzen
        newEvent = new EventDTO();
    }

    public void registerForEvent(Event event) {
        if (event == null || event.getId() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Anmeldung fehlgeschlagen", "Die Veranstaltung wurde nicht gefunden.");
            return;
        }

        EventRegistrationResult result = eventRegistrationService.registerForEvent(event.getId(), authController.getCurrentUser());

        switch (result) {
            case SUCCESS -> {
                registeredEventIds.add(event.getId());
                addMessage(FacesMessage.SEVERITY_INFO, "Anmeldung erfolgreich", "Du bist fuer die Veranstaltung angemeldet.");
            }
            case ALREADY_REGISTERED -> {
                registeredEventIds.add(event.getId());
                addMessage(FacesMessage.SEVERITY_INFO, "Bereits angemeldet", "Du bist bereits fuer diese Veranstaltung angemeldet.");
            }
            case NOT_PARTICIPANT -> addMessage(FacesMessage.SEVERITY_ERROR, "Anmeldung nicht erlaubt", "Nur Teilnehmer:innen koennen sich fuer Veranstaltungen anmelden.");
            case NOT_LOGGED_IN -> addMessage(FacesMessage.SEVERITY_ERROR, "Anmeldung erforderlich", "Bitte melde dich zuerst an.");
            case EVENT_NOT_FOUND -> addMessage(FacesMessage.SEVERITY_ERROR, "Anmeldung fehlgeschlagen", "Die Veranstaltung wurde nicht gefunden.");
            case USER_NOT_FOUND -> addMessage(FacesMessage.SEVERITY_ERROR, "Anmeldung fehlgeschlagen", "Der Benutzer wurde nicht gefunden.");
        }
    }

    public boolean canRegister(Event event) {
        return isParticipant() && !isRegistered(event);
    }

    public boolean isRegistered(Event event) {
        return event != null && event.getId() != null && registeredEventIds.contains(event.getId());
    }

    public boolean isParticipant() {
        return authController.isLoggedIn()
                && authController.getCurrentUser() != null
                && authController.getCurrentUser().getRole() == UserRole.TEILNEHMER;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public EventDTO getNewEvent() {return newEvent;}

    public List<Event> getEvents() {
        return events;
    }

    public List<String> getAvailableStatuses() {
        return eventService.getAvailableStatuses();
    }
}
