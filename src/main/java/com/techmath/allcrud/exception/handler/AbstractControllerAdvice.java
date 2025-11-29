package com.techmath.allcrud.exception.handler;

import com.techmath.allcrud.common.ControllerErrorVO;
import com.techmath.allcrud.enums.CrudErrorMessage;
import com.techmath.allcrud.exception.BusinessException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.*;

/**
 * Abstract base class for global exception handling in Allcrud controllers.
 * <p>
 * Captures and converts standard exceptions like {@code EntityNotFoundException},
 * {@code MethodArgumentNotValidException}, and {@code BusinessException} into structured
 * JSON error responses using {@link ControllerErrorVO}.
 * <p>
 * This class is meant to be extended by concrete `@ControllerAdvice` classes.
 * <p>
 * Logs exception messages using SLF4J, and formats user-friendly errors for the client.
 *
 * @see com.techmath.allcrud.exception.BusinessException
 * @see com.techmath.allcrud.enums.CrudErrorMessage
 * @see com.techmath.allcrud.common.ControllerErrorVO
 *
 * @author Matheus Maia
 */
@Slf4j
public abstract class AbstractControllerAdvice {

    /**
     * Protected constructor to prevent direct instantiation.
     * This class is designed to be extended by concrete controller advice classes.
     */
    protected AbstractControllerAdvice() {
        // Constructor for subclasses
    }

    protected static final String BUSINESS_RULE_FAILED = "Business rule failed";
    protected static final String HTTP_MESSAGE_NOT_READABLE = "Http message not readable";

    /**
     * Handles {@link EntityNotFoundException} thrown when an entity is not found.
     *
     * @param ex the exception thrown
     * @return list of error VOs with 404 status
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public List<ControllerErrorVO> entityNotFound(EntityNotFoundException ex) {
        logMessages(CrudErrorMessage.ENTITY_NOT_FOUND_MESSAGE, ex);
        return buildErrorResponse(CrudErrorMessage.ENTITY_NOT_FOUND_MESSAGE.getTitle(), ex.getMessage());
    }

    /**
     * Handles {@link EntityExistsException} thrown when trying to create a duplicate entity.
     *
     * @param ex the exception thrown
     * @return list of error VOs with 400 status
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EntityExistsException.class)
    public List<ControllerErrorVO> entityExists(EntityExistsException ex) {
        logMessages(CrudErrorMessage.ENTITY_ALREADY_EXISTS_MESSAGE, ex);
        return buildErrorResponse(CrudErrorMessage.ENTITY_ALREADY_EXISTS_MESSAGE.getTitle(), ex.getMessage());
    }

    /**
     * Handles {@link MethodArgumentNotValidException} thrown when bean validation fails.
     *
     * @param ex the exception thrown
     * @return list of error VOs with 400 status
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public List<ControllerErrorVO> handleValidationException(MethodArgumentNotValidException ex) {
        logMessages(CrudErrorMessage.VALIDATION_CONSTRAINTS_FAILED_MESSAGE, ex);

        Map<String, String> failedFields = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            var fieldName = ((FieldError) error).getField();
            var errorMessage = error.getDefaultMessage();
            failedFields.put(fieldName, errorMessage);
        });

        return buildValidationErrorsFromFields(failedFields);
    }

    /**
     * Handles {@link ConstraintViolationException} thrown when constraint validation fails.
     *
     * @param ex the exception thrown
     * @return list of error VOs with 400 status
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public List<ControllerErrorVO> handleValidationException(ConstraintViolationException ex) {
        logMessages(CrudErrorMessage.VALIDATION_CONSTRAINTS_FAILED_MESSAGE, ex);

        Map<String, String> failedFields = new HashMap<>();
        ex.getConstraintViolations().forEach(error -> {
            var fieldName = error.getPropertyPath().toString();
            var errorMessage = error.getMessage();
            failedFields.put(fieldName, errorMessage);
        });

        return buildValidationErrorsFromFields(failedFields);
    }

    /**
     * Handles {@link HttpMessageNotReadableException} thrown when request body cannot be parsed.
     *
     * @param ex the exception thrown
     * @return list of error VOs with 400 status
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public List<ControllerErrorVO> httpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logMessages(HTTP_MESSAGE_NOT_READABLE, ex);
        var message = ex.getMessage();
        var treatedMessage = message.substring(message.indexOf("String"));
        var error = new ControllerErrorVO(HTTP_MESSAGE_NOT_READABLE, treatedMessage);
        return List.of(error);
    }

    /**
     * Handles {@link BusinessException} thrown when business rules are violated.
     *
     * @param ex the exception thrown
     * @return list of error VOs with 422 status
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(BusinessException.class)
    public List<ControllerErrorVO> businessException(BusinessException ex) {
        logMessages(BUSINESS_RULE_FAILED, ex);
        List<ControllerErrorVO> errors = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ex.getMessages())) {
            errors.addAll(buildErrorResponse(BUSINESS_RULE_FAILED, ex.getMessagesArray()));
        } else {
            errors.add(new ControllerErrorVO(BUSINESS_RULE_FAILED, ex.getMessage()));
        }
        return errors;
    }

    //*****************************************************************************************************************
    //******************************************* PRIVATE/PROTECTED METHODS *******************************************
    //*****************************************************************************************************************

    protected List<ControllerErrorVO> buildErrorResponse(String title, String... messages) {
        List<ControllerErrorVO> errors = new ArrayList<>();
        if (Objects.nonNull(messages)) {
            for (var message : messages) {
                errors.add(new ControllerErrorVO(title, message));
            }
        }
        return errors;
    }

    private List<ControllerErrorVO> buildValidationErrorsFromFields(Map<String, String> failedFields) {
        List<ControllerErrorVO> errors = new ArrayList<>();
        for (var entry : failedFields.entrySet()) {
            var baseMessage = CrudErrorMessage.VALIDATION_CONSTRAINTS_FAILED_MESSAGE.getMessage();
            var message = String.format(baseMessage, entry.getKey(), entry.getValue());
            errors.add(new ControllerErrorVO(CrudErrorMessage.VALIDATION_CONSTRAINTS_FAILED_MESSAGE.getTitle(), message));
        }
        return errors;
    }

    protected void logMessages(String errorMessage, Exception ex) {
        log.debug(errorMessage, ex);
        if (!log.isDebugEnabled()) {
            log.warn(errorMessage);
        }
    }

    private void logMessages(CrudErrorMessage errorMessage, Exception ex) {
        log.debug(errorMessage.getTitle(), ex);
        if (!log.isDebugEnabled()) {
            log.warn(errorMessage.getTitle());
        }
    }

}
