package com.abz.library.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.abz.library.exception.ErrorResponse;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

@Configuration 
@SecurityScheme (
    name = "BearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "The access token via /api/auth/login")
@OpenAPIDefinition (
    info = @Info (
        title = "Library Management System API",
        version = "1.0"),
    security = @SecurityRequirement (name = "BearerAuth")
)
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer standardResponsesCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            ModelConverters.getInstance()
                .readAll(new AnnotatedType(ErrorResponse.class))
                .forEach((name, schema) -> openApi.getComponents().addSchemas(name, schema));

            ApiResponse clientError = errorResponse(
                "Client error: 400 malformed request or validation failed, "
              + "401 unauthorized, 403 forbidden, 404 not found, 409 conflict");
            ApiResponse serverError = errorResponse("Unexpected server error");

            openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    ApiResponses responses = operation.getResponses();
                    if (responses == null) return;
                    responses.putIfAbsent("4XX", clientError);
                    responses.putIfAbsent("500", serverError);
                }));
        };
    }

    private ApiResponse errorResponse(String description) {
        return new ApiResponse()
            .description(description)
            .content(new Content().addMediaType("application/json",
                new MediaType().schema(
                    new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }
}
