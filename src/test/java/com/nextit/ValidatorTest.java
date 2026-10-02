package com.nextit;

import com.nextit.exception.AppException;
import com.nextit.util.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    @Test
    void notBlankRejectsEmpty() {
        assertThrows(AppException.class, () -> Validator.notBlank("  ", "Name"));
    }

    @Test
    void notBlankAcceptsValue() {
        assertDoesNotThrow(() -> Validator.notBlank("Maria", "Name"));
    }

    @Test
    void scoreMustNotExceedMax() {
        assertThrows(AppException.class, () -> Validator.score(60, 50));
    }

    @Test
    void scoreAcceptsValid() {
        assertDoesNotThrow(() -> Validator.score(45, 50));
    }

    @Test
    void emailValidatesFormat() {
        assertThrows(AppException.class, () -> Validator.email("not-an-email"));
        assertDoesNotThrow(() -> Validator.email("student@nextit.edu"));
    }
}
