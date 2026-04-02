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

public class LoginFragment extends Fragment {

    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        EditText etEmail   = view.findViewById(R.id.et_login_email);
        EditText etPassword= view.findViewById(R.id.et_login_password);
        Button   btnSubmit = view.findViewById(R.id.btn_login_submit);
        View     tvGoReg   = view.findViewById(R.id.tv_go_to_register);

        btnSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pwd   = etPassword.getText().toString().trim();
            if (email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.msg_fields_empty), Toast.LENGTH_SHORT).show();
                return;
            }
            authViewModel.login(email, pwd);
        });

        tvGoReg.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegisterFragment())
                        .commit());

        authViewModel.getAuthErrorState().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg == null || errorMsg.isEmpty()) return;
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            authViewModel.clearErrorState();

            // "No account" error → offer Snackbar shortcut to Register
            if (errorMsg.startsWith("No account")) {
                com.google.android.material.snackbar.Snackbar
                        .make(view, getString(R.string.msg_no_account),
                                com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.action_register), sv ->
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new RegisterFragment())
                                        .commit())
                        .show();
            }
        });

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
