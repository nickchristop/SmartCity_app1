package com.smartcity.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smartcity.app.R;
import com.smartcity.app.data.model.UserProfile;
import com.smartcity.app.viewmodel.AuthViewModel;

/**
 * Account fragment.
 * - Unauthenticated → navigates to LoginFragment.
 * - Authenticated    → shows email, name/age in VIEW mode (fields disabled).
 *   "Edit Profile" button enables fields → "Save Profile" on click saves + re-locks fields.
 *   Sign-out icon (top-right) and red Sign Out button both trigger logout.
 * - User ID row has been removed as requested.
 */
public class AccountFragment extends Fragment {

    private AuthViewModel authViewModel;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        TextView         tvEmail       = view.findViewById(R.id.tv_account_email);
        TextInputEditText etName       = view.findViewById(R.id.et_account_name);
        TextInputEditText etAge        = view.findViewById(R.id.et_account_age);
        MaterialButton   btnToggle     = view.findViewById(R.id.btn_save_profile);
        View             btnLogout     = view.findViewById(R.id.btn_logout);
        View             btnSignoutIcon= view.findViewById(R.id.btn_signout_icon);

        // Start in VIEW mode (fields disabled, button = "Edit Profile")
        setEditMode(false, etName, etAge, btnToggle);

        // Toggle between Edit and Save
        btnToggle.setOnClickListener(v -> {
            if (!isEditMode) {
                // Switch to EDIT mode
                setEditMode(true, etName, etAge, btnToggle);
            } else {
                // SAVE and return to VIEW mode
                UserProfile u = authViewModel.getUser().getValue();
                if (u == null) return;
                String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                String age  = etAge.getText()  != null ? etAge.getText().toString().trim()  : "";
                authViewModel.saveProfile(u.getUid(), name, age);
                setEditMode(false, etName, etAge, btnToggle);
            }
        });

        // Save result toast (uses locale-appropriate getString from ViewModel's context)
        authViewModel.getSaveResultState().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                String toastText = msg.startsWith("Error") ? msg : getString(R.string.msg_saved);
                Toast.makeText(getContext(), toastText, Toast.LENGTH_SHORT).show();
            }
        });

        // Logout
        View.OnClickListener signOut = sv -> authViewModel.logout();
        btnLogout.setOnClickListener(signOut);
        btnSignoutIcon.setOnClickListener(signOut);

        // Observe auth state
        authViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .commit();
            } else {
                tvEmail.setText(user.getEmail());
                // Pre-fill saved name/age (fields stay disabled until Edit is pressed)
                if (!user.getName().isEmpty()) etName.setText(user.getName());
                if (!user.getAge().isEmpty())  etAge.setText(user.getAge());
            }
        });

        return view;
    }

    /** Switches the form between VIEW (locked) and EDIT (unlocked) modes */
    private void setEditMode(boolean edit, TextInputEditText etName,
                             TextInputEditText etAge, MaterialButton btn) {
        isEditMode = edit;
        etName.setEnabled(edit);
        etAge.setEnabled(edit);
        btn.setText(edit ? getString(R.string.btn_save_profile)
                         : getString(R.string.btn_edit_profile));
    }
}
