package iu.piisj.eventmanager_sose2026.auth;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class RegistrationController {

    @Inject
    private AuthService authService;

    @Inject
    private AuthController authController;

    // Alternative zum DTO: attribute direkt im Controller verwalten
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String confirmPassword;

    public String submitRegistration() {
        if (!password.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registrierung fehlgeschlagen", "Passwort und Passwort-Bestaetigung stimmen nicht ueberein.")
            );
            return null;
        }

        RegistrationResult regResult = authService.register(username, email, firstName, lastName, password);

        if (regResult == RegistrationResult.USERNAME_EXISTS){
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registrierung fehlgeschlagen", "Username ist bereits vergeben.")
            );
            return null;
        }

        if (regResult == RegistrationResult.EMAIL_EXISTS){
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registrierung fehlgeschlagen", "Email ist bereits vergeben.")
            );
            return null;
        }

        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Registrierung erfolgreich", "Benutzerkonto wurde angelegt.")
        );

        return "/events.xhtml?faces-redirect=true";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
