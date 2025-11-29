package com.techmath.allcrud.exception;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * Custom runtime exception for representing business rule violations.
 * <p>
 * This exception is meant to be thrown when a domain-specific rule fails,
 * and it integrates with the Allcrud error handler to return a structured
 * {@link com.techmath.allcrud.common.ControllerErrorVO} response.
 * <p>
 * It can hold either a single message (inherited from {@link RuntimeException})
 * or a list of messages for more detailed error reporting.
 *
 * @see com.techmath.allcrud.exception.handler.AbstractControllerAdvice
 * @see com.techmath.allcrud.enums.CrudErrorMessage
 *
 * @author Matheus Maia
 */
@Getter
public class BusinessException extends RuntimeException {

    /** List of detailed error messages. */
    private final List<String> messages;

    /**
     * Constructs a {@code BusinessException} with a list of error messages.
     *
     * @param messages the list of error messages
     */
    public BusinessException(List<String> messages) {
        if (CollectionUtils.isNotEmpty(messages)) {
            this.messages = messages;
        } else {
            this.messages = Collections.emptyList();
        }
    }

    /**
     * Constructs a {@code BusinessException} with a single error message.
     *
     * @param message the error message
     */
    public BusinessException(String message) {
        super(message);
        this.messages = Collections.emptyList();
    }

    /**
     * Returns the error messages as a string array.
     *
     * @return array of error messages
     */
    public String[] getMessagesArray() {
        return messages.toArray(new String[0]);
    }

}
