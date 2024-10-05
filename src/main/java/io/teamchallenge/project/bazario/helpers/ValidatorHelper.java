package io.teamchallenge.project.bazario.helpers;

public interface ValidatorHelper {
    static boolean isValidEmail(String email) {
        // taken from https://owasp.org/www-community/OWASP_Validation_Regex_Repository#
        final var EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";

        return email != null && email.matches(EMAIL_REGEX);
    }

    static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^\\+38\\d{10}$");
    }
}
