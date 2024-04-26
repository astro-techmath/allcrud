package com.astro.allcrud.exception;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Getter
public class BusinessException extends RuntimeException {

    private final List<String> messages;

    public BusinessException(List<String> messages) {
        if (CollectionUtils.isNotEmpty(messages)) {
            this.messages = messages;
        } else {
            this.messages = Collections.emptyList();
        }
    }

    public String[] getMessagesArray() {
        return messages.toArray(new String[0]);
    }

}
