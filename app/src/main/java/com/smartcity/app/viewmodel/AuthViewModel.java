package com.smartcity.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.smartcity.app.data.model.UserProfile;
import com.smartcity.app.data.repository.AuthRepository;

/**
 * Pipes User Sessions reactively via Android LiveData.
 * Total "Backend Agnostic" decoupling.
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final LiveData<UserProfile> userProfileLiveData;

    // Transient UI state (toasts, navigation triggers)
    private final MutableLiveData<String>  authErrorState   = new MutableLiveData<>();
    private final MutableLiveData<Boolean> authSuccessState = new MutableLiveData<>();
    private final MutableLiveData<String>  saveResultState  = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository    = new AuthRepository();
        userProfileLiveData = authRepository.getUserLiveData();
    }

    public LiveData<UserProfile> getUser()               { return userProfileLiveData; }
    public LiveData<String>  getAuthErrorState()         { return authErrorState; }
    public LiveData<Boolean> getAuthSuccessState()       { return authSuccessState; }
    public LiveData<String>  getSaveResultState()        { return saveResultState; }

    public void login(String email, String password) {
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override public void onSuccess()            { authSuccessState.postValue(true); }
            @Override public void onError(String msg)    { authErrorState.postValue(msg); }
        });
    }

    public void register(String email, String password) {
        authRepository.register(email, password, new AuthRepository.AuthCallback() {
            @Override public void onSuccess()            { authSuccessState.postValue(true); }
            @Override public void onError(String msg)    { authErrorState.postValue(msg); }
        });
    }

    /** Persists name + age to Firebase DB for the given user UID */
    public void saveProfile(String uid, String name, String age) {
        authRepository.saveProfile(uid, name, age, errorOrNull ->
                saveResultState.postValue(
                        errorOrNull == null ? "Saved!" : "Error: " + errorOrNull));
    }

    public void logout()              { authRepository.logout(); authSuccessState.setValue(false); }
    public void clearErrorState()     { authErrorState.setValue(null); }
    public void clearSuccessState()   { authSuccessState.setValue(null); }
}
