package com.techmath.allcrud.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void whenConstructedWithNonEmptyMessageList_thenMessagesArePreserved() {
        List<String> messages = List.of("first error", "second error");

        BusinessException exception = new BusinessException(messages);

        assertThat(exception.getMessages()).containsExactly("first error", "second error");
        assertThat(exception.getMessagesArray()).containsExactly("first error", "second error");
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    void whenConstructedWithEmptyMessageList_thenMessagesIsEmpty() {
        BusinessException exception = new BusinessException(List.<String>of());

        assertThat(exception.getMessages()).isEmpty();
        assertThat(exception.getMessagesArray()).isEmpty();
    }

    @Test
    void whenConstructedWithNullMessageList_thenMessagesIsEmpty() {
        BusinessException exception = new BusinessException((List<String>) null);

        assertThat(exception.getMessages()).isEmpty();
        assertThat(exception.getMessagesArray()).isEmpty();
    }

    @Test
    void whenConstructedWithSingleMessage_thenMessageIsSetAndMessagesIsEmpty() {
        BusinessException exception = new BusinessException("single error");

        assertThat(exception.getMessage()).isEqualTo("single error");
        assertThat(exception.getMessages()).isEmpty();
        assertThat(exception.getMessagesArray()).isEmpty();
    }

}
