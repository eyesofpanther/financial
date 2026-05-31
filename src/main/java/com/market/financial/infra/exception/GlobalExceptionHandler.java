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

        // 1. Captura erros de validação do Bean Validation (@Valid, @NotBlank, @Min,
        // @Max)
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
                                Instant.now(),
                                status.value(),
                                "Erro de validação nos campos",
                                request.getRequestURI(),
                                fieldErrors);

                return ResponseEntity.status(status).body(err);
        }

        // 2. Captura erros de IDs não encontrados que lançamos no Service
        // (RuntimeException)
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<StandardError> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
                HttpStatus status = HttpStatus.NOT_FOUND;

                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                ex.getMessage(),
                                request.getRequestURI(),
                                null // Sem sub-erros de campo neste caso
                );

                return ResponseEntity.status(status).body(err);
        }

        // 3. Opcional: Evita que erros inesperados quebrem a API de forma feia
        @ExceptionHandler(Exception.class)
        public ResponseEntity<StandardError> handleGenericException(Exception ex, HttpServletRequest request) {
                HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "Ocorreu um erro interno no servidor: " + ex.getLocalizedMessage(),
                                request.getRequestURI(),
                                null);

                return ResponseEntity.status(status).body(err);
        }
        // Importe esta classe no topo do arquivo:
        // import org.springframework.web.servlet.resource.NoResourceFoundException;

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<StandardError> handleNoResourceFoundException(NoResourceFoundException ex,
                        HttpServletRequest request) {
                HttpStatus status = HttpStatus.NOT_FOUND; // Retorna 404 em vez de 500

                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                "A rota ou recurso solicitado não foi encontrado. Verifique a URL.",
                                request.getRequestURI(),
                                null);

                return ResponseEntity.status(status).body(err);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<StandardError> handleResourceNotFoundException(ResourceNotFoundException ex,
                        HttpServletRequest request) {
                HttpStatus status = HttpStatus.NOT_FOUND; // Transforma em 404

                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                ex.getMessage(), // Vai exibir a mensagem exata que você escreveu no Service
                                request.getRequestURI(),
                                null);

                return ResponseEntity.status(status).body(err);
        }

        @ExceptionHandler(ResourceAlreadyExistsException.class)
        public ResponseEntity<StandardError> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex,
                        HttpServletRequest request) {
                HttpStatus status = HttpStatus.CONFLICT; // Código 409

                StandardError err = new StandardError(
                                Instant.now(),
                                status.value(),
                                ex.getMessage(),
                                request.getRequestURI(),
                                null);

                return ResponseEntity.status(status).body(err);
        }

}
