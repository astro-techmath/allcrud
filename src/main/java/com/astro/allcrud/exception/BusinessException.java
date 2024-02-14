package com.astro.allcrud.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class BusinessException extends RuntimeException {

    private final String message;
    private final List<String> messages;

    public BusinessException(List<String> messages) {
        this.message = null;
        this.messages = messages;
    }

    public String[] getMessagesArray() {
        return messages.toArray(new String[0]);
    }

}
