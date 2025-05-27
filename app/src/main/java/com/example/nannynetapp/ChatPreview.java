package com.example.nannynetapp;

import java.util.Date;

/**
 * The type Chat preview.
 */
public class ChatPreview {
    /**
     * The Chat id.
     */
    public String chatId;
    /**
     * The Display name.
     */
    public String displayName;
    /**
     * The Image url.
     */
    public String imageUrl;
    /**
     * The Last message.
     */
    public String lastMessage;
    /**
     * The Last message time.
     */
    public Date lastMessageTime;

    /**
     * Instantiates a new Chat preview.
     */
    public ChatPreview() {
        // Default constructor for Firebase
    }

    /**
     * Instantiates a new Chat preview.
     *
     * @param chatId      the chat id
     * @param displayName the display name
     * @param imageUrl    the image url
     */
    public ChatPreview(String chatId, String displayName, String imageUrl) {
        this.chatId = chatId;
        this.displayName = displayName;
        this.imageUrl = imageUrl;
        this.lastMessage = "";
        this.lastMessageTime = new Date();
    }

    /**
     * Instantiates a new Chat preview.
     *
     * @param chatId          the chat id
     * @param displayName     the display name
     * @param imageUrl        the image url
     * @param lastMessage     the last message
     * @param lastMessageTime the last message time
     */
    public ChatPreview(String chatId, String displayName, String imageUrl, String lastMessage, Date lastMessageTime) {
        this.chatId = chatId;
        this.displayName = displayName;
        this.imageUrl = imageUrl;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }
}