package com.sorteio.controlador.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";
    private static final String ERRO_SCHEMA = "Erro";

    @Bean
    public OpenAPI controladorOpenAPI() {
        Schema<?> erro = new ObjectSchema()
                .addProperty("erro", new StringSchema().example("Rifa não encontrada com o ID: 99"));

        return new OpenAPI()
                .info(new Info()
                        .title("Controlador de Rifas API")
                        .version("v1")
                        .description("""
                                API para cadastro de clientes, rifas, usuários e compras de cotas.

                                **Como autenticar:** cadastre um usuário em `POST /controlador/usuario` \
                                (idTipoUsuario = 1), faça login em `POST /controlador/usuario/login`, \
                                copie o token e clique em **Authorize**.

                                **Regras de negócio:** rifa encerrada não aceita compras nem alterações (409); \
                                compra acima das cotas disponíveis é recusada (409); \
                                o total da compra é calculado pelo servidor com o preço da cota no momento da compra.

                                **Erros:** todos no formato `{"erro": "mensagem"}`."""))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
                .components(new Components()
                        .addSchemas(ERRO_SCHEMA, erro)
                        .addSecuritySchemes(BEARER, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }

    // Documenta as respostas de erro padrão em todos os endpoints
    @Bean
    public OpenApiCustomizer respostasDeErroPadrao() {
        return openApi -> openApi.getPaths().values().forEach(path -> path.readOperations().forEach(op -> {
            ApiResponses responses = op.getResponses();
            boolean temCorpo = op.getRequestBody() != null;
            boolean temId = path.getParameters() != null || (op.getParameters() != null && !op.getParameters().isEmpty());

            if (temCorpo || temId) {
                responses.addApiResponse("400", erro("Dados inválidos (validação, JSON mal formatado ou ID inválido)"));
            }
            if (temId) {
                responses.addApiResponse("404", erro("Registro não encontrado"));
            }
            if (temCorpo || "DELETE".equalsIgnoreCase(metodo(path, op)) || op.getOperationId().contains("encerrar")
                    || op.getOperationId().contains("reabrir")) {
                responses.addApiResponse("409", erro("Conflito: dado duplicado ou regra de negócio violada"));
            }
            if (op.getSecurity() == null || !op.getSecurity().isEmpty()) {
                responses.addApiResponse("403", erro("Token ausente ou inválido"));
            }
        }));
    }

    private static String metodo(io.swagger.v3.oas.models.PathItem path, io.swagger.v3.oas.models.Operation op) {
        return path.readOperationsMap().entrySet().stream()
                .filter(e -> e.getValue() == op)
                .map(e -> e.getKey().name())
                .findFirst().orElse("");
    }

    private static ApiResponse erro(String descricao) {
        return new ApiResponse()
                .description(descricao)
                .content(new Content().addMediaType("application/json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + ERRO_SCHEMA))));
    }
}
