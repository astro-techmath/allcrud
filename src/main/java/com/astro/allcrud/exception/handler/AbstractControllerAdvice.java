package com.astro.allcrud.exception.handler;

import com.astro.allcrud.common.ControllerErrorVO;
import com.astro.allcrud.enums.CrudErrorMessage;
import com.astro.allcrud.exception.BusinessException;
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

@Slf4j
public abstract class AbstractControllerAdvice {

    protected static final String BUSINESS_RULE_FAILED = "Business rule failed";
    protected static final String HTTP_MESSAGE_NOT_READABLE = "Http message not readable";

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public List<ControllerErrorVO> entityNotFound(EntityNotFoundException ex) {
        logMessages(CrudErrorMessage.ENTITY_NOT_FOUND_MESSAGE, ex);
        return buildErrorResponse(CrudErrorMessage.ENTITY_NOT_FOUND_MESSAGE.getTitle(), ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EntityExistsException.class)
    public List<ControllerErrorVO> entityExists(EntityExistsException ex) {
        logMessages(CrudErrorMessage.ENTITY_ALREADY_EXISTS_MESSAGE, ex);
        return buildErrorResponse(CrudErrorMessage.ENTITY_ALREADY_EXISTS_MESSAGE.getTitle(), ex.getMessage());
    }

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

        List<ControllerErrorVO> errors = new ArrayList<>();
        for (var entry : failedFields.entrySet()) {
            var baseMessage = CrudErrorMessage.VALIDATION_CONSTRAINTS_FAILED_MESSAGE.getMessage();
            var message = String.format(baseMessage, entry.getKey(), entry.getValue());
            errors.add(new ControllerErrorVO(CrudErrorMessage.VALIDATION_CONSTRAINTS_FAILED_MESSAGE.getTitle(), message));
        }

        return errors;
    }

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

        List<ControllerErrorVO> errors = new ArrayList<>();
        for (var entry : failedFields.entrySet()) {
            var baseMessage = CrudErrorMessage.VALIDATION_CONSTRAINTS_FAILED_MESSAGE.getMessage();
            var message = String.format(baseMessage, entry.getKey(), entry.getValue());
            errors.add(new ControllerErrorVO(CrudErrorMessage.VALIDATION_CONSTRAINTS_FAILED_MESSAGE.getTitle(), message));
        }

        return errors;
    }

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
