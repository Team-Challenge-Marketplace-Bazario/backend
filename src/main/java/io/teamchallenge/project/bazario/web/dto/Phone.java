package io.teamchallenge.project.bazario.web.dto;

import io.teamchallenge.project.bazario.helpers.PhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface Phone {
    String message() default "Field phone not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
