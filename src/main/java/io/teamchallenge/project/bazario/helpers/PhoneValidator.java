package io.teamchallenge.project.bazario.helpers;

import io.teamchallenge.project.bazario.web.dto.Phone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return ValidatorHelper.isValidPhone(value);
    }
}
