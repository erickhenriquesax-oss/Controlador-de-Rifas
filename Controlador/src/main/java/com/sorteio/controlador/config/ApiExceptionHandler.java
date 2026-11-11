package com.sorteio.controlador.config;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // 404 - recurso não encontrado
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(EntityNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", exception.getMessage()));
    }

    // 404 - fallback para algum .orElseThrow() sem mensagem
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> notFound(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Recurso não encontrado."));
    }

    // 404 - URL que não existe
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> noRoute(NoResourceFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Endpoint não encontrado."));
    }

    // 401 - login inválido (segurança)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> unauthorized(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "E-mail ou senha inválidos"));
    }

    // 403 - sem permissão (segurança)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> forbidden(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Acesso negado."));
    }

    // 400 - dados inválidos enviados pelo cliente
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("erro", exception.getMessage()));
    }

    // 409 - violação de regra de negócio / conflito com o estado atual
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflict(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", exception.getMessage()));
    }

    // 400 - Bean Validation (@NotNull, @Email, @Min...): lista todos os campos inválidos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalidRequest(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of("erro", message.isEmpty() ? "Requisição inválida" : message));
    }

    // 400 - id não numérico na URL (ex.: /cliente/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> typeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "Valor inválido para o parâmetro '" + exception.getName() + "': " + exception.getValue()));
    }

    // 400 - JSON malformado ou valor de tipo errado (ex.: status de rifa inexistente, texto em campo numérico)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> unreadable(HttpMessageNotReadableException exception) {
        String detalhe = exception.getMostSpecificCause().getMessage();
        String message = "Corpo da requisição inválido ou mal formatado.";
        if (detalhe != null && detalhe.contains("StatusRifa")) {
            message = "Status da rifa inválido. Valores aceitos: ABERTA, ENCERRADA.";
        } else if (detalhe != null && detalhe.contains("Integer")) {
            message = "Valor numérico inválido: os campos de quantidade devem ser números inteiros.";
        }
        return ResponseEntity.badRequest().body(Map.of("erro", message));
    }

    // 405 - método HTTP não suportado no endpoint
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> methodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("erro", "Método " + exception.getMethod() + " não é suportado neste endpoint."));
    }

    // 409 - unique/foreign key no banco
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleConflitoDeDados(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", "Os dados entram em conflito com um registro existente."));
    }

    // 500 - qualquer erro inesperado, no mesmo formato e sem expor detalhes internos
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> unexpected(Exception exception) {
        // Exceções do próprio Spring MVC (415, 406...) já carregam o status correto
        if (exception instanceof ErrorResponse errorResponse) {
            return ResponseEntity.status(errorResponse.getStatusCode())
                    .body(Map.of("erro", "Requisição não suportada: " + errorResponse.getStatusCode()));
        }
        log.error("Erro inesperado", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", "Erro interno no servidor. Tente novamente mais tarde."));
    }
}
