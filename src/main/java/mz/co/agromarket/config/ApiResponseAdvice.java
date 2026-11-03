package mz.co.agromarket.config;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import mz.co.agromarket.dto.response.ApiResponse;
import mz.co.agromarket.dto.response.ApiErrorResponse;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body == null || body instanceof ApiResponse<?> || body instanceof ApiErrorResponse || body instanceof Resource
                || isNoContent(response) || isOpenApiResource(request)) {
            return body;
        }
        return ApiResponse.sucesso(body);
    }

    private boolean isNoContent(ServerHttpResponse response) {
        return response instanceof ServletServerHttpResponse servletResponse
                && servletResponse.getServletResponse().getStatus() == HttpStatus.NO_CONTENT.value();
    }

    private boolean isOpenApiResource(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")
                || path.startsWith("/webjars/") || path.startsWith("/actuator/");
    }
}
