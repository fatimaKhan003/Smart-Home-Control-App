package com.example.smarthomecontrolapp.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smarthomecontrolapp.models.Device;
import com.example.smarthomecontrolapp.models.LogEntry;
import com.example.smarthomecontrolapp.models.Room;
import com.example.smarthomecontrolapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EnergyViewModel extends ViewModel {
    private final MutableLiveData<List<Device>> devices = new MutableLiveData<>();
    private final MutableLiveData<List<LogEntry>> logs = new MutableLiveData<>();
    private final MutableLiveData<List<Room>> rooms = new MutableLiveData<>();
    private final MutableLiveData<User> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private DatabaseReference userRef;
    private ValueEventListener userListener;

    public EnergyViewModel() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            startListening();
        }
    }

    private void startListening() {
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userProfile.setValue(snapshot.getValue(User.class));
                
                List<Device> deviceList = new ArrayList<>();
                DataSnapshot dsDevices = snapshot.child("devices");
                for (DataSnapshot child : dsDevices.getChildren()) {
                    Device device = child.getValue(Device.class);
                    if (device != null) deviceList.add(device);
                }
                devices.setValue(deviceList);

                List<LogEntry> logList = new ArrayList<>();
                DataSnapshot dsLogs = snapshot.child("logs");
                for (DataSnapshot child : dsLogs.getChildren()) {
                    LogEntry log = child.getValue(LogEntry.class);
                    if (log != null) logList.add(log);
                }
                logs.setValue(logList);

                List<Room> roomList = new ArrayList<>();
                DataSnapshot dsRooms = snapshot.child("rooms");
                for (DataSnapshot child : dsRooms.getChildren()) {
                    Room room = child.getValue(Room.class);
                    if (room != null) roomList.add(room);
                }
                rooms.setValue(roomList);
                
                isLoading.setValue(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoading.setValue(false);
            }
        };
        userRef.addValueEventListener(userListener);
    }

    public LiveData<List<Device>> getDevices() { return devices; }
    public LiveData<List<LogEntry>> getLogs() { return logs; }
    public LiveData<List<Room>> getRooms() { return rooms; }
    public LiveData<User> getUserProfile() { return userProfile; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
    }
}
