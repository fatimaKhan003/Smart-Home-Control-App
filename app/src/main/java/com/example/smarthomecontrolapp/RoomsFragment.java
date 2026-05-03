package com.example.smarthomecontrolapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoomsFragment extends Fragment {

    RecyclerView recyclerRooms;
    RoomAdapter roomAdapter;
    List<Room> roomList;

    public RoomsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);


        ImageView ivMenu = view.findViewById(R.id.ivMenu);
        ivMenu.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });


        View profileCircle = view.findViewById(R.id.profileCircle);
        profileCircle.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(null)
                        .commit()
        );

        recyclerRooms = view.findViewById(R.id.recyclerRooms);
        roomList = new ArrayList<>();
        roomAdapter = new RoomAdapter(getContext(), roomList);
        recyclerRooms.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerRooms.setAdapter(roomAdapter);

        loadRooms();

        return view;
    }

    private void loadRooms() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("devices")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        roomList.clear();


                        String[] defaultRooms = {"Living Room", "Bedroom", "Kitchen",
                                "Washroom", "Drawing Room", "Dining Room", "TV Lounge"};

                        List<String> allRoomNames = new ArrayList<>();
                        for (String r : defaultRooms) allRoomNames.add(r);


                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Device device = ds.getValue(Device.class);
                            if (device != null && device.getRoomId() != null) {
                                if (!allRoomNames.contains(device.getRoomId())) {
                                    allRoomNames.add(device.getRoomId());
                                }
                            }
                        }


                        for (String roomName : allRoomNames) {
                            int count = 0;
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Device device = ds.getValue(Device.class);
                                if (device != null && device.getRoomId() != null
                                        && device.getRoomId().equalsIgnoreCase(roomName)) {
                                    count++;
                                }
                            }
                            roomList.add(new Room("1", roomName, 20.0, count));
                        }

                        roomAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}