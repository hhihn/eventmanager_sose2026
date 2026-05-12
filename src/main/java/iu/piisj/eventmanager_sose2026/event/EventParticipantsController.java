package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.auth.AuthController;
import iu.piisj.eventmanager_sose2026.dto.EventParticipantDTO;
import iu.piisj.eventmanager_sose2026.registration.EventRegistrationService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class EventParticipantsController implements Serializable {

    @Inject
    private AuthController authController;

    @Inject
    private EventService eventService;

    @Inject
    private EventRegistrationService eventRegistrationService;

    private Long eventId;

    private Event event;

    private List<EventParticipantDTO> participants = List.of();

    public String load() {
        if (!authController.isOrganizer()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nicht erlaubt", "Nur Organisator:innen koennen Anmeldungen einsehen.");
            return "/events.xhtml?faces-redirect=true";
        }

        if (eventId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Veranstaltung fehlt", "Es wurde keine Veranstaltung ausgewaehlt.");
            return "/events.xhtml?faces-redirect=true";
        }

        event = eventService.getEventById(eventId);
        if (event == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Veranstaltung nicht gefunden", "Die Veranstaltung wurde nicht gefunden.");
            return "/events.xhtml?faces-redirect=true";
        }

        participants = eventRegistrationService.getParticipantsForEvent(eventId, authController.getCurrentUser());
        return null;
    }

    public boolean hasParticipants() {
        return !participants.isEmpty();
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Event getEvent() {
        return event;
    }

    public List<EventParticipantDTO> getParticipants() {
        return participants;
    }
}
