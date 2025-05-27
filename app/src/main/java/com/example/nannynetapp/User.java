package com.example.nannynetapp;

/**
 * The type User.
 */
public class User {
    private String userId;
    private String name;
    private String email;
    private String type; // "parent" or "babysitter"
    private String phone;
    private String location;
    private String profileImageUrl;
    private String bio;
    private double rating;
    private int ratingCount;

    /**
     * Instantiates a new User.
     */
// Empty constructor needed for Firebase
    public User() {}

    /**
     * Instantiates a new User.
     *
     * @param userId the user id
     * @param name   the name
     * @param email  the email
     * @param type   the type
     */
    public User(String userId, String name, String email, String type) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.type = type;
        this.rating = 0.0;
        this.ratingCount = 0;
    }

    /**
     * Gets user id.
     *
     * @return the user id
     */
// Getters and Setters
    public String getUserId() { return userId; }

    /**
     * Sets user id.
     *
     * @param userId the user id
     */
    public void setUserId(String userId) { this.userId = userId; }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() { return name; }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() { return email; }

    /**
     * Sets email.
     *
     * @param email the email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() { return type; }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) { this.type = type; }

    /**
     * Gets phone.
     *
     * @return the phone
     */
    public String getPhone() { return phone; }

    /**
     * Sets phone.
     *
     * @param phone the phone
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Gets location.
     *
     * @return the location
     */
    public String getLocation() { return location; }

    /**
     * Sets location.
     *
     * @param location the location
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * Gets profile image url.
     *
     * @return the profile image url
     */
    public String getProfileImageUrl() { return profileImageUrl; }

    /**
     * Sets profile image url.
     *
     * @param profileImageUrl the profile image url
     */
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    /**
     * Gets bio.
     *
     * @return the bio
     */
    public String getBio() { return bio; }

    /**
     * Sets bio.
     *
     * @param bio the bio
     */
    public void setBio(String bio) { this.bio = bio; }

    /**
     * Gets rating.
     *
     * @return the rating
     */
    public double getRating() { return rating; }

    /**
     * Sets rating.
     *
     * @param rating the rating
     */
    public void setRating(double rating) { this.rating = rating; }

    /**
     * Gets rating count.
     *
     * @return the rating count
     */
    public int getRatingCount() { return ratingCount; }

    /**
     * Sets rating count.
     *
     * @param ratingCount the rating count
     */
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    /**
     * Add rating.
     *
     * @param newRating the new rating
     */
// Helper method for adding new ratings
    public void addRating(double newRating) {
        double totalRating = this.rating * this.ratingCount;
        this.ratingCount++;
        this.rating = (totalRating + newRating) / this.ratingCount;
    }
} 