package com.example.smarthomecontrolapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smarthomecontrolapp.models.Device;
import com.example.smarthomecontrolapp.adapters.DeviceAdapter;
import com.example.smarthomecontrolapp.viewmodel.EnergyViewModel;
import com.example.smarthomecontrolapp.models.LogEntry;
import com.example.smarthomecontrolapp.R;
import com.example.smarthomecontrolapp.activities.MainActivity;
import com.example.smarthomecontrolapp.utils.DeviceType;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerDevices;
    private ArrayList<Device> list;
    private DeviceAdapter adapter;
    private TextView tvWelcome, tvExpense, tvMaxExpense;
    private ProgressBar expenseProgress;
    private View homeContent;
    private ProgressBar loadingSpinner;
    
    private EnergyViewModel viewModel;
    private String currentRoom = "Living Room";
    private double userRate = 0.12;
    private double savingsTarget = 500.0;
    
    private List<Device> allDevices = new ArrayList<>();
    private List<LogEntry> allLogs = new ArrayList<>();

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        homeContent = view.findViewById(R.id.homeContent);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        recyclerDevices = view.findViewById(R.id.recyclerDevices);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvMaxExpense = view.findViewById(R.id.tvMaxExpense);
        expenseProgress = view.findViewById(R.id.expenseProgress);
        
        View fabAdd = view.findViewById(R.id.fabAddDevice);
        View profileCircle = view.findViewById(R.id.profileCircle);
        ImageView ivMenu=view.findViewById(R.id.ivMenu);
        ivMenu.setOnClickListener(v->
        {
            if(getActivity() instanceof MainActivity)
            {
                ((MainActivity)getActivity()).openDrawer();
            }
        });
        fabAdd.setOnClickListener(v->showAddDeviceDialog());
        profileCircle.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        list = new ArrayList<>();
        adapter = new DeviceAdapter(getContext(), list);
        recyclerDevices.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerDevices.setAdapter(adapter);

        setupRoomTabs(view);
        setupViewModel();

        return view;
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(EnergyViewModel.class);


        if (viewModel.getDevices().getValue() != null && !viewModel.getDevices().getValue().isEmpty()) {
            allDevices.clear();
            allDevices.addAll(viewModel.getDevices().getValue());
            if (viewModel.getLogs().getValue() != null) {
                allLogs.clear();
                allLogs.addAll(viewModel.getLogs().getValue());
            }
            loadingSpinner.setVisibility(View.GONE);
            homeContent.setVisibility(View.VISIBLE);
            refreshUI();
        }

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading && allDevices.isEmpty()) {
                loadingSpinner.setVisibility(View.VISIBLE);
                homeContent.setVisibility(View.GONE);
            } else {
                loadingSpinner.setVisibility(View.GONE);
                homeContent.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                if (user.getName() != null) tvWelcome.setText("Welcome " + user.getName());
                userRate = user.getElectricityRate() > 0 ? user.getElectricityRate() : 0.12;
                savingsTarget = user.getSavingsTarget() > 0 ? user.getSavingsTarget() : 500.0;
                calculateAndDisplayExpenses();
            }
        });

        viewModel.getDevices().observe(getViewLifecycleOwner(), devices -> {
            allDevices.clear();
            allDevices.addAll(devices);
            refreshUI();
        });

        viewModel.getLogs().observe(getViewLifecycleOwner(), logs -> {
            allLogs.clear();
            allLogs.addAll(logs);
            calculateAndDisplayExpenses();
        });
    }

    private void setupRoomTabs(View view) {
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutRooms);

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("devices")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {

                        List<String> roomNames = new ArrayList<>();


                        String[] defaultRooms = {"Living Room", "Bedroom", "Kitchen",
                                "Washroom", "Drawing Room", "Dining Room", "TV Lounge"};
                        for (String r : defaultRooms) roomNames.add(r);


                        for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                            Device device = ds.getValue(Device.class);
                            if (device != null && device.getRoomId() != null) {
                                if (!roomNames.contains(device.getRoomId())) {
                                    roomNames.add(device.getRoomId());
                                }
                            }
                        }


                        tabLayout.removeAllTabs();
                        for (String room : roomNames) {
                            tabLayout.addTab(tabLayout.newTab().setText(room));
                        }


                        for (int i = 0; i < tabLayout.getTabCount(); i++) {
                            TabLayout.Tab tab = tabLayout.getTabAt(i);
                            if (tab != null && currentRoom.equals(tab.getText())) {
                                tab.select();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentRoom = tab.getText().toString();
                refreshGridOnly();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void refreshUI() {
        refreshGridOnly();
        calculateAndDisplayExpenses();
    }

    private void refreshGridOnly() {
        list.clear();
        HashMap<String, Device> groupMap = new HashMap<>();
        for (Device device : allDevices) {
            if (device.getRoomId() != null && device.getRoomId().equalsIgnoreCase(currentRoom)) {
                String category = device.getType();
                if (groupMap.containsKey(category)) {
                    Device existing = groupMap.get(category);
                    existing.setCount(existing.getCount() + 1);
                    if (device.isStatus()) existing.setStatus(true);
                } else {
                    Device copy = new Device(device.getDeviceId(), device.getRoomId(), device.getDeviceName(), 
                                           device.getType(), device.isStatus(), device.getPowerConsumption(), 1);
                    groupMap.put(category, copy);
                }
            }
        }
        list.addAll(groupMap.values());
        adapter.notifyDataSetChanged();
    }

    private void calculateAndDisplayExpenses() {
        Map<String, Long> lastOnTimestamp = new HashMap<>();
        Map<String, Long> deviceTotalTimeMillis = new HashMap<>();

        List<LogEntry> sortedLogs = new ArrayList<>(allLogs);
        Collections.sort(sortedLogs, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

        for (LogEntry log : sortedLogs) {
            String dId = log.getDeviceId();
            if ("TURN_ON".equals(log.getAction())) {
                lastOnTimestamp.put(dId, log.getTimestamp());
            } else if ("TURN_OFF".equals(log.getAction())) {
                if (lastOnTimestamp.containsKey(dId)) {
                    long duration = log.getTimestamp() - lastOnTimestamp.get(dId);
                    deviceTotalTimeMillis.put(dId, deviceTotalTimeMillis.getOrDefault(dId, 0L) + duration);
                    lastOnTimestamp.remove(dId);
                }
            }
        }

        double totalSpent = 0;
        long now = System.currentTimeMillis();
        for (Device device : allDevices) {
            long totalDuration = deviceTotalTimeMillis.getOrDefault(device.getDeviceId(), 0L);
            if (device.isStatus()) {
                long sessionStart = lastOnTimestamp.containsKey(device.getDeviceId()) 
                        ? lastOnTimestamp.get(device.getDeviceId()) 
                        : device.getLastStatusChangeTimestamp();
                if (sessionStart > 0 && sessionStart < now) {
                    totalDuration += (now - sessionStart);
                }
            }
            double hours = totalDuration / (1000.0 * 60.0 * 60.0);
            

            int count = device.getCount() > 0 ? device.getCount() : 1;
            totalSpent += (hours * device.getPowerConsumption() * userRate * count);
        }

        tvExpense.setVisibility(View.VISIBLE);
        tvExpense.setText(String.format(Locale.US, "$ %.2f", totalSpent));
        tvMaxExpense.setText(String.format(Locale.US, "$%.2f", savingsTarget));

        int precision = 10000;
        int progress = (savingsTarget > 0) ? (int) ((totalSpent / savingsTarget) * precision) : 0;
        expenseProgress.setMax(precision);
        expenseProgress.setProgress(Math.min(progress, precision));
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
        String[] fixedRooms = {"Living Room", "Bedroom", "Drawing Room", "Dining Room",
                "TV Lounge", "Kitchen", "Washroom"};


        List<String> roomOptions = new ArrayList<>();
        Collections.addAll(roomOptions, fixedRooms);


        for (Device d : allDevices) {
            if (d.getRoomId() != null && !roomOptions.contains(d.getRoomId())) {
                roomOptions.add(d.getRoomId());
            }
        }


        roomOptions.add("+ Add New Room");

        String[] roomArray = roomOptions.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Room");
        builder.setItems(roomArray, (dialog, which) -> {
            String selected = roomArray[which];
            if (selected.equals("+ Add New Room")) {
                showAddNewRoomDialog(category);
            } else {
                addDeviceToFirebase(category, selected);
            }
        });
        builder.show();
    }

    private void showAddNewRoomDialog(String category) {

        android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Enter room name");
        input.setPadding(50, 30, 50, 30);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        new AlertDialog.Builder(getContext())
                .setTitle("New Room")
                .setMessage("Enter the name of the new room:")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String newRoom = input.getText().toString().trim();

                    if (newRoom.isEmpty()) {
                        android.widget.Toast.makeText(getContext(),
                                "Room name cannot be empty",
                                android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }


                    boolean alreadyExists = false;
                    String[] fixedRooms = {"Living Room", "Bedroom", "Drawing Room",
                            "Dining Room", "TV Lounge", "Kitchen", "Washroom"};
                    for (String r : fixedRooms) {
                        if (r.equalsIgnoreCase(newRoom)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (!alreadyExists) {
                        for (Device d : allDevices) {
                            if (d.getRoomId() != null && d.getRoomId().equalsIgnoreCase(newRoom)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                    }

                    if (alreadyExists) {

                        android.widget.Toast.makeText(getContext(),
                                "Room already exists. Adding device to " + newRoom,
                                android.widget.Toast.LENGTH_SHORT).show();
                        addDeviceToFirebase(category, newRoom);
                    } else {

                        addDeviceToFirebase(category, newRoom);
                        android.widget.Toast.makeText(getContext(),
                                newRoom + " created!",
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addDeviceToFirebase(String category, String room) {
        showWattsInputDialog(category,room);
    }
    private void showWattsInputDialog(String category, String room) {

        String defaultWatts;
        if (category.equals("Air Condition"))       defaultWatts = "1500";
        else if (category.equals("Smart Fridge"))   defaultWatts = "500";
        else if (category.equals("Smart TV"))       defaultWatts = "200";
        else if (category.equals("Lighting"))       defaultWatts = "60";
        else if (category.equals("Blinds"))         defaultWatts = "50";
        else                                         defaultWatts = "100";


        android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Enter watts (e.g. 1500)");
        input.setText(defaultWatts);
        input.setSelectAllOnFocus(true);
        input.setPadding(50, 30, 50, 30);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(getContext())
                .setTitle("Power Consumption")
                .setMessage("Enter the wattage for " + category + "\n(suggested: " + defaultWatts + "W)")
                .setView(input)
                .setPositiveButton("Add Device", (dialog, which) -> {
                    String wattsStr = input.getText().toString().trim();

                    if (wattsStr.isEmpty()) {
                        android.widget.Toast.makeText(getContext(),
                                "Please enter wattage",
                                android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double watts;
                    try {
                        watts = Double.parseDouble(wattsStr);
                    } catch (NumberFormatException e) {
                        android.widget.Toast.makeText(getContext(),
                                "Invalid wattage entered",
                                android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (watts <= 0) {
                        android.widget.Toast.makeText(getContext(),
                                "Wattage must be greater than 0",
                                android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }


                    double powerInKwh = watts / 1000.0;


                    String uid = FirebaseAuth.getInstance().getUid();
                    if (uid == null) return;
                    DatabaseReference dRef = FirebaseDatabase.getInstance()
                            .getReference("users").child(uid).child("devices");
                    String id = dRef.push().getKey();

                    Device newDevice = new Device(id, room, category, category, false, powerInKwh, 1);
                    if (id != null) {
                        dRef.child(id).setValue(newDevice)
                                .addOnSuccessListener(aVoid ->
                                        android.widget.Toast.makeText(getContext(),
                                                category + " added to " + room,
                                                android.widget.Toast.LENGTH_SHORT).show()
                                );
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
