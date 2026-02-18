package ai.foodscan.aggregate.db.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class EanCheckServiceTest {

    private final EanCheckService eanCheckService = new EanCheckService();

    @Test
    public void testCalculateEan13CheckDigit_ValidInput() {
        // Given a 12-digit EAN partial code
        String eanPartial = "400638133393";
        // When calculating the check digit
        int checkDigit = eanCheckService.calculateEan13CheckDigit(eanPartial);
        // Then the expected check digit should be 1 (4006381333931 is a valid EAN-13 code)
        assertEquals(1, checkDigit, "Check digit should be 1 for input 400638133393");
    }

    @Test
    public void testIsValidEan_ValidEAN() {
        // Given a valid 13-digit EAN code
        String validEan = "4006381333931"; // Correct check digit is 1.
        // When validating the EAN code
        boolean isValid = eanCheckService.isValidEan(validEan);
        // Then the EAN code should be valid
        assertTrue(isValid, "EAN should be valid when the check digit matches.");
    }

    @Test
    public void testIsValidEan_InvalidEAN() {
        // Given an invalid 13-digit EAN code with an incorrect check digit
        String invalidEan = "4006381333932"; // Incorrect check digit (should be 1)
        // When validating the EAN code
        boolean isValid = eanCheckService.isValidEan(invalidEan);
        // Then the EAN code should be invalid
        assertFalse(isValid, "EAN should be invalid when the check digit does not match.");
    }

    @Test
    public void testCalculateEan13CheckDigit_InvalidInputLength() {
        // Given an invalid 12-digit string (incorrect length)
        String invalidEanPartial = "1234567890"; // Only 10 digits
        // When attempting to calculate the check digit, expect an IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eanCheckService.calculateEan13CheckDigit(invalidEanPartial);
        });
        // Then the exception message should indicate the input requirements
        assertTrue(exception.getMessage().contains("Input must be a 12-digit number string"));
    }

    @Test
    public void testIsValidEan_InvalidInputLength() {
        // Given a string that is not 13 digits
        String invalidEan = "123456789012"; // Only 12 digits instead of 13
        // When validating, expect an IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eanCheckService.isValidEan(invalidEan);
        });
        // Then the exception message should indicate the input requirements
        assertTrue(exception.getMessage().contains("EAN must be a 13-digit number string"));
    }

    @Test
    public void testCalculateEan13CheckDigit_NonDigitInput() {
        // Given a non-digit 12-character string
        String nonDigitInput = "ABCDEFGHIJKL";
        // When attempting to calculate the check digit, expect an IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eanCheckService.calculateEan13CheckDigit(nonDigitInput);
        });
        // Then the exception message should indicate the input must be numeric
        assertTrue(exception.getMessage().contains("Input must be a 12-digit number string"));
    }
}
