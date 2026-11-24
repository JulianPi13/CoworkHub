package com.campus.coworkhub.business.exception;

/** Datos con formato válido pero incoherentes entre sí (por ejemplo, fin antes que inicio). Responde 400. */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
