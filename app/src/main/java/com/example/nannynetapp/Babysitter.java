package com.example.nannynetapp;

public class Babysitter {
    private String id;
    private String fullName;
    private String location;
    private int salary;
    private String availableDates;
    private String availableHours;

    // חובה שיהיה בנאי ריק (נדרש על ידי Firebase)
    public Babysitter() {}

    public Babysitter(String id, String fullName, String location, int salary, String availableDates, String availableHours) {
        this.id = id;
        this.fullName = fullName;
        this.location = location;
        this.salary = salary;
        this.availableDates = availableDates;
        this.availableHours = availableHours;
    }

    // Getters ו- Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public String getAvailableDates() {
        return availableDates;
    }

    public void setAvailableDates(String availableDates) {
        this.availableDates = availableDates;
    }

    public String getAvailableHours() {
        return availableHours;
    }

    public void setAvailableHours(String availableHours) {
        this.availableHours = availableHours;
    }
}
