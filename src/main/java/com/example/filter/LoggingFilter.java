package com.example.filter;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;


public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final List<MediaType> VISIBLE_TYPES = Arrays.asList(
            MediaType.valueOf("text/*"),
            MediaType.APPLICATION_FORM_URLENCODED,
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.valueOf("application/*+json"),
            MediaType.valueOf("application/*+xml"),
            MediaType.MULTIPART_FORM_DATA
    );

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {

        if(isAsyncDispatch(httpServletRequest)) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } else {
            doFilterWrapped(wrapRequest(httpServletRequest), wrapResponse(httpServletResponse), filterChain);
        }
    }
    // Метод будет обарачивать наши запросы и ответы
    protected void doFilterWrapped(ContentCachingRequestWrapper contentCachingRequestWrapper,
                                   ContentCachingResponseWrapper contentCachingResponseWrapper,
                                   FilterChain filterChain) throws ServletException, IOException {

        try {
            beforeRequest(contentCachingRequestWrapper, contentCachingResponseWrapper);
            filterChain.doFilter(contentCachingRequestWrapper, contentCachingResponseWrapper);
        } finally {
            afterRequest(contentCachingRequestWrapper, contentCachingResponseWrapper);
        }
    }

    protected void beforeRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if(logger.isInfoEnabled()) {
            logRequestHeader(request, request.getRemoteAddr() + "|>");
        }
    }

    protected void afterRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if(logger.isInfoEnabled()) {
            logRequestBody(request, request.getRemoteAddr() + "|>");
            logResponse(response, request.getRemoteAddr() + "|>");
        }
    }

    private static void logRequestHeader(ContentCachingRequestWrapper request, String prefix) {
        String queryString = request.getQueryString();
        if(queryString == null) {
            logger.info("{} {} {}", prefix, request.getMethod(), request.getRequestURI());
        }else {
            logger.info("{} {} {}?{}", prefix, request.getMethod(), request.getRequestURI(), queryString);
        }
        Collections.list(request.getHeaderNames()).forEach(headerName ->
                Collections.list(request.getHeaders(headerName)).forEach(headerValue ->
                        logger.info("{} {} {}", prefix, headerName, headerValue)));
        logger.info("{}", prefix);
    }

    private static void logRequestBody(ContentCachingRequestWrapper request, String prefix) {
        byte[] content = request.getContentAsByteArray();
        if(content.length > 0) {
            logContent(content, request.getContentType(), request.getCharacterEncoding(), prefix);
        }
    }

    private static void logResponse(ContentCachingResponseWrapper response, String prefix) {
        int status = response.getStatus();
        logger.info("{} {} {}", prefix, status, HttpStatus.valueOf(status).getReasonPhrase());
        response.getHeaderNames()
                .forEach(header -> response.getHeaders(header)
                        .forEach(headerValue -> logger.info("{} {} {}", prefix, header, headerValue)));
        logger.info("{}", prefix);
        byte[] content = response.getContentAsByteArray();
        if(content.length > 0) {
            logContent(content, response.getContentType(), response.getCharacterEncoding(), prefix);
        }
    }

    private static void logContent(byte[] content, String contentType, String contentEncoding, String prefix) {
        MediaType mediaType = MediaType.valueOf(contentType);
        boolean visible = VISIBLE_TYPES.stream().anyMatch(visibleType -> visibleType.includes(mediaType));
        if(visible) {
            try {
                String contentString = new String(content, contentEncoding);
                Stream.of(contentString.split("\r\n|\r"))
                        .forEach(line -> logger.info("{} {}", prefix, line));
            }catch(UnsupportedEncodingException e) {
                logger.info("{}, [{} bytes content]", prefix, content.length);
            }
        }else {
            logger.info("{}, [{} bytes content]", prefix, content.length);
        }
    }

    private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest httpServletRequest) {
        if(httpServletRequest instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) httpServletRequest;
        }else {
            return new ContentCachingRequestWrapper(httpServletRequest);
        }
    }

    private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse httpServletResponse) {
        if(httpServletResponse instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) httpServletResponse;
        }else {
            return new ContentCachingResponseWrapper(httpServletResponse);
        }
    }
}