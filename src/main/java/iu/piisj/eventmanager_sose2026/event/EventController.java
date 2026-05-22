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
    private AuthController authController;

    @Inject
    private EventRegistrationService eventRegistrationService;

    private List<Event> events;

    private EventDTO newEvent = new EventDTO();

    // eager loading der event ids für die der current user angemeldet ist
    private Set<Long> registeredEventIds = new HashSet<>();

    // wird nach dem konstruktor aufgerufen
    @PostConstruct
    public void init() {
        events = eventService.getEvents();
        // lädt für current user alle events aus der datenbank falls er angemeldet ist
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

    public void saveEvent() {

        if (authController.isOrganizerOrAdmin()) {
            Event eventEntity = mapDTOToEvent(newEvent);
            eventService.saveEvent(eventEntity, authController.getCurrentUser());
            // Formular zurücksetzen, bzw. die EventDTO zurücksetzen
            newEvent = new EventDTO();
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Nicht erlaubt",
                    "Nur Organistor:innen oder Admins dürfen Events anlegen");
        }
    }

    public void registerForEvent(Event event) {
        if (event == null || event.getId() == null){
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Anmeldung für Event fehlgeschlagen",
                    "Event konnte nicht gefunden werden.");
        }

        EventRegistrationResult result = eventRegistrationService.registerForEvent(event.getId(),
                authController.getCurrentUser());

        switch (result){
            case SUCCESS -> {
                registeredEventIds.add(event.getId());
                addMessage(FacesMessage.SEVERITY_INFO, "Anmeldung erfolgreich.", "Du bist erfolgreich" +
                        "für das Event angemeldet worden.");
            }
            case ALREADY_REGISTERED -> {
                registeredEventIds.add(event.getId());
                addMessage(FacesMessage.SEVERITY_INFO, "Bereits angemeldet.",
                        "Du bist für das Event bereits angemeldet.");
            }
            case EVENT_NOT_FOUND -> {
                addMessage(FacesMessage.SEVERITY_ERROR,
                    "Anmeldung fehlgeschlagen",
                    "Das Event konnte nicht gefunden werden.");
            }
            case USER_NOT_FOUND -> {
                addMessage(FacesMessage.SEVERITY_ERROR,
                    "Anmeldung fehlgeschlagen",
                    "Der User konnte nicht gefunden werden.");
            }
            case NOT_LOGGED_IN -> {
                addMessage(FacesMessage.SEVERITY_ERROR,
                    "Anmeldung fehlgeschlagen",
                    "Bitte melde dich zuerst an.");
            }
            case NOT_PARTICIPANT -> {
                addMessage(FacesMessage.SEVERITY_ERROR,
                    "Anmeldung fehlgeschlagen",
                    "Nur Teilnehmer:innen können sich für Events registrieren.");
            }
        }
    }

    public boolean canRegister(Event event) {
        return isParticipant() && !isRegistered(event);
    }

    public boolean isRegistered(Event event) {
        return event != null && event.getId() != null && registeredEventIds.contains(event.getId());
    }

    public boolean isParticipant(){
        return authController.isLoggedIn()
                && authController.getCurrentUser() != null
                && authController.getCurrentUser().getRole() == UserRole.TEILNEHMER;
    }

    public boolean isOrganizer() {
        return authController.isLoggedIn()
                && authController.getCurrentUser() != null
                && authController.getCurrentUser().getRole() == UserRole.ORGANISATOR;
    }

    public boolean canEditEvent(Long eventId){

        if(eventId == null){
            return false;
        }

        if (authController.isAdmin()){
            return true;
        }

        Event event = eventService.getEventById(eventId);

        return authController.isOrganizer()
                && event != null
                && event.getOrganizer().getId().equals(authController.getCurrentUser().getId());
    }

    public void addMessage(FacesMessage.Severity severity, String summary, String detail){
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
