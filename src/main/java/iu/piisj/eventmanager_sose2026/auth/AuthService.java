package iu.piisj.eventmanager_sose2026.auth;

import iu.piisj.eventmanager_sose2026.repository.UserRepository;
import iu.piisj.eventmanager_sose2026.user.User;
import iu.piisj.eventmanager_sose2026.user.UserRole;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

@RequestScoped
public class AuthService {

    @Inject
    private UserRepository userRepository;

    public RegistrationResult register(String username,
                                       String email,
                                       String firstName,
                                       String lastName,
                                       String plainPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            return RegistrationResult.USERNAME_EXISTS;
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return RegistrationResult.EMAIL_EXISTS;
        }

        String passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        User user = new User(username, email, firstName, lastName, passwordHash, UserRole.TEILNEHMER);
        userRepository.save(user);
        return RegistrationResult.SUCCESS;
    }

    public Optional<SessionUser> authenticate(String username, String plainPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        if (!BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
            return Optional.empty();
        }

        return Optional.of(new SessionUser(user.getId(), user.getUsername(), user.getRole()));
    }

    public enum RegistrationResult {
        SUCCESS,
        USERNAME_EXISTS,
        EMAIL_EXISTS
    }
}
