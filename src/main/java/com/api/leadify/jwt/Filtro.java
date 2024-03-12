package com.api.leadify.jwt;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.api.leadify.entity.SessionM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
public class Filtro implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        log.info("ENTRY::-> doFilter");

        ((HttpServletResponse) response).addHeader("Access-Control-Allow-Origin", "*");
        ((HttpServletResponse) response).addHeader("Access-Control-Allow-Methods","GET, OPTIONS, HEAD, PUT, POST");
        ((HttpServletResponse) response).setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
        ((HttpServletResponse) response).setHeader("Access-Control-Allow-Headers", "*");
        ((HttpServletResponse) response).setHeader("Access-Control-Allow-Credentials", "true");

        String metodo = ((HttpServletRequest ) request ).getMethod();
        log.info("Metodo: {}", metodo);
        if (metodo.equalsIgnoreCase("OPTIONS")) {
            log.info("It's an OPTIONS request, filter does not apply here");
            chain.doFilter(request, response);
            return;
        }
        String path = ((HttpServletRequest ) request).getServletPath();
        SessionM sessionM;
        log.info("Path: {}", path);
        log.info("Request Context Path: {}", ((HttpServletRequest ) request).getContextPath());
        log.info("Request Path Translated: {}", ((HttpServletRequest ) request).getPathTranslated());
        log.info("Request Servlet Path: {}", ((HttpServletRequest ) request).getServletPath());
        if (path.startsWith("/api/user/login") || path.startsWith("/api/interested/create") || path.startsWith("/api/booked/")) {
            log.info("Filter does not apply here");
            chain.doFilter(request, response);
        } else {
            log.info("Validating SESSION");
            sessionM = JWT.getSession((jakarta.servlet.http.HttpServletRequest) request); // Casting to Jakarta EE HttpServletRequest
            log.info("Session token: {}", sessionM.token);
            log.info("Session status: {}", sessionM.status);
            if (sessionM.status < 0) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Incorrect token");
            } else {
                request.setAttribute("idUsuario", sessionM.idUsuario);
                chain.doFilter(request, response);
            }
        }
    }
}
