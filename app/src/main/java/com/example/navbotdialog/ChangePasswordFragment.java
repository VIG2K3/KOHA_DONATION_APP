package com.example.navbotdialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class ChangePasswordFragment extends Fragment {

    private EditText etCurrentPass, etNewPass, etConfirmPass;
    private Button btnChangePassword;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        etCurrentPass = view.findViewById(R.id.etCurrentPass);
        etNewPass = view.findViewById(R.id.etNewPass);
        etConfirmPass = view.findViewById(R.id.etConfirmPass);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);

        btnChangePassword.setOnClickListener(v -> {
            String current = etCurrentPass.getText().toString();
            String newPass = etNewPass.getText().toString();
            String confirm = etConfirmPass.getText().toString();

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirm)) {
                Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getActivity(), "Password changed successfully", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
