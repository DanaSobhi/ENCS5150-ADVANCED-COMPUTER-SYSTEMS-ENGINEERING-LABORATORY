package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private TextView errors;
    private EditText emailAddress, password;
    private Button loginB, signupB;
    private CheckBox rememberme;
    private SharedPrefManager sharedPrefManager;
    private final String email =  "email";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        errors = findViewById(R.id.errorHandler);
        emailAddress = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        loginB = findViewById(R.id.loginButton);
        signupB = findViewById(R.id.signUpButton2);

        emailAddress.setBackgroundResource(R.drawable.normalbg);
        password.setBackgroundResource(R.drawable.normalbg);

        rememberme = findViewById(R.id.rememberBox);
        sharedPrefManager = SharedPrefManager.getInstance(this);


        String REMEMBER_ME = "remember";
        boolean istoRemember = sharedPrefManager.getBoolean(REMEMBER_ME,false);
        rememberme.setChecked(istoRemember);
        rememberme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPrefManager.setBoolean(REMEMBER_ME,rememberme.isChecked());
                setUserData(rememberme.isChecked());
                if (rememberme.isChecked()){
                    String rememerToast = "User email has been saved.";
                    Toast toast =Toast.makeText(MainActivity.this, rememerToast, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        if (istoRemember){
            emailAddress.setText(sharedPrefManager.readString(email,"No value"));
        }

        loginB.setOnClickListener(v -> {
            RegisterPlayer();
            String check = errors.getText().toString();
            if (check.equals("welcome")) {
                // Navigate to HomeActivity after successful login
                Toast toast =Toast.makeText(MainActivity.this, errors.getText().toString(), Toast.LENGTH_SHORT);
                toast.show();
                Intent intent = new Intent(MainActivity.this, HomeLayout.class);
               startActivity(intent);
                finish(); // Close the current activity (MainActivity)
            }
        });

        signupB.setOnClickListener(v -> {
            // Navigate to SignUpActivity
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish(); // Close the current activity (MainActivity)
        });

        Switch toggleDarkmode =(Switch) findViewById(R.id.darkmodeToggle);

        String DARK_MODE_KEY = "dark";

        boolean isDarkMode = sharedPrefManager.getBoolean(DARK_MODE_KEY,false);
        applyDarkMode(isDarkMode);
        toggleDarkmode.setChecked(isDarkMode);

        toggleDarkmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPrefManager.setDarkmode(DARK_MODE_KEY,toggleDarkmode.isChecked());
                applyDarkMode(toggleDarkmode.isChecked());

            }
        });

    }
    private void applyDarkMode(boolean enable){
        if (enable){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void RegisterPlayer() {
        Users newUser = new Users();
        String email = emailAddress.getText().toString();
        String passwrd = password.getText().toString();
        boolean hasErrors = false;

        errors.setText(""); // Clear previous error messages

        // Validate password
        if (passwrd.isEmpty()) {
            errors.append("Missing Password\n");
            password.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        } else {
            newUser.setPassword(passwrd);
            password.setBackgroundResource(R.drawable.normalbg);
        }

        // Validate email
        if (email.isEmpty()) {
            errors.append("Missing Email Address\n");
            emailAddress.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        } else if (email.matches("(?i).*@.*\\.com")) {  // Email pattern check
            newUser.setEmailAddress(email);
            emailAddress.setBackgroundResource(R.drawable.normalbg);
        } else {
            errors.append("Email is incorrect\n");
            emailAddress.setBackgroundResource(R.drawable.errorbg);
            hasErrors = true;
        }

        // If there are no errors, check user in database
        if (!hasErrors) {
            try {
                DataBaseHelper dataBaseHelper = new DataBaseHelper(MainActivity.this, "project1", null, 1);
                if (dataBaseHelper.checkUser(newUser.getEmailAddress(), newUser.getPassword())) {
                    errors.setText("welcome");
                } else {
                    errors.setText("Error: Email or password.");
                }
            } catch (Exception e) {
                errors.append("Error while setting user data: " + e.getMessage() + "\n");
            }
        }
    }

    private void setUserData(Boolean enable){
        if(enable){
            sharedPrefManager.writeString(email,emailAddress.getText().toString());
        }
    }
}
