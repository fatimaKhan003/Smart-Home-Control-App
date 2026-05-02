package com.example.smarthomecontrolapp;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeFragment extends Fragment {

    RecyclerView recyclerDevices;
    ArrayList<Device> list;
    DeviceAdapter adapter;
    TextView tvWelcome, tvExpense;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference userRef, deviceRef;
    ValueEventListener devicesListener;
    String currentRoom = "Living Room";
    private double userRate = 0.12; // Default

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerDevices = view.findViewById(R.id.recyclerDevices);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvExpense = view.findViewById(R.id.tvExpense);
        View fabAdd = view.findViewById(R.id.fabAddDevice);
        View profileCircle = view.findViewById(R.id.profileCircle);

        fabAdd.setOnClickListener(v -> showAddDeviceDialog());
        profileCircle.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        list = new ArrayList<>();
        adapter = new DeviceAdapter(getContext(), list);
        recyclerDevices.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerDevices.setAdapter(adapter);

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            deviceRef = userRef.child("devices");
            loadUserData();
            setupRoomTabs(view);
        }

        return view;
    }

    private void setupRoomTabs(View view) {
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutRooms);
        String[] rooms = {"Living Room", "Bedroom", "Kitchen", "Washroom", "Drawing Room", "Dining Room", "TV Lounge"};
        for (String room : rooms) {
            tabLayout.addTab(tabLayout.newTab().setText(room));
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentRoom = tab.getText().toString();
                loadDevices();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        loadDevices();
    }

    private void loadDevices() {
        if (devicesListener != null) {
            deviceRef.removeEventListener(devicesListener);
        }

        devicesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                HashMap<String, Device> groupMap = new HashMap<>();
                double totalConsumptionKWh = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Device device = ds.getValue(Device.class);
                    if (device != null) {
                        if (device.getRoomId().equalsIgnoreCase(currentRoom)) {
                            String category = device.getType();
                            if (groupMap.containsKey(category)) {
                                Device existing = groupMap.get(category);
                                existing.setCount(existing.getCount() + 1);
                                if (device.isStatus()) existing.setStatus(true);
                            } else {
                                device.setCount(1);
                                groupMap.put(category, device);
                            }
                        }
                        // Sum up all devices' energy consumption (in kWh)
                        totalConsumptionKWh += device.calculateExpenditure(1.0);
                    }
                }
                list.addAll(groupMap.values());
                adapter.notifyDataSetChanged();
                updateExpense(totalConsumptionKWh);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        deviceRef.addValueEventListener(devicesListener);
    }

    private void updateExpense(double totalConsumptionKWh) {
        double totalCost = totalConsumptionKWh * userRate;
        tvExpense.setText(String.format(java.util.Locale.US, "$ %.2f", totalCost));
    }

    private void showAddDeviceDialog() {
        String[] categories = DeviceType.getAllDisplayNames();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Device Category");
        builder.setItems(categories, ((dialog, which) -> {
            String selectedCategory = categories[which];
            showRoomSelectionDialog(selectedCategory);
        }));
        builder.show();
    }

    private void showRoomSelectionDialog(String category) {
        String[] rooms = {"Living Room", "Bedroom", "Drawing Room", "Dining Room", "TV Lounge", "Kitchen", "Washroom"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Room");
        builder.setItems(rooms, (dialog, which) -> {
            String selectedRoom = rooms[which];
            addDeviceToFirebase(category, selectedRoom);
        });
        builder.show();
    }

    private void addDeviceToFirebase(String category, String room) {
        String id = deviceRef.push().getKey();
        double power = 0;
        if (category.equals("Air Condition")) power = 1.5;
        else if (category.equals("Smart Fridge")) power = 0.5;
        else if (category.equals("Smart TV")) power = 0.2;
        else power = 0.05;

        Device newDevice = new Device(id, room, category, category, false, power, 1);
        if (id != null) {
            deviceRef.child(id).setValue(newDevice);
        }
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null) tvWelcome.setText("Welcome " + name);
                    
                    Double rate = snapshot.child("electricityRate").getValue(Double.class);
                    if (rate != null && rate > 0) {
                        userRate = rate;
                    }
                    // Refresh expenses if rate changed
                    loadDevices();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (deviceRef != null && devicesListener != null) {
            deviceRef.removeEventListener(devicesListener);
        }
    }
}
