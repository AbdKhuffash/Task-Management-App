package com.example.labproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //hideKeyboard(getCurrentFocus());
        getSupportActionBar().hide();
        EditText email=findViewById(R.id.emailET);
        EditText firstName=findViewById(R.id.FirstNameET);
        EditText lastName=findViewById(R.id.LastNameET);
        EditText password=findViewById(R.id.PasswordET);
        EditText confirmPassword=findViewById(R.id.ConfirmPasswordET);
        Button signUp=findViewById(R.id.SignUpBTN);


        DataBaseHelper dbHelper = new DataBaseHelper(this, "DB", null, 1);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(getCurrentFocus());
                String emailInput = email.getText().toString().trim();
                String firstNameInput = firstName.getText().toString().trim();
                String lastNameInput = lastName.getText().toString().trim();
                String passwordInput = password.getText().toString().trim();
                String confirmPasswordInput = confirmPassword.getText().toString().trim();

                if (!isValidEmail(emailInput)) {
                    email.setError("Invalid email format");
                    return;
                }
                if (!isValidName(firstNameInput)) {
                    firstName.setError("First name must be 5-20 characters");
                    return;
                }
                if (!isValidName(lastNameInput)) {
                    lastName.setError("Last name must be 5-20 characters");
                    return;
                }
                if (!isValidPassword(passwordInput)) {
                    password.setError("Password must be 6-12 characters, include uppercase, lowercase, and a number");
                    return;
                }
                if (!passwordInput.equals(confirmPasswordInput)) {
                    confirmPassword.setError("Passwords do not match");
                    return;
                }

                User newUser = new User(emailInput, firstNameInput, lastNameInput, passwordInput);
                if (dbHelper.registerUser(newUser)) {
                    Toast.makeText(SignUpActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(SignUpActivity.this,SignInActivity.class);
                    SignUpActivity.this.startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Email already exists!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidName(String name) {
        return name.length() >= 5 && name.length() <= 20;
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 6 || password.length() > 12) return false;
        boolean hasUppercase = false, hasLowercase = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            if (Character.isLowerCase(c)) hasLowercase = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUppercase && hasLowercase && hasDigit;
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}