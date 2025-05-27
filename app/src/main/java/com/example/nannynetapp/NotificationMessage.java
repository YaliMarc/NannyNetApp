package com.example.nannynetapp;

/**
 * The type Notification message.
 */
public class NotificationMessage {
    private String message;

    /**
     * Instantiates a new Notification message.
     */
    public NotificationMessage() {
    }

    /**
     * Instantiates a new Notification message.
     *
     * @param message the message
     */
    public NotificationMessage(String message) {
        this.message = message;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}

