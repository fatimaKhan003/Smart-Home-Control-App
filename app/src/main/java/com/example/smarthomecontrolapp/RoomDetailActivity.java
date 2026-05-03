package com.example.smarthomecontrolapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoomDetailActivity extends AppCompatActivity {

    TextView tvRoomTitle, tvTemperature, tvTodayCost;
    RecyclerView recyclerRoomDevices;
    DeviceAdapter deviceAdapter;
    ArrayList<Device> deviceList;
    DatabaseReference deviceRef;
    String roomName;
    MaterialButton historybtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);


        roomName = getIntent().getStringExtra("roomName");
        if(roomName==null)
        {
            roomName="UnKnown Room";
        }

        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvTodayCost = findViewById(R.id.tvTodayCost);
        recyclerRoomDevices = findViewById(R.id.recyclerRoomDevices);



        tvRoomTitle.setText(roomName);


        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        historybtn=findViewById(R.id.btnHistory);
        historybtn.setOnClickListener(v->
        {
            DetailsFragment fragment = new DetailsFragment();
            Bundle args=new Bundle();
            args.putString("roomName",roomName);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment).commit();
        });


        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        recyclerRoomDevices.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerRoomDevices.setAdapter(deviceAdapter);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            deviceRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("devices");

            loadDevicesForRoom();
        }
    }

    private void loadDevicesForRoom() {
        deviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deviceList.clear();
                double totalPower = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Device device = ds.getValue(Device.class);

                        if (device != null && device.getRoomId() != null && roomName != null) {

                            if (roomName.equalsIgnoreCase(device.getRoomId())) {
                                deviceList.add(device);

                                if (device.isStatus()) {
                                    totalPower += device.getPowerConsumption();
                                }
                            }
                        }} catch (Exception e) {
                        Log.e("FirebaseError", "Error parsing device: " + e.getMessage());
                    }
                }

                deviceAdapter.notifyDataSetChanged();

                // Update cost display
                double cost = totalPower * 0.12;
                tvTodayCost.setText(String.format("$%.2f", cost));

                
                tvRoomTitle.setText(roomName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}