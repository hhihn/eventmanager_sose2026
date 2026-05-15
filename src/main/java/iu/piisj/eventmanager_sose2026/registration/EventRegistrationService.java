package iu.piisj.eventmanager_sose2026.registration;

import iu.piisj.eventmanager_sose2026.auth.SessionUser;
import iu.piisj.eventmanager_sose2026.dto.EventParticipantDTO;
import iu.piisj.eventmanager_sose2026.repository.EventRegistrationRepository;
import iu.piisj.eventmanager_sose2026.user.UserRole;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;

@RequestScoped
public class EventRegistrationService {

    @Inject
    private EventRegistrationRepository eventRegistrationRepository;

    public EventRegistrationResult registerForEvent(Long eventId, SessionUser currentUser){

        if (currentUser == null){
            return EventRegistrationResult.NOT_LOGGED_IN;
        }

        if (currentUser.getRole() != UserRole.TEILNEHMER) {
            return EventRegistrationResult.NOT_PARTICIPANT;
        }

        return eventRegistrationRepository.register(eventId, currentUser.getId());
    }

    public Set<Long> getRegisteredEventIds(SessionUser currentUser){
        if (currentUser == null){
            return Set.of();
        }

        return eventRegistrationRepository.findEventIdsByUserId(currentUser.getId());
    }

    public List<EventParticipantDTO> getParticipantsForEvent(Long eventId) {
        if (eventId == null) {
            return List.of();
        }

        return eventRegistrationRepository.findParticipantsByEventId(eventId);
    }
}
