package com.example.nannynetapp;

import java.util.Date;

/**
 * The type Message.
 */
public class Message {
    private String senderId;
    private Date timestamp;
    private String type; // "text", "image", "video"
    private String content;

    /**
     * Instantiates a new Message.
     */
    public Message() {
        // Required for Firebase
    }

    /**
     * Instantiates a new Message.
     *
     * @param senderId  the sender id
     * @param timestamp the timestamp
     * @param type      the type
     * @param content   the content
     */
    public Message(String senderId, Date timestamp, String type, String content) {
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.type = type;
        this.content = content;
    }

    /**
     * Gets sender id.
     *
     * @return the sender id
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Sets sender id.
     *
     * @param senderId the sender id
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets timestamp.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }
}