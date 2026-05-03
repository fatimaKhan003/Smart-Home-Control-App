package com.example.smarthomecontrolapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EnergyFragment extends Fragment {

    private ArcProgress arcProgressBar;
    private ProgressBar savingsProgressBar, loadingSpinner;
    private View energyContent;
    private TextView tvTotalExpenses, tvBudgetStatus, tvMaxBudget, tvSavingsValue, tvSavingsTargetValue, tvSelectedRoom, tvCurrentDate;
    private RecyclerView rvEnergyDevices;
    private EnergyDeviceAdapter adapter;
    private List<EnergyDeviceAdapter.EnergyCategoryData> categoryDataList = new ArrayList<>();

    private String currentRoom = "Living Room";
    private double userRate = 0.12;
    private double profileSavingsTarget = 0;
    private User currentUserProfile;
    private List<Device> allDevices = new ArrayList<>();
    private List<LogEntry> allLogs = new ArrayList<>();
    private double totalBudget = 0;

    private EnergyViewModel viewModel;
    private final Handler refreshHandler = new Handler();
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            calculateAndDisplayEnergy();
            refreshHandler.postDelayed(this, 5000); 
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_energy, container, false);
        initViews(view);
        setupViewModel();

        tvCurrentDate.setText(new SimpleDateFormat("dd MMMM yyyy", Locale.US).format(new Date()));
        view.findViewById(R.id.roomFilterCard).setOnClickListener(this::showRoomFilterMenu);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        return view;
    }

    private void initViews(View view) {
        arcProgressBar = view.findViewById(R.id.arcProgressBar);
        savingsProgressBar = view.findViewById(R.id.savingsProgressBar);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        energyContent = view.findViewById(R.id.energyContent);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        tvBudgetStatus = view.findViewById(R.id.tvBudgetStatus);
        tvMaxBudget = view.findViewById(R.id.tvMaxBudget);
        tvSavingsValue = view.findViewById(R.id.tvSavingsValue);
        tvSavingsTargetValue = view.findViewById(R.id.tvSavingsTargetValue);
        tvSelectedRoom = view.findViewById(R.id.tvSelectedRoom);
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        rvEnergyDevices = view.findViewById(R.id.rvEnergyDevices);
        rvEnergyDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        tvSelectedRoom.setText(currentRoom);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(EnergyViewModel.class);

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            loadingSpinner.setVisibility(loading ? View.VISIBLE : View.GONE);
            energyContent.setVisibility(loading ? View.GONE : View.VISIBLE);
        });

        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.currentUserProfile = user;
                userRate = user.getElectricityRate() > 0 ? user.getElectricityRate() : 0.12;
                profileSavingsTarget = user.getSavingsTarget();
                calculateAndDisplayEnergy();
            }
        });

        viewModel.getDevices().observe(getViewLifecycleOwner(), devices -> {
            allDevices.clear();
            allDevices.addAll(devices);
            calculateAndDisplayEnergy();
        });

        viewModel.getLogs().observe(getViewLifecycleOwner(), logs -> {
            allLogs.clear();
            allLogs.addAll(logs);
            calculateAndDisplayEnergy();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void calculateAndDisplayEnergy() {
        if (allDevices.isEmpty()) return;

        Map<String, Double> deviceTypeTotalCost = new HashMap<>();
        Map<String, Map<String, Double>> roomTypeCost = new HashMap<>(); 

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
            double cost = hours * device.getPowerConsumption() * userRate * (device.getCount() > 0 ? device.getCount() : 1);
            
            updateMaps(deviceTypeTotalCost, roomTypeCost, device.getType(), device.getRoomId(), cost);
        }

        double totalHouseExpenditure = 0;
        for (Double val : deviceTypeTotalCost.values()) totalHouseExpenditure += val;

        totalBudget = (profileSavingsTarget > 0) ? profileSavingsTarget : 500.0;

        updateTopUI(totalHouseExpenditure, totalBudget);
        updateDeviceList(deviceTypeTotalCost, roomTypeCost.getOrDefault(currentRoom, new HashMap<>()));
    }

    private void updateMaps(Map<String, Double> totalMap, Map<String, Map<String, Double>> roomMap, String type, String room, double cost) {
        totalMap.put(type, totalMap.getOrDefault(type, 0.0) + cost);
        Map<String, Double> typesInRoom = roomMap.get(room);
        if (typesInRoom == null) {
            typesInRoom = new HashMap<>();
            roomMap.put(room, typesInRoom);
        }
        typesInRoom.put(type, typesInRoom.getOrDefault(type, 0.0) + cost);
    }

    private void updateTopUI(double spent, double budget) {
        tvTotalExpenses.setText(String.format(Locale.US, "$%.2f", spent));
        tvSavingsValue.setText(String.format(Locale.US, "$%.2f", spent));
        tvMaxBudget.setText(String.format(Locale.US, "$%.2f", budget));
        tvSavingsTargetValue.setText(String.format(Locale.US, "$%.2f", budget));
        
        int precision = 10000;
        int progress = (budget > 0) ? (int) ((spent / budget) * precision) : 0;
        if (arcProgressBar != null) {
            arcProgressBar.setMax(precision);
            arcProgressBar.setProgress(Math.min(progress, precision));
        }
        savingsProgressBar.setMax(precision);
        savingsProgressBar.setProgress(Math.min(progress, precision));
        
        if (spent > budget) {
            tvBudgetStatus.setText("Over Budget");
            tvBudgetStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvBudgetStatus.setText("Good");
            tvBudgetStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void updateDeviceList(Map<String, Double> totalExp, Map<String, Double> roomExp) {
        categoryDataList.clear();
        Map<String, Double> deviceTargets = (currentUserProfile != null)
                ? currentUserProfile.getDeviceSavingsTargets() : new HashMap<>();

        for (String type : roomExp.keySet()) {
            double budget = deviceTargets.getOrDefault(type, 50.0);
            categoryDataList.add(new EnergyDeviceAdapter.EnergyCategoryData(
                    type, currentRoom, totalExp.getOrDefault(type, 0.0), roomExp.getOrDefault(type, 0.0), budget));
        }

        Collections.sort(categoryDataList, (o1, o2) -> {
            if (o1.roomExpenditure > 0 && o2.roomExpenditure == 0) return -1;
            if (o1.roomExpenditure == 0 && o2.roomExpenditure > 0) return 1;
            return Double.compare(o2.roomExpenditure, o1.roomExpenditure);
        });

        if (adapter == null) {
            adapter = new EnergyDeviceAdapter(getContext(), categoryDataList, totalBudget);
            rvEnergyDevices.setAdapter(adapter);
        } else {
            adapter.setTotalBudget(totalBudget);
            adapter.notifyDataSetChanged();
        }
    }

    private void showRoomFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        java.util.Set<String> roomIds = new java.util.TreeSet<>();
        for (Device d : allDevices) if (d.getRoomId() != null) roomIds.add(d.getRoomId());
        
        if (roomIds.isEmpty()) {
            String[] rooms = {"Living Room", "Bedroom", "Kitchen", "Washroom", "Drawing Room", "Dining Room", "TV Lounge"};
            for (String room : rooms) popup.getMenu().add(room);
        } else {
            for (String room : roomIds) popup.getMenu().add(room);
        }
        
        popup.setOnMenuItemClickListener(item -> {
            currentRoom = item.getTitle().toString();
            tvSelectedRoom.setText(currentRoom);
            calculateAndDisplayEnergy();
            return true;
        });
        popup.show();
    }
}
