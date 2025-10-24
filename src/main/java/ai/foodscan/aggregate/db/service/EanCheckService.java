package ai.foodscan.aggregate.db.service;

import org.springframework.stereotype.Service;

@Service
public class EanCheckService {

    /**
     * Calculate the check digit for a 12-digit EAN code.
     * @param eanPartial a 12-digit string representing the first 12 digits of an EAN.
     * @return the computed check digit as an int.
     */
    public int calculateEan13CheckDigit(String eanPartial) {
        if (eanPartial == null || eanPartial.length() != 12 || !eanPartial.matches("\\d{12}")) {
            throw new IllegalArgumentException("Input must be a 12-digit number string.");
        }

        int total = 0;
        // Loop through each digit: positions 0,2,4,... get weight 1; positions 1,3,5,... get weight 3.
        for (int i = 0; i < eanPartial.length(); i++) {
            int digit = Character.getNumericValue(eanPartial.charAt(i));
            total += (i % 2 == 0) ? digit : digit * 3;
        }
        // Compute the check digit: (10 - (total mod 10)) mod 10.
        return (10 - (total % 10)) % 10;
    }

    /**
     * Validate a 13-digit EAN code by checking that its last digit matches the computed check digit.
     * @param ean a 13-digit string EAN code.
     * @return true if the EAN is valid, false otherwise.
     */
    public boolean isValidEan(String ean) {
        if (ean == null || ean.length() != 13 || !ean.matches("\\d{13}")) {
            throw new IllegalArgumentException("EAN must be a 13-digit number string.");
        }
        String eanPartial = ean.substring(0, 12);
        int calculatedCheckDigit = calculateEan13CheckDigit(eanPartial);
        int providedCheckDigit = Character.getNumericValue(ean.charAt(12));
        return calculatedCheckDigit == providedCheckDigit;
    }
}
