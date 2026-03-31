package com.smartcity.app.data.model;

/**
 * ACADEMIC MVVM DOCUMENTATION:
 * This class operates within the strict boundaries of the Model-View-ViewModel (MVVM) architecture.
 * Leveraging the Repository Pattern, the UI and ViewModel layers are strictly "Backend Agnostic."
 * They maintain zero direct references to Firebase capabilities. This decoupling allows the underlying 
 * data source to be seamlessly migrated to a REST API or Supabase without triggering 
 * cascading source rewrites across the application surface.
 */

/**
 * POJO representing a crowdsourced hazard report.
 */
public class Report {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String status;
    private double latitude;
    private double longitude;
    private long timestamp;

    // Required default constructor for Firebase Realtime Database
    public Report() {
    }

    public Report(String id, String userId, String title, String description, String status, double latitude, double longitude, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
