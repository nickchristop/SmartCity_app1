package com.smartcity.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.smartcity.app.R;
import com.smartcity.app.viewmodel.AuthViewModel;

/**
 * ACADEMIC MVVM DOCUMENTATION:
 * The AccountFragment leverages the reactive Observer Pattern to dictate its state context.
 * It has zero awareness of Firebase SDKs, relying purely on the decoupled LiveData routing.
 */
public class AccountFragment extends Fragment {

    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        TextView tvEmail = view.findViewById(R.id.tv_account_email);
        TextView tvUid = view.findViewById(R.id.tv_account_uid);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(v -> authViewModel.logout());

        authViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                // Safe Routing: Trigger LoginFragment transition transparently upon logout/null state
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .commit();
            } else {
                tvEmail.setText(user.getEmail());
                tvUid.setText("UID: " + user.getUid());
            }
        });

        return view;
    }
}
