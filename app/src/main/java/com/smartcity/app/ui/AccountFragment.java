package com.smartcity.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.smartcity.app.R;
import com.smartcity.app.data.model.UserProfile;
import com.smartcity.app.viewmodel.AuthViewModel;

/**
 * Account fragment:
 * - If user is null → show LoginFragment
 * - If user is logged in → show email, editable name & age, save + sign-out buttons
 */
public class AccountFragment extends Fragment {

    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        android.widget.TextView tvEmail = view.findViewById(R.id.tv_account_email);
        android.widget.TextView tvUid   = view.findViewById(R.id.tv_account_uid);
        TextInputEditText etName        = view.findViewById(R.id.et_account_name);
        TextInputEditText etAge         = view.findViewById(R.id.et_account_age);
        View btnSave                    = view.findViewById(R.id.btn_save_profile);
        View btnLogout                  = view.findViewById(R.id.btn_logout);
        View btnSignOutIcon             = view.findViewById(R.id.btn_signout_icon);

        // Both sign-out buttons do the same thing
        View.OnClickListener signOut = v -> authViewModel.logout();
        btnLogout.setOnClickListener(signOut);
        btnSignOutIcon.setOnClickListener(signOut);

        // Save name + age to Firebase DB
        btnSave.setOnClickListener(v -> {
            UserProfile u = authViewModel.getUser().getValue();
            if (u == null) return;
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String age  = etAge.getText()  != null ? etAge.getText().toString().trim()  : "";
            authViewModel.saveProfile(u.getUid(), name, age);
        });

        // Show save result as a toast
        authViewModel.getSaveResultState().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });

        // Observe auth state → populate fields or redirect to Login
        authViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .commit();
            } else {
                tvEmail.setText(user.getEmail());
                tvUid.setText("User ID: " + user.getUid());
                // Pre-fill editable fields if profile already saved
                if (!user.getName().isEmpty()) etName.setText(user.getName());
                if (!user.getAge().isEmpty())  etAge.setText(user.getAge());
            }
        });

        return view;
    }
}
