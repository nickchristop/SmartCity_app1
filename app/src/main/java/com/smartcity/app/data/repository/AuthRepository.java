package com.smartcity.app.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smartcity.app.data.model.UserProfile;

/**
 * Handles Firebase Authentication and profile persistence in Realtime Database.
 * Error messages are user-friendly (not raw Firebase codes).
 */
public class AuthRepository {

    private final FirebaseAuth auth;
    private final MutableLiveData<UserProfile> userLiveData;
    // Reference to the "users" node in Firebase Realtime Database
    private final DatabaseReference usersRef;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        userLiveData = new MutableLiveData<>();
        usersRef = FirebaseDatabase.getInstance(
                "https://smartcityui-12e16-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users");

        // Auto-update userLiveData every time auth state changes
        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // Load extended profile (name, age) from DB
                loadProfile(user.getUid(), user.getEmail());
            } else {
                userLiveData.postValue(null);
            }
        });
    }

    /** Loads name + age from Firebase DB and merges with Auth data */
    private void loadProfile(String uid, String email) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile profile = snapshot.exists()
                        ? snapshot.getValue(UserProfile.class)
                        : new UserProfile(uid, email);
                if (profile != null) {
                    profile.setUid(uid);
                    profile.setEmail(email);
                }
                userLiveData.postValue(profile);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                userLiveData.postValue(new UserProfile(uid, email));
            }
        });
    }

    /** Save name + age back to Firebase DB */
    public void saveProfile(String uid, String name, String age, SimpleCallback callback) {
        usersRef.child(uid).child("name").setValue(name);
        usersRef.child(uid).child("age").setValue(age)
                .addOnSuccessListener(unused -> callback.onDone(null))
                .addOnFailureListener(e -> callback.onDone(e.getMessage()));
    }

    public LiveData<UserProfile> getUserLiveData() { return userLiveData; }

    /** Login with friendly error messages */
    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(friendlyError(e)));
    }

    /** Register with friendly error messages */
    public void register(String email, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(friendlyError(e)));
    }

    public void logout() { auth.signOut(); }

    /** Translates Firebase exceptions into readable messages */
    private String friendlyError(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "No account found with this email. Please Register.";
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Wrong password. Please try again.";
        }
        return e.getMessage(); // fallback for network errors etc.
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface SimpleCallback {
        void onDone(String errorOrNull);
    }
}
