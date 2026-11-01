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
    void scoreRejectsNonFiniteValues() {
        assertThrows(AppException.class, () -> Validator.score(Double.NaN, 50));
        assertThrows(AppException.class, () -> Validator.score(10, Double.POSITIVE_INFINITY));
        assertThrows(AppException.class, () -> Validator.nonNegative(Double.NaN, "Assessment score"));
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

    @Test
    void optionalWebUrlAllowsEmptyButRejectsInvalidValues() {
        assertDoesNotThrow(() -> Validator.optionalWebUrl(null, "Repository URL"));
        assertDoesNotThrow(() -> Validator.optionalWebUrl("  ", "Repository URL"));
        assertDoesNotThrow(() -> Validator.optionalWebUrl("https://github.com/example", "Repository URL"));
        assertThrows(AppException.class, () -> Validator.optionalWebUrl("github.com/example", "Repository URL"));
        assertThrows(AppException.class, () -> Validator.optionalWebUrl("ftp://example.com", "Repository URL"));
    }

    @Test
    void maxLengthAllowsNullAndExactLimit() {
        assertDoesNotThrow(() -> Validator.maxLength(null, 5, "Code"));
        assertDoesNotThrow(() -> Validator.maxLength("ABCDE", 5, "Code"));
        assertDoesNotThrow(() -> Validator.maxLength("", 5, "Code"));
    }

    @Test
    void maxLengthRejectsTextThatWouldNotFitTheColumn() {
        AppException error = assertThrows(AppException.class,
                () -> Validator.maxLength("ABCDEF", 5, "Subject code"));
        assertTrue(error.getMessage().contains("Subject code"));
        assertTrue(error.getMessage().contains("5"));
    }
}
