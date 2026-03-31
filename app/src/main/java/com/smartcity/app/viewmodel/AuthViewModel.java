package com.smartcity.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.smartcity.app.data.model.UserProfile;
import com.smartcity.app.data.repository.AuthRepository;

/**
 * ACADEMIC MVVM DOCUMENTATION:
 * Pipes User Sessions reactively via Android LiveData. Total "Backend Agnostic" decoupling.
 */
public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final LiveData<UserProfile> userProfileLiveData;
    
    // UI Transient state handling
    private final MutableLiveData<String> authErrorState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> authSuccessState = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository = new AuthRepository();
        userProfileLiveData = authRepository.getUserLiveData();
    }

    public LiveData<UserProfile> getUser() {
        return userProfileLiveData;
    }

    public LiveData<String> getAuthErrorState() { return authErrorState; }
    public LiveData<Boolean> getAuthSuccessState() { return authSuccessState; }

    public void login(String email, String password) {
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                authSuccessState.postValue(true);
            }
            @Override
            public void onError(String message) {
                authErrorState.postValue(message);
            }
        });
    }

    public void register(String email, String password) {
        authRepository.register(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                authSuccessState.postValue(true);
            }
            @Override
            public void onError(String message) {
                authErrorState.postValue(message);
            }
        });
    }

    public void logout() {
        authRepository.logout();
        authSuccessState.setValue(false);
    }
    
    public void clearErrorState() {
        authErrorState.setValue(null);
    }
    
    public void clearSuccessState() {
        authSuccessState.setValue(null);
    }
}
