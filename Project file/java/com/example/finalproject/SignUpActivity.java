package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private TextView errors;
    private EditText emailAddress, firstName, lastName, password, confirmPassword;
    private Button signup,returnbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        errors = findViewById(R.id.errorHandler);
        emailAddress = findViewById(R.id.emailEditText);
        firstName = findViewById(R.id.firstNameText);
        lastName = findViewById(R.id.lastNameText);
        password = findViewById(R.id.passwordText);
        confirmPassword = findViewById(R.id.confirmPasswordText);
        signup = findViewById(R.id.signupButton);
        returnbutton = findViewById(R.id.returnB);

        emailAddress.setBackgroundResource(R.drawable.normalbg);
        firstName.setBackgroundResource(R.drawable.normalbg);
        lastName.setBackgroundResource(R.drawable.normalbg);
        password.setBackgroundResource(R.drawable.normalbg);
        confirmPassword.setBackgroundResource(R.drawable.normalbg);

        signup.setOnClickListener(v -> {
            RegisterPlayer();
            String check = errors.getText().toString();
            if (check.equals("Added, welcome.")) {
                Toast toast =Toast.makeText(SignUpActivity.this, errors.getText().toString(), Toast.LENGTH_SHORT);
                toast.show();
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        returnbutton.setOnClickListener(v->{
            Toast toast =Toast.makeText(SignUpActivity.this, "Return to sign in", Toast.LENGTH_SHORT);
            toast.show();
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void RegisterPlayer() {
        Users newUser = new Users();
        String email = emailAddress.getText().toString();
        String passwrd = password.getText().toString();
        String confirmpswd = confirmPassword.getText().toString();
        String firstN = firstName.getText().toString();
        String lastN = lastName.getText().toString();

        boolean hasErrors = false;
        errors.setText("");

        if (email.isEmpty()) {
            errors.append("Missing Email Address\n");
            emailAddress.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        } else if (email.matches("(?i).*@.*\\.com")) {
            newUser.setEmailAddress(email);
            emailAddress.setBackgroundResource(R.drawable.normalbg);
        } else {
            errors.append("Email is incorrect\n");
            emailAddress.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        }

        if (firstN.isEmpty()) {
            errors.append("First name is missing \n");
            firstName.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        } else if (isValidName(firstN)) {
            newUser.setFirstName(firstN);
            firstName.setBackgroundResource(R.drawable.normalbg);
        } else {
            errors.append("First name is invalid \n");
            firstName.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        }

        if (lastN.isEmpty()) {
            errors.append("Last name is missing \n");
            lastName.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        } else if (isValidName(lastN)) {
            newUser.setLastName(lastN);
            lastName.setBackgroundResource(R.drawable.normalbg);
        } else {
            errors.append("Last name is invalid \n");
            lastName.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        }

        if (passwrd.isEmpty()) {
            errors.append("Missing Password\n");
            password.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        } else if (isValidPassword(passwrd)) {
            newUser.setPassword(passwrd);
            password.setBackgroundResource(R.drawable.normalbg);
        } else {
            errors.append("Invalid Password\n");
            password.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        }

        if (confirmpswd.isEmpty()) {
            errors.append("Missing Password confirm\n");
            confirmPassword.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        } else if (!confirmpswd.equals(passwrd)) {
            errors.append("Passwords don't match\n");
            confirmPassword.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        } else {
            confirmPassword.setBackgroundResource(R.drawable.normalbg);
        }

        if (!hasErrors) {
            try {
                DataBaseHelper dataBaseHelper = new DataBaseHelper(SignUpActivity.this, "project1", null, 1);
                errors.setText(dataBaseHelper.addUser(newUser));
            } catch (Exception e) {
                errors.append("Error while setting user data: " + e.getMessage() + "\n");
            }
        }
    }

    public boolean isValidName(String name) {
        return name.length() >= 5 && name.length() <= 20;
    }

    public boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,12}$";
        return password.length() >= 6 && password.length() <= 12 && password.matches(passwordPattern);
    }
}
