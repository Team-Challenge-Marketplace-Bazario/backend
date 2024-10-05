package io.teamchallenge.project.bazario.web.dto;

import io.teamchallenge.project.bazario.helpers.EmailOrPhoneValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailOrPhoneValidator.class)
public @interface EmailOrPhone {
}
