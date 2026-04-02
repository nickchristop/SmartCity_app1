package com.smartcity.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.smartcity.app.R;
import com.smartcity.app.viewmodel.AuthViewModel;

/**
 * Login screen.
 * Error messages are already made user-friendly by AuthRepository:
 *  - Wrong email → "No account found. Please Register."
 *  - Wrong password → "Wrong password. Please try again."
 * When "no account" error arrives, we offer an inline "Register" snackbar action.
 */
public class LoginFragment extends Fragment {

    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        EditText etEmail    = view.findViewById(R.id.et_login_email);
        EditText etPassword = view.findViewById(R.id.et_login_password);
        Button   btnSubmit  = view.findViewById(R.id.btn_login_submit);
        View     tvGoReg    = view.findViewById(R.id.tv_go_to_register);

        btnSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pwd   = etPassword.getText().toString().trim();
            if (email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            authViewModel.login(email, pwd);
        });

        tvGoReg.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegisterFragment())
                        .commit());

        // Error observer: shows friendly message; prompts registration for "no account" errors
        authViewModel.getAuthErrorState().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg == null || errorMsg.isEmpty()) return;

            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            authViewModel.clearErrorState();

            // If the error is "no account found" → also offer a Snackbar with "Register" action
            if (errorMsg.startsWith("No account")) {
                com.google.android.material.snackbar.Snackbar
                        .make(view, "No account? Create one now.", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                        .setAction("Register", sv ->
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new RegisterFragment())
                                        .commit())
                        .show();
            }
        });

        // On login success → go to Account page
        authViewModel.getAuthSuccessState().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                authViewModel.clearSuccessState();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AccountFragment())
                        .commit();
            }
        });

        return view;
    }
}
