package io.teamchallenge.project.bazario.helpers;

import io.teamchallenge.project.bazario.web.dto.EmailOrPhone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailOrPhoneValidator implements ConstraintValidator<EmailOrPhone, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return ValidatorHelper.isValidEmail(value) || ValidatorHelper.isValidPhone(value);
    }
}
