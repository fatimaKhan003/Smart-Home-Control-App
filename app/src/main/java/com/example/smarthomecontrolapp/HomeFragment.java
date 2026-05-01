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

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    RecyclerView recyclerDevices;
    ArrayList<Device> list;
    DeviceAdapter adapter;
    TextView tvWelcome, tvExpense;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference userRef, deviceRef;
    public HomeFragment()
    {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);
        recyclerDevices=view.findViewById(R.id.recyclerDevices);
        tvWelcome=view.findViewById(R.id.tvWelcome);
        tvExpense=view.findViewById(R.id.tvExpense);
        View fabAdd=view.findViewById(R.id.fabAddDevice);
        fabAdd.setOnClickListener(v->showAddDeviceDialog());
        mAuth= FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        list=new ArrayList<>();
        adapter=new DeviceAdapter(getContext(),list);
        recyclerDevices.setLayoutManager(new GridLayoutManager(getContext(),2));
        recyclerDevices.setAdapter(adapter);
        if(currentUser!=null)
        {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            deviceRef=FirebaseDatabase.getInstance().getReference("Devices");
            loadUserData();
            loadDevices();

        }


        return view;
    }

    private void showAddDeviceDialog() {
        String[] categories={"Smart TV", "Smart Fridge", "Lights", "ACs"};
        AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
        builder.setTitle("Select Device Category");
        builder.setItems(categories,((dialog, which) -> {
            String selectedCategory=categories[which];
            showRoomSelectionDialog(selectedCategory);
        }));
        builder.show();
    }

    private void showRoomSelectionDialog(String category) {
        String[] rooms = {"Bedroom", "Drawing Room", "Dining Room", "TV Lounge", "Kitchen", "Washroom"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Room");
        builder.setItems(rooms, (dialog, which) -> {
            String selectedRoom = rooms[which];
            addDeviceToFirebase(category, selectedRoom);
        });
        builder.show();
    }

    private void addDeviceToFirebase(String category, String room) {
        String id=deviceRef.push().getKey();
        double power=0;
        if(category.equals("ACs")) power=1.5;
        else if(category.equals("Smart Fridge"))power=0.5;
        else if(category.equals("Smart TV"))power=0.2;
        else power=0.05;
        Device newDevice= new Device(id,room,category,category,false,power);
        if(id!=null)
        {
            deviceRef.child(id).setValue(newDevice).addOnSuccessListener(aVoid->
            {

            });
        }
    }

    private void loadUserData() {
       userRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(snapshot.exists())
               {
                   String name=snapshot.child("name").getValue(String.class);
                   tvWelcome.setText("Welcome "+name);
               }
           }


           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }
    private void loadDevices()
    {
        deviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                double totalPower=0;
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    Device device=ds.getValue(Device.class);
                    if(device!=null)
                    {
                        list.add(device);
                        if(device.isStatus())
                        {
                            totalPower+=device.getPowerConsumption();
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                updateExpense(totalPower);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void updateExpense(double totalPower) {
        double estimatedBill = totalPower * 0.12;
        tvExpense.setText("$ "+estimatedBill);
    }

}