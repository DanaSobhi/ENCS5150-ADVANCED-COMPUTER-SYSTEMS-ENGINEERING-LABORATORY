package com.example.finalproject;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class ProfileFragment extends Fragment {

    private TextView tvFullNameValue, tvEmailValue, tvPasswordValue;
    private EditText etEditEmail, etEditPassword;
    private Button btnEditEmail, btnEditPassword, btnShowEditPassword, btnShowPassword;
    private String passwordValue;
    private DataBaseHelper dbHelper;
    private LinearLayout passwordSectionLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvFullNameValue = view.findViewById(R.id.tvFullNameValue);
        tvEmailValue = view.findViewById(R.id.tvEmailValue);
        tvPasswordValue = view.findViewById(R.id.tvPasswordValue);

        etEditEmail = view.findViewById(R.id.etEditEmail);
        etEditPassword = view.findViewById(R.id.etEditPassword);

        btnEditEmail = view.findViewById(R.id.btnEditEmail);
        btnEditPassword = view.findViewById(R.id.btnEditPassword);
        btnShowEditPassword = view.findViewById(R.id.btnShowEditPassword);
        btnShowPassword = view.findViewById(R.id.btnShowPassword);
        passwordSectionLayout = view.findViewById(R.id.passwordSection);
        Drawable originalBackground = etEditEmail.getBackground();

        loadUserData();

        btnEditEmail.setOnClickListener(v -> {
            if (etEditEmail.getVisibility() == View.GONE) {
                etEditEmail.setVisibility(View.VISIBLE);
                etEditEmail.setText(tvEmailValue.getText().toString());
            }
            else{
                int state = updateEmail(etEditEmail, tvEmailValue);
                switch (state) {
                    case 0:
                        etEditEmail.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        etEditEmail.setVisibility(View.GONE);
                        break;
                    case 2:
                        etEditEmail.setVisibility(View.VISIBLE);
                        etEditEmail.setBackgroundResource(R.drawable.errorbg);
                        break;
                    case 3:
                        etEditEmail.setVisibility(View.VISIBLE);
                        etEditEmail.setText(tvEmailValue.getText().toString());
                        etEditEmail.setBackground(originalBackground);
                        break;
                }
            }
        });

        btnEditPassword.setOnClickListener(v -> {
            if (passwordSectionLayout.getVisibility() == View.GONE) {
                passwordSectionLayout.setVisibility(View.VISIBLE);
                etEditPassword.setText("");
            } else {
                int state = updatePassword(etEditPassword, tvPasswordValue);
                switch (state) {
                    case 1:
                        passwordSectionLayout.setVisibility(View.GONE);
                        etEditPassword.setBackground(originalBackground);
                        break;
                    case 2:
                        passwordSectionLayout.setVisibility(View.VISIBLE);
                        etEditPassword.setBackgroundResource(R.drawable.errorbg);
                        break;
                    case 3:
                        passwordSectionLayout.setVisibility(View.VISIBLE);
                        etEditPassword.setText(passwordValue);
                        etEditPassword.setBackground(originalBackground);
                        break;
                }
            }

        });

        btnShowPassword.setOnClickListener(v -> {
            if (tvPasswordValue.getText().toString().equals("********")) {
                tvPasswordValue.setText(passwordValue); // Retrieve and show the real password
                btnShowPassword.setText("Hide");
            } else {
                tvPasswordValue.setText("********");
                btnShowPassword.setText("Show");
            }
        });
        btnShowEditPassword.setOnClickListener(view1 -> {
            if (etEditPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                etEditPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnShowEditPassword.setText("Show");
            } else {
                etEditPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnShowEditPassword.setText("Hide");
            }
        });

        ImageView ivBackArrow = view.findViewById(R.id.iv_back_arrow);
        ivBackArrow.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });
        return view;
    }

    private void loadUserData() {
        String email = SharedPrefManager.getInstance(requireContext()).readString("logged_in_user", null);
        dbHelper = new DataBaseHelper(getContext(), "project1", null, 1);
        if (email != null) {
            Cursor cursor = dbHelper.getUserData(email);
            if (cursor.moveToFirst()) {
                String emailValue = cursor.getString(0);
                passwordValue = cursor.getString(1);
                String fullName = cursor.getString(2) + " " + cursor.getString(3);
                tvFullNameValue.setText(fullName);
                tvEmailValue.setText(emailValue);
                tvPasswordValue.setText("********");

            }
            cursor.close();
        }
    }

    private int updateEmail(EditText etEditEmail, TextView tvEmailValue) {
        if (etEditEmail.getVisibility() == View.GONE) {
            etEditEmail.setVisibility(View.VISIBLE);
            etEditEmail.setText(tvEmailValue.getText().toString());
        } else {
            String newEmail = etEditEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(newEmail)) {
                String currentEmail = SharedPrefManager.getInstance(getContext()).readString("logged_in_user", null);
                if (currentEmail != null) {
                    if(newEmail.matches("(?i).*@.*\\.com")) {
                        boolean success = dbHelper.updateUserField(currentEmail, "EMAIL", newEmail);
                        if (success) {
                            tvEmailValue.setText(newEmail);
                            SharedPrefManager.getInstance(getContext()).writeString("logged_in_user", newEmail);
                            Toast.makeText(getContext(), "Email updated successfully.", Toast.LENGTH_SHORT).show();
                            etEditEmail.setBackgroundResource(0);
                            etEditEmail.setVisibility(View.GONE);
                            return 1;
                        } else {
                            Toast.makeText(getContext(), "Failed to update email.", Toast.LENGTH_SHORT).show();
                            return 1;
                        }
                    }
                    else {
                        Toast.makeText(getContext(), "Wrong email input.", Toast.LENGTH_SHORT).show();
                        return 2;
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Email cannot be empty.", Toast.LENGTH_SHORT).show();
                etEditEmail.setBackgroundResource(R.drawable.errorbg);
                return 3;
            }

        }
        return 0;
    }

    private int updatePassword(EditText etEditPassword, TextView tvPasswordValue) {
        String newPassword = etEditPassword.getText().toString().trim();
        if (!TextUtils.isEmpty(newPassword)) {
            String currentEmail = SharedPrefManager.getInstance(requireContext()).readString("logged_in_user", null);

            boolean success = dbHelper.updateUserField(currentEmail, "PASSWORD", newPassword);
            if (success) {
                tvPasswordValue.setText("********");
                Toast.makeText(requireContext(), "Password updated successfully.", Toast.LENGTH_SHORT).show();
                loadUserData();
                return 1;
            } else {
                Toast.makeText(requireContext(), "Failed to update password.", Toast.LENGTH_SHORT).show();
                return 2;
            }

        } else {
            Toast.makeText(requireContext(), "Password cannot be empty.", Toast.LENGTH_SHORT).show();
            etEditPassword.setBackgroundResource(R.drawable.errorbg);
            return 3;
        }
    }
}
