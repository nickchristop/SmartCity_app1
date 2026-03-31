package com.smartcity.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smartcity.app.data.model.UserProfile;

/**
 * ACADEMIC MVVM DOCUMENTATION:
 * Leveraging the Repository Pattern, the UI and ViewModel layers are strictly "Backend Agnostic."
 * They maintain zero direct references to Firebase capabilities. This decoupling allows the Session
 * Management to be seamlessly migrated to Supabase Auth without triggering UI cascading rewrites.
 */
public class AuthRepository {
    private final FirebaseAuth auth;
    private final MutableLiveData<UserProfile> userLiveData;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        userLiveData = new MutableLiveData<>();
        
        // Listeners autonomously broadcast session switches system-wide
        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                userLiveData.postValue(new UserProfile(user.getUid(), user.getEmail()));
            } else {
                userLiveData.postValue(null);
            }
        });
    }

    public LiveData<UserProfile> getUserLiveData() {
        return userLiveData;
    }

    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(result -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void register(String email, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(result -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void logout() {
        auth.signOut();
    }

    // Abstract interface ensures UI acts autonomously from Google Tasks
    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }
}
