package com.example.smarthomecontrolapp;

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

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;
    private EditText etRate, etTargetTV, etTargetFridge, etTargetLighting, etTargetAC, etTargetBlinds, etTargetMusic;
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
        
        etTargetTV = view.findViewById(R.id.etTargetTV);
        etTargetFridge = view.findViewById(R.id.etTargetFridge);
        etTargetLighting = view.findViewById(R.id.etTargetLighting);
        etTargetAC = view.findViewById(R.id.etTargetAC);
        etTargetBlinds = view.findViewById(R.id.etTargetBlinds);
        etTargetMusic = view.findViewById(R.id.etTargetMusic);

        View btnBack = view.findViewById(R.id.btnBack);
        View btnSave = view.findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            loadUserData();
        }

        btnSave.setOnClickListener(v -> saveSettings());
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

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
                        
                        Map<String, Double> targets = user.getDeviceSavingsTargets();
                        etTargetTV.setText(String.valueOf(targets.getOrDefault(DeviceType.SMART_TV.getDisplayName(), 50.0)));
                        etTargetFridge.setText(String.valueOf(targets.getOrDefault(DeviceType.SMART_FRIDGE.getDisplayName(), 50.0)));
                        etTargetLighting.setText(String.valueOf(targets.getOrDefault(DeviceType.LIGHTING.getDisplayName(), 50.0)));
                        etTargetAC.setText(String.valueOf(targets.getOrDefault(DeviceType.AIR_CONDITION.getDisplayName(), 50.0)));
                        etTargetBlinds.setText(String.valueOf(targets.getOrDefault(DeviceType.BLINDS.getDisplayName(), 50.0)));
                        etTargetMusic.setText(String.valueOf(targets.getOrDefault(DeviceType.MUSIC_SYSTEM.getDisplayName(), 50.0)));
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

        try {
            double rate = Double.parseDouble(etRate.getText().toString());
            
            double targetTV = Double.parseDouble(etTargetTV.getText().toString());
            double targetFridge = Double.parseDouble(etTargetFridge.getText().toString());
            double targetLighting = Double.parseDouble(etTargetLighting.getText().toString());
            double targetAC = Double.parseDouble(etTargetAC.getText().toString());
            double targetBlinds = Double.parseDouble(etTargetBlinds.getText().toString());
            double targetMusic = Double.parseDouble(etTargetMusic.getText().toString());

            Map<String, Double> targets = new HashMap<>();
            targets.put(DeviceType.SMART_TV.getDisplayName(), targetTV);
            targets.put(DeviceType.SMART_FRIDGE.getDisplayName(), targetFridge);
            targets.put(DeviceType.LIGHTING.getDisplayName(), targetLighting);
            targets.put(DeviceType.AIR_CONDITION.getDisplayName(), targetAC);
            targets.put(DeviceType.BLINDS.getDisplayName(), targetBlinds);
            targets.put(DeviceType.MUSIC_SYSTEM.getDisplayName(), targetMusic);

            double totalTarget = targetTV + targetFridge + targetLighting + targetAC + targetBlinds + targetMusic;

            userRef.child("electricityRate").setValue(rate);
            userRef.child("deviceSavingsTargets").setValue(targets);
            userRef.child("savingsTarget").setValue(totalTarget)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Settings Saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save settings", Toast.LENGTH_SHORT).show());
            
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
}
