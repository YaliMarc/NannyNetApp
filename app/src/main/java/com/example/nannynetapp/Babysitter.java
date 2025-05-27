package com.example.nannynetapp;

/**
 * The type Babysitter.
 */
public class Babysitter {

        private String name;
        private String location;
        private String date;
        private String startTime;
        private String endTime;

    /**
     * Instantiates a new Babysitter.
     */
// דרוש לפיירבייס
        public Babysitter() {}

    /**
     * Instantiates a new Babysitter.
     *
     * @param name      the name
     * @param location  the location
     * @param date      the date
     * @param startTime the start time
     * @param endTime   the end time
     */
    public Babysitter(String name, String location, String date, String startTime, String endTime) {
            this.name = name;
            this.location = location;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
        }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
            return name;
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
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
            this.name = name;
        }

    /**
     * Sets location.
     *
     * @param location the location
     */
    public void setLocation(String location) {
            this.location = location;
        }

    /**
     * Sets date.
     *
     * @param date the date
     */
    public void setDate(String date) {
            this.date = date;
        }

    /**
     * Sets start time.
     *
     * @param startTime the start time
     */
    public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

    /**
     * Sets end time.
     *
     * @param endTime the end time
     */
    public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }

