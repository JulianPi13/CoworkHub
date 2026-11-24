package com.campus.coworkhub.business.exception;

/** Se lanza cuando se viola una regla de negocio (RN-xx) o hay un duplicado. Responde 409. */
public class BusinessRuleException extends RuntimeException {

    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
