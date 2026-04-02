package com.smartcity.app.data.model;

/** Authenticated user's profile. Stored in Firebase DB under users/{uid}. */
public class UserProfile {
    private String uid;
    private String email;
    private String name;  // Display name (editable)
    private String age;   // Stored as String for simplicity

    public UserProfile() {} // Firebase no-arg constructor

    public UserProfile(String uid, String email) {
        this.uid   = uid;
        this.email = email;
        this.name  = "";
        this.age   = "";
    }

    public String getUid()   { return uid; }
    public String getEmail() { return email; }
    public String getName()  { return name != null ? name : ""; }
    public String getAge()   { return age  != null ? age  : ""; }

    public void setUid(String uid)     { this.uid   = uid; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name)   { this.name  = name; }
    public void setAge(String age)     { this.age   = age; }
}
