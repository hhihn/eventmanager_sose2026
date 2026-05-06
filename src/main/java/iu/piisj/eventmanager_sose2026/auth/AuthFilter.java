package iu.piisj.eventmanager_sose2026.auth;

import iu.piisj.eventmanager_sose2026.user.User;
import iu.piisj.eventmanager_sose2026.user.UserRole;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@WebFilter("*.xhtml")
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_PAGES = Set.of(
            "/index.xhtml",
            "/login.xhtml",
            "/register.xhtml"
    );

    private static final Set<String> ORGANIZER_PAGES = Set.of("/create-event.xhtml");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        // cast der allgemeinen requests zu http request und response types
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        // holt sich aus der gesamten URL den path nach der top level domain (z.b. google.com/index.xhtml wird dann zu
        // index.html
        String contextPath = httpRequest.getContextPath();
        String requestUri = httpRequest.getRequestURI();
        String path = requestUri.substring(contextPath.length());

        // handelt es sich um einen öffentlichen pfad?
        if (isPublic(path)){
            // falls ja, erlaube den Aufruf
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        // wenn es kein öffentlicher pfad ist, dann muss die rolle des eingeloggten Users überprüft werden
        // hole die session
        HttpSession session = httpRequest.getSession(false);
        // hole den eingeloggten user
        SessionUser authUser = session != null ? (SessionUser) session.getAttribute(AuthController.SESSION_USER_KEY) : null;
        if (authUser != null){
            UserRole loggedUserRole = authUser.getRole();
            if (!((isOrganizerOnly(path) && (loggedUserRole == UserRole.ORGANISATOR || loggedUserRole == UserRole.ADMIN)))){
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            // wenn es einen eingeloggten user gibt, dann erlaubte den Aufruf
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        // wenn wir hier ankommen, dann haben wir keinen öffentlichen pfad angefragt und sind auch nicht eingeloggt
        // d.h. wir wollen hier auf dei login seite umleiten
        String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
        httpResponse.sendRedirect(contextPath + "/login.xhtml?redirect=" + encodedPath);

    }

    private boolean isPublic(String path) {
        // ist der angeforderte path öffentlich?
        if (PUBLIC_PAGES.contains(path)) {
            return true;
        }
        // oder handelt es sich um eine angefragte JSF resource?
        return path.startsWith("/jakarta.faces.resource/");
    }

    private boolean isOrganizerOnly(String path) {
        return ORGANIZER_PAGES.contains(path);
    }


}
