package io.teamchallenge.project.bazario.helpers;

import io.teamchallenge.project.bazario.web.dto.EmailOrPhone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailOrPhoneValidator implements ConstraintValidator<EmailOrPhone, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return isValidEmail(value) || isValidPhone(value);
    }

    private boolean isValidEmail(String email) {
        // taken from https://owasp.org/www-community/OWASP_Validation_Regex_Repository#
        final var EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";

        return email != null && email.matches(EMAIL_REGEX);
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^\\+38\\d{10}$");
    }
}
