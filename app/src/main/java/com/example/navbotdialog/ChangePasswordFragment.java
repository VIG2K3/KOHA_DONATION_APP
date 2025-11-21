package com.example.navbotdialog;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;

public class ChangePasswordFragment extends Fragment {

    private EditText etCurrentPass, etNewPass, etConfirmPass;
    private Button btnChangePassword;
    private ImageView ivToggleCurrent, ivToggleNew, ivToggleConfirm;
    private boolean isCurrentVisible = false, isNewVisible = false, isConfirmVisible = false;

    public ChangePasswordFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        etCurrentPass = view.findViewById(R.id.etCurrentPass);
        etNewPass = view.findViewById(R.id.etNewPass);
        etConfirmPass = view.findViewById(R.id.etConfirmPass);

        ivToggleCurrent = view.findViewById(R.id.ivToggleCurrent);
        ivToggleNew = view.findViewById(R.id.ivToggleNew);
        ivToggleConfirm = view.findViewById(R.id.ivToggleConfirm);

        btnChangePassword = view.findViewById(R.id.btnChangePassword);

        // Toggle password visibility
        ivToggleCurrent.setOnClickListener(v -> togglePasswordVisibility(etCurrentPass, ivToggleCurrent, "current"));
        ivToggleNew.setOnClickListener(v -> togglePasswordVisibility(etNewPass, ivToggleNew, "new"));
        ivToggleConfirm.setOnClickListener(v -> togglePasswordVisibility(etConfirmPass, ivToggleConfirm, "confirm"));

        // Change password logic
        btnChangePassword.setOnClickListener(v -> changePassword());

        return view;
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggle, String field) {
        boolean isVisible;
        if (field.equals("current")) isVisible = isCurrentVisible;
        else if (field.equals("new")) isVisible = isNewVisible;
        else isVisible = isConfirmVisible;

        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggle.setImageResource(R.drawable.eye_close);
            if (field.equals("current")) isCurrentVisible = false;
            else if (field.equals("new")) isNewVisible = false;
            else isConfirmVisible = false;
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggle.setImageResource(R.drawable.eye_open);
            if (field.equals("current")) isCurrentVisible = true;
            else if (field.equals("new")) isNewVisible = true;
            else isConfirmVisible = true;
        }
        editText.setSelection(editText.getText().length());
    }

    private void changePassword() {
        String current = etCurrentPass.getText().toString().trim();
        String newPass = etNewPass.getText().toString().trim();
        String confirm = etConfirmPass.getText().toString().trim();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirm)) {
            Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Re-authenticate user
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), current);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update password
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(getActivity(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                        etCurrentPass.setText("");
                        etNewPass.setText("");
                        etConfirmPass.setText("");
                    } else {
                        Toast.makeText(getActivity(), "Failed to change password", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getActivity(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
