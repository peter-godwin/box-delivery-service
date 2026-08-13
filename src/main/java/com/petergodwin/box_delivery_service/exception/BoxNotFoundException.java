package com.petergodwin.box_delivery_service.exception;

public class BoxNotFoundException extends RuntimeException {

    public BoxNotFoundException(String message) {
        super(message);
    }
}