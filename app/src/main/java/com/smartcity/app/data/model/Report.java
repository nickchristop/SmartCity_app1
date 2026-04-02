package com.smartcity.app.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO representing a crowdsourced hazard report.
 * Firebase Realtime Database requires a no-arg constructor and public getters/setters.
 */
public class Report {
    private String id;
    private String userId;
    private String submitterName; // Display name of the person who filed the report
    private String title;
    private String description;
    private String status;
    private double latitude;
    private double longitude;
    private long timestamp;
    // Firebase Storage download URLs for attached images (max 5)
    private List<String> imageUrls;

    // Required by Firebase for deserialization
    public Report() {}

    public Report(String id, String userId, String submitterName, String title,
                  String description, String status,
                  double latitude, double longitude, long timestamp) {
        this.id            = id;
        this.userId        = userId;
        this.submitterName = submitterName;
        this.title         = title;
        this.description   = description;
        this.status        = status;
        this.latitude      = latitude;
        this.longitude     = longitude;
        this.timestamp     = timestamp;
        this.imageUrls     = new ArrayList<>();
    }

    public String getId()            { return id; }
    public void   setId(String id)   { this.id = id; }

    public String getUserId()               { return userId; }
    public void   setUserId(String userId)  { this.userId = userId; }

    public String getSubmitterName()                        { return submitterName != null ? submitterName : ""; }
    public void   setSubmitterName(String submitterName)    { this.submitterName = submitterName; }

    public String getTitle()             { return title; }
    public void   setTitle(String title) { this.title = title; }

    public String getDescription()                   { return description; }
    public void   setDescription(String description) { this.description = description; }

    public String getStatus()              { return status; }
    public void   setStatus(String status) { this.status = status; }

    public double getLatitude()                  { return latitude; }
    public void   setLatitude(double latitude)   { this.latitude = latitude; }

    public double getLongitude()                 { return longitude; }
    public void   setLongitude(double longitude) { this.longitude = longitude; }

    public long getTimestamp()                { return timestamp; }
    public void setTimestamp(long timestamp)  { this.timestamp = timestamp; }

    public List<String> getImageUrls()                      { return imageUrls; }
    public void         setImageUrls(List<String> imageUrls){ this.imageUrls = imageUrls; }
}
