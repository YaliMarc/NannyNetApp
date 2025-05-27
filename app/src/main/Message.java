package com.example.nannynetapp.models;

/**
 * The type Message.
 */
public class Message {
    private String senderId;
    private String receiverId;
    private String messageText;
    private String imageUrl;
    private String timestamp;
    private String type; // "text", "image", "videoCall"

    /**
     * Instantiates a new Message.
     */
    public Message() {
        // נדרש על ידי Firebase
    }

    /**
     * Instantiates a new Message.
     *
     * @param senderId    the sender id
     * @param receiverId  the receiver id
     * @param messageText the message text
     * @param imageUrl    the image url
     * @param timestamp   the timestamp
     * @param type        the type
     */
    public Message(String senderId, String receiverId, String messageText, String imageUrl, String timestamp, String type) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.type = type;
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
     * Gets receiver id.
     *
     * @return the receiver id
     */
    public String getReceiverId() {
        return receiverId;
    }

    /**
     * Gets message text.
     *
     * @return the message text
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * Gets image url.
     *
     * @return the image url
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }
}
