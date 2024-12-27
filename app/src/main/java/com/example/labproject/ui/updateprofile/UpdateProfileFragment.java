package com.example.labproject.ui.updateprofile;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.labproject.R;
import com.example.labproject.DataBaseHelper;

import java.util.regex.Pattern;

public class UpdateProfileFragment extends Fragment {

    private UpdateProfileViewModel mViewModel;
    private DataBaseHelper databaseHelper;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,12}$");

    public static UpdateProfileFragment newInstance() {
        return new UpdateProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update_profile, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UpdateProfileViewModel.class);

        databaseHelper = new DataBaseHelper(getActivity(), "DB", null, 1);

        View view = getView();
        if (view != null) {
            RadioGroup radioGroup = view.findViewById(R.id.radio_group_choice);
            EditText emailField = view.findViewById(R.id.edit_email);
            EditText passwordField = view.findViewById(R.id.edit_password);
            Button updateButton = view.findViewById(R.id.button_update_profile);

            emailField.setVisibility(View.GONE);
            passwordField.setVisibility(View.GONE);

            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.radio_edit_email) {
                    emailField.setVisibility(View.VISIBLE);
                    passwordField.setVisibility(View.GONE);
                } else if (checkedId == R.id.radio_edit_password) {
                    emailField.setVisibility(View.GONE);
                    passwordField.setVisibility(View.VISIBLE);
                }
            });

            updateButton.setOnClickListener(v -> {
                if (emailField.getVisibility() == View.VISIBLE) {
                    String updatedEmail = emailField.getText().toString().trim();
                    if (!isValidEmail(updatedEmail)) {
                        emailField.setError("Invalid email format");
                        //Toast.makeText(getActivity(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    } else if (databaseHelper.checkUsernameExists(updatedEmail)) {
                        Toast.makeText(getActivity(), "Email already exists in the database", Toast.LENGTH_SHORT).show();
                    } else {
                        boolean success = databaseHelper.updateEmail(getCurrentUserEmail(), updatedEmail);
                        if (success) {
                            Toast.makeText(getActivity(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to update email", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (passwordField.getVisibility() == View.VISIBLE) {
                    String updatedPassword = passwordField.getText().toString().trim();
                    if (!isValidPassword(updatedPassword)) {
                        passwordField.setError("Password must be 6-12 characters, include uppercase, lowercase, and a number");
                        //Toast.makeText(getActivity(), "Password must be 6-12 characters, include at least one number, one lowercase, and one uppercase letter", Toast.LENGTH_SHORT).show();
                    } else {
                        boolean success = databaseHelper.updatePassword(getCurrentUserEmail(), updatedPassword);
                        if (success) {
                            Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to update password", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "Please select an option", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getCurrentUserEmail() {
        return getActivity().getIntent().getStringExtra("email");
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
