package com.example.nannynetapp;

public class Babysitter {

        private String name;
        private String location;
        private String date;
        private String startTime;
        private String endTime;

        // דרוש לפיירבייס
        public Babysitter() {}

        public Babysitter(String name, String location, String date, String startTime, String endTime) {
            this.name = name;
            this.location = location;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getName() {
            return name;
        }

        public String getLocation() {
            return location;
        }

        public String getDate() {
            return date;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }

