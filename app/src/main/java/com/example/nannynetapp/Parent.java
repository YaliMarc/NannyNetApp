package com.example.nannynetapp;

/**
 * The type Parent.
 */
public class Parent {

        private String fullName;
        private String location;
        private String date;
        private String startTime;
        private String endTime;
        private String phoneNumber;

    /**
     * Instantiates a new Parent.
     */
    public Parent() {
            // חובה לפיירבייס
        }

    /**
     * Instantiates a new Parent.
     *
     * @param fullName    the full name
     * @param location    the location
     * @param date        the date
     * @param startTime   the start time
     * @param endTime     the end time
     * @param phoneNumber the phone number
     */
    public Parent(String fullName, String location, String date, String startTime, String endTime, String phoneNumber) {
            this.fullName = fullName;
            this.location = location;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.phoneNumber = phoneNumber;
        }

    /**
     * Gets full name.
     *
     * @return the full name
     */
    public String getFullName() {
            return fullName;
        }

    /**
     * Gets location.
     *
     * @return the location
     */
    public String getLocation() {
            return location;
        }

    /**
     * Gets date.
     *
     * @return the date
     */
    public String getDate() {
            return date;
        }

    /**
     * Gets start time.
     *
     * @return the start time
     */
    public String getStartTime() {
            return startTime;
        }

    /**
     * Gets end time.
     *
     * @return the end time
     */
    public String getEndTime() {
            return endTime;
        }

    /**
     * Gets phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() {
            return phoneNumber;
        }
    }


