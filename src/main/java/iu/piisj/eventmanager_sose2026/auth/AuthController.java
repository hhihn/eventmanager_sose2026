package iu.piisj.eventmanager_sose2026.auth;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Optional;

@Named
@SessionScoped
public class AuthController implements Serializable {

    public static final String SESSION_USER_KEY = "AUTH_USER";

    @Inject
    private AuthService authService;

    private SessionUser currentUser;

    public boolean login(String userName, String password) {
        Optional<SessionUser> sessionUser = authService.authenticate(userName, password);
        if (sessionUser.isEmpty()){
            return false;
        }

        currentUser = sessionUser.get();
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(SESSION_USER_KEY, currentUser);
        return true;
    }

    public String logout() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.invalidateSession();
        currentUser = null;
        return "/login.xhtml?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public SessionUser getCurrentUser() {
        return currentUser;
    }


}
