package com.smartcity.app.data.model;

/**
 * ACADEMIC MVVM DOCUMENTATION:
 * This class operates strictly within the Model-View-ViewModel (MVVM) architecture.
 * It is a Backend-Agnostic POJO preventing the UI from relying on raw 'FirebaseUser' references,
 * establishing true Supabase-ready independence.
 */
public class UserProfile {
    private String uid;
    private String email;

    public UserProfile(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
}
