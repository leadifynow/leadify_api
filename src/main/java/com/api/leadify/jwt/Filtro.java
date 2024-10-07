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

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get the origin of the request
        String origin = httpRequest.getHeader("Origin");

        // Log the request origin and other info
        /*log.info("Request received from origin: {}", origin);
        log.info("Request URL: {}", httpRequest.getRequestURL());
        log.info("Request Method: {}", httpRequest.getMethod());
        log.info("Request Path: {}", httpRequest.getServletPath());*/

        // Allow specific origins: localhost and leadifynow.com
        if ("http://localhost:5173".equals(origin) || "https://leadifynow.com".equals(origin)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin); // Set dynamic origin
        } else {
            log.warn("Request from disallowed origin: {}", origin);
        }

        // Set other CORS headers
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

        // Handle preflight requests
        if (httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            log.info("Handled preflight (OPTIONS) request from: {}", origin);
            return;
        }

        String path = httpRequest.getServletPath();
        SessionM sessionM;

        if (path.startsWith("/api/user/login") || path.startsWith("/api/interested/create") || path.startsWith("/api/booked/")) {
            chain.doFilter(request, response);
        } else {
            sessionM = JWT.getSession(httpRequest); // Retrieve session
            if (sessionM.status < 0) {
                log.error("Invalid token for request from origin: {}", origin);
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Incorrect token");
            } else {
                request.setAttribute("idUsuario", sessionM.idUsuario);
                chain.doFilter(request, response);
            }
        }
    }
}
