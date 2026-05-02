package com.example.smarthomecontrolapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;
    private EditText etRate, etTarget;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvUserName);
        tvEmail = view.findViewById(R.id.tvUserEmail);
        etRate = view.findViewById(R.id.etRate);
        etTarget = view.findViewById(R.id.etTarget);
        View btnBack = view.findViewById(R.id.btnBack);
        View btnSave = view.findViewById(R.id.btnSave);
        View btnLogout = view.findViewById(R.id.btnLogout);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            loadUserData();
        }

        btnSave.setOnClickListener(v -> saveSettings());
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void loadUserData() {
        if (userRef == null) return;
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        if (user.getName() != null) tvName.setText(user.getName());
                        etRate.setText(String.valueOf(user.getElectricityRate()));
                        etTarget.setText(String.valueOf(user.getSavingsTarget()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error loading profile data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveSettings() {
        if (userRef == null) return;

        String rateStr = etRate.getText().toString();
        String targetStr = etTarget.getText().toString();

        if (rateStr.isEmpty() || targetStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double rate = Double.parseDouble(rateStr);
            double target = Double.parseDouble(targetStr);

            userRef.child("electricityRate").setValue(rate);
            userRef.child("savingsTarget").setValue(target)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Settings Saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save settings", Toast.LENGTH_SHORT).show());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
