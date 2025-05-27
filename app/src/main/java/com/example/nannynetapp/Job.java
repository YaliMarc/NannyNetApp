package com.example.nannynetapp;

/**
 * The type Job.
 */
public class Job {
    private String jobId;
    private String parentId;
    private String location;
    private String date;
    private String startTime;
    private String endTime;
    private long startTimeMillis;
    private String status; // "Pending", "Approved", "Completed"
    private String babySitterId;
    private boolean isParentApproved;
    private boolean isBabysitterApproved;
    private String approvedBy;
    private String requirements; // JSON string of requirements
    private double minHourlyRate;
    private double maxHourlyRate;
    private boolean reminderSent;
    private String parentName;
    private String babysitterName;
    private boolean isBabysitterSearch; // true if this is a babysitter searching for jobs

    /**
     * Instantiates a new Job.
     */
// ריקון קונסטרקטור נחוץ ל-Firebase
    public Job() {}

    /**
     * Instantiates a new Job.
     *
     * @param other the other
     */
// קונסטרקטור העתקה
    public Job(Job other) {
        this.jobId = other.jobId;
        this.parentId = other.parentId;
        this.location = other.location;
        this.date = other.date;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.startTimeMillis = other.startTimeMillis;
        this.status = other.status;
        this.babySitterId = other.babySitterId;
        this.requirements = other.requirements;
        this.minHourlyRate = other.minHourlyRate;
        this.maxHourlyRate = other.maxHourlyRate;
        this.isParentApproved = other.isParentApproved;
        this.isBabysitterApproved = other.isBabysitterApproved;
        this.approvedBy = other.approvedBy;
        this.reminderSent = other.reminderSent;
        this.parentName = other.parentName;
        this.babysitterName = other.babysitterName;
        this.isBabysitterSearch = other.isBabysitterSearch;
    }

    /**
     * Instantiates a new Job.
     *
     * @param jobId              the job id
     * @param parentId           the parent id
     * @param location           the location
     * @param date               the date
     * @param startTime          the start time
     * @param endTime            the end time
     * @param startTimeMillis    the start time millis
     * @param status             the status
     * @param babySitterId       the baby sitter id
     * @param requirements       the requirements
     * @param minHourlyRate      the min hourly rate
     * @param maxHourlyRate      the max hourly rate
     * @param isBabysitterSearch the is babysitter search
     */
// קונסטרקטור מלא
    public Job(String jobId, String parentId, String location, String date, String startTime, String endTime, 
               long startTimeMillis, String status, String babySitterId, String requirements, 
               double minHourlyRate, double maxHourlyRate, boolean isBabysitterSearch) {
        this.jobId = jobId;
        this.parentId = parentId;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startTimeMillis = startTimeMillis;
        this.status = status;
        this.babySitterId = babySitterId;
        this.requirements = requirements;
        this.minHourlyRate = minHourlyRate;
        this.maxHourlyRate = maxHourlyRate;
        this.isBabysitterSearch = isBabysitterSearch;
        this.isParentApproved = false;
        this.isBabysitterApproved = false;
        this.reminderSent = false;
    }

    /**
     * Gets job id.
     *
     * @return the job id
     */
// GETTERS & SETTERS
    public String getJobId() { return jobId; }

    /**
     * Sets job id.
     *
     * @param jobId the job id
     */
    public void setJobId(String jobId) { this.jobId = jobId; }

    /**
     * Gets parent id.
     *
     * @return the parent id
     */
    public String getParentId() { return parentId; }

    /**
     * Sets parent id.
     *
     * @param parentId the parent id
     */
    public void setParentId(String parentId) { this.parentId = parentId; }

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
     * Gets date.
     *
     * @return the date
     */
    public String getDate() { return date; }

    /**
     * Sets date.
     *
     * @param date the date
     */
    public void setDate(String date) { this.date = date; }

    /**
     * Gets start time.
     *
     * @return the start time
     */
    public String getStartTime() { return startTime; }

    /**
     * Sets start time.
     *
     * @param startTime the start time
     */
    public void setStartTime(String startTime) { this.startTime = startTime; }

    /**
     * Gets end time.
     *
     * @return the end time
     */
    public String getEndTime() { return endTime; }

    /**
     * Sets end time.
     *
     * @param endTime the end time
     */
    public void setEndTime(String endTime) { this.endTime = endTime; }

    /**
     * Gets start time millis.
     *
     * @return the start time millis
     */
    public long getStartTimeMillis() { return startTimeMillis; }

    /**
     * Sets start time millis.
     *
     * @param startTimeMillis the start time millis
     */
    public void setStartTimeMillis(long startTimeMillis) { this.startTimeMillis = startTimeMillis; }

    /**
     * Gets status.
     *
     * @return the status
     */
    public String getStatus() { return status; }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Gets baby sitter id.
     *
     * @return the baby sitter id
     */
    public String getBabySitterId() { return babySitterId; }

    /**
     * Sets baby sitter id.
     *
     * @param babySitterId the baby sitter id
     */
    public void setBabySitterId(String babySitterId) { this.babySitterId = babySitterId; }

    /**
     * Is parent approved boolean.
     *
     * @return the boolean
     */
    public boolean isParentApproved() { return isParentApproved; }

    /**
     * Sets parent approved.
     *
     * @param parentApproved the parent approved
     */
    public void setParentApproved(boolean parentApproved) { isParentApproved = parentApproved; }

    /**
     * Is babysitter approved boolean.
     *
     * @return the boolean
     */
    public boolean isBabysitterApproved() { return isBabysitterApproved; }

    /**
     * Sets babysitter approved.
     *
     * @param babysitterApproved the babysitter approved
     */
    public void setBabysitterApproved(boolean babysitterApproved) { isBabysitterApproved = babysitterApproved; }

    /**
     * Gets approved by.
     *
     * @return the approved by
     */
    public String getApprovedBy() { return approvedBy; }

    /**
     * Sets approved by.
     *
     * @param approvedBy the approved by
     */
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    /**
     * Gets requirements.
     *
     * @return the requirements
     */
    public String getRequirements() { return requirements; }

    /**
     * Sets requirements.
     *
     * @param requirements the requirements
     */
    public void setRequirements(String requirements) { this.requirements = requirements; }

    /**
     * Gets min hourly rate.
     *
     * @return the min hourly rate
     */
    public double getMinHourlyRate() { return minHourlyRate; }

    /**
     * Sets min hourly rate.
     *
     * @param minHourlyRate the min hourly rate
     */
    public void setMinHourlyRate(double minHourlyRate) { this.minHourlyRate = minHourlyRate; }

    /**
     * Gets max hourly rate.
     *
     * @return the max hourly rate
     */
    public double getMaxHourlyRate() { return maxHourlyRate; }

    /**
     * Sets max hourly rate.
     *
     * @param maxHourlyRate the max hourly rate
     */
    public void setMaxHourlyRate(double maxHourlyRate) { this.maxHourlyRate = maxHourlyRate; }

    /**
     * Is reminder sent boolean.
     *
     * @return the boolean
     */
    public boolean isReminderSent() { return reminderSent; }

    /**
     * Sets reminder sent.
     *
     * @param reminderSent the reminder sent
     */
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }

    /**
     * Gets parent name.
     *
     * @return the parent name
     */
    public String getParentName() { return parentName; }

    /**
     * Sets parent name.
     *
     * @param parentName the parent name
     */
    public void setParentName(String parentName) { this.parentName = parentName; }

    /**
     * Gets babysitter name.
     *
     * @return the babysitter name
     */
    public String getBabysitterName() { return babysitterName; }

    /**
     * Sets babysitter name.
     *
     * @param babysitterName the babysitter name
     */
    public void setBabysitterName(String babysitterName) { this.babysitterName = babysitterName; }

    /**
     * Is babysitter search boolean.
     *
     * @return the boolean
     */
    public boolean isBabysitterSearch() { return isBabysitterSearch; }

    /**
     * Sets babysitter search.
     *
     * @param babysitterSearch the babysitter search
     */
    public void setBabysitterSearch(boolean babysitterSearch) { isBabysitterSearch = babysitterSearch; }

    /**
     * Is fully approved boolean.
     *
     * @return the boolean
     */
    public boolean isFullyApproved() {
        return isParentApproved && isBabysitterApproved;
    }

    /**
     * Gets salary range text.
     *
     * @return the salary range text
     */
    public String getSalaryRangeText() {
        return String.format("₪%.0f - ₪%.0f לשעה", minHourlyRate, maxHourlyRate);
    }
}
