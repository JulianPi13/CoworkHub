package com.campus.coworkhub.business.exception;

/** El recurso pedido no existe. Responde 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " con id " + id + " no existe");
    }
}
