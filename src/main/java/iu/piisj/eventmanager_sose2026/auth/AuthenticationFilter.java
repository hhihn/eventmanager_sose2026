package iu.piisj.eventmanager_sose2026.auth;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@WebFilter("*.xhtml")
public class AuthenticationFilter implements Filter {

    private static final Set<String> PUBLIC_PAGES = Set.of(
            "/index.xhtml",
            "/login.xhtml",
            "/register.xhtml"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String requestUri = httpRequest.getRequestURI();
        String path = requestUri.substring(contextPath.length());

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        Object authUser = session != null ? session.getAttribute(AuthController.SESSION_USER_KEY) : null;
        if (authUser != null) {
            chain.doFilter(request, response);
            return;
        }

        String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
        httpResponse.sendRedirect(contextPath + "/login.xhtml?redirect=" + encodedPath);
    }

    private boolean isPublic(String path) {
        if (PUBLIC_PAGES.contains(path)) {
            return true;
        }
        return path.startsWith("/jakarta.faces.resource/");
    }
}
