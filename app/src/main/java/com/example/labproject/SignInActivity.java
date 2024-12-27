package com.example.labproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignInActivity extends AppCompatActivity {
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        getSupportActionBar().hide();
        //hideKeyboard(getCurrentFocus());

        TextView signup=findViewById(R.id.singup);
        EditText email=findViewById(R.id.emailET);
        EditText password=findViewById(R.id.FirstNameET);
        Button login=findViewById(R.id.SignUpBTN);
        CheckBox rememberMe=findViewById(R.id.remembermeCB);

        sharedPrefManager = SharedPrefManager.getInstance(this);


        String savedEmail = sharedPrefManager.readString("email", "");
        if (!savedEmail.isEmpty()) {
            email.setText(savedEmail);
            //rememberMe.setChecked(true);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredEmail = email.getText().toString().trim();
                String enteredPassword = password.getText().toString().trim();

                hideKeyboard(getCurrentFocus());

                if (isValidCredentials(enteredEmail, enteredPassword)) {
                    if (rememberMe.isChecked()) {
                        sharedPrefManager.writeString("email", enteredEmail);
                    } else {
                        sharedPrefManager.writeString("email", "");
                    }
                    Toast.makeText(SignInActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(SignInActivity.this,MainScreenActivity.class);
                    intent.putExtra("email",enteredEmail);
                    SignInActivity.this.startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignInActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SignInActivity.this,SignUpActivity.class);
                SignInActivity.this.startActivity(intent);
                finish();
            }
        });
    }
    private boolean isValidCredentials(String email, String password) {
        DataBaseHelper dbHelper = new DataBaseHelper(this, "DB", null, 1);
        return dbHelper.validateUserCredentials(email, password);
    }
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}