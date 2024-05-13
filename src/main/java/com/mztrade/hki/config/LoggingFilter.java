package com.mztrade.hki.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

public class LoggingFilter extends AbstractRequestLoggingFilter {

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        this.logger.info(message);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {

    }
}
