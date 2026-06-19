package com.market.financial.infra.exception;

import com.market.financial.dto.StandardError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

        // 1. Erros de validação do Bean Validation (@Valid, @NotBlank, @Min, etc)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<StandardError> handleValidationErrors(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                HttpStatus status = HttpStatus.BAD_REQUEST;
                List<StandardError.ValidationErrorField> fieldErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> new StandardError.ValidationErrorField(error.getField(),
                                                error.getDefaultMessage()))
                                .toList();

                StandardError err = new StandardError(
                                Instant.now(), status.value(), "Erro de validação nos campos", request.getRequestURI(),
                                fieldErrors);
                return ResponseEntity.status(status).body(err);
        }

        // 2. 🛡️ NOVO: Captura quebras de Regras de Negócio do Motor FIFO
        // (IllegalStateException)
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<StandardError> handleIllegalStateException(IllegalStateException ex,
                        HttpServletRequest request) {
                HttpStatus status = HttpStatus.UNPROCESSABLE_CONTENT; // Código 422 - Perfeito para regras de negócio
                                                                     // impeditivas
                StandardError err = new StandardError(
                                Instant.now(), status.value(), ex.getMessage(), request.getRequestURI(), null);
                return ResponseEntity.status(status).body(err);
        }

        // 3. Captura erros específicos de Recursos Não Encontrados (Custom Exception)
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<StandardError> handleResourceNotFoundException(ResourceNotFoundException ex,
                        HttpServletRequest request) {
                HttpStatus status = HttpStatus.NOT_FOUND; // Código 404
                StandardError err = new StandardError(
                                Instant.now(), status.value(), ex.getMessage(), request.getRequestURI(), null);
                return ResponseEntity.status(status).body(err);
        }

        // 4. Captura conflitos de chaves ou registros duplicados (Custom Exception)
        @ExceptionHandler(ResourceAlreadyExistsException.class)
        public ResponseEntity<StandardError> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex,
                        HttpServletRequest request) {
                HttpStatus status = HttpStatus.CONFLICT; // Código 409
                StandardError err = new StandardError(
                                Instant.now(), status.value(), ex.getMessage(), request.getRequestURI(), null);
                return ResponseEntity.status(status).body(err);
        }

        // 5. Captura erros de rotas inexistentes diretas do Spring
        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<StandardError> handleNoResourceFoundException(NoResourceFoundException ex,
                        HttpServletRequest request) {
                HttpStatus status = HttpStatus.NOT_FOUND; // Código 404
                StandardError err = new StandardError(
                                Instant.now(), status.value(),
                                "A rota ou recurso solicitado não foi encontrado. Verifique a URL.",
                                request.getRequestURI(), null);
                return ResponseEntity.status(status).body(err);
        }

        // 6. Captura exceções genéricas de tempo de execução que não se enquadram nas
        // anteriores
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<StandardError> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
                HttpStatus status = HttpStatus.BAD_REQUEST; // Código 400 - Fallback genérico mais seguro que 404
                StandardError err = new StandardError(
                                Instant.now(), status.value(), ex.getMessage(), request.getRequestURI(), null);
                return ResponseEntity.status(status).body(err);
        }

        // 7. Evita que erros inesperados quebrem a API sem tratamento
        @ExceptionHandler(Exception.class)
        public ResponseEntity<StandardError> handleGenericException(Exception ex, HttpServletRequest request) {
                HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // Código 500
                StandardError err = new StandardError(
                                Instant.now(), status.value(),
                                "Ocorreu um erro interno no servidor: " + ex.getLocalizedMessage(),
                                request.getRequestURI(), null);
                return ResponseEntity.status(status).body(err);
        }
}
