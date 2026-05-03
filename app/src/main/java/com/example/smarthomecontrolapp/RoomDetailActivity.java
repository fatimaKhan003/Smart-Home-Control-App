package com.example.smarthomecontrolapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoomDetailActivity extends AppCompatActivity {

    TextView tvRoomTitle, tvTemperature, tvTodayCost, tvTodayBudget, tvFilterSelector, tvDevicesLabel;
    RecyclerView recyclerRoomDevices;
    DeviceAdapter deviceAdapter;
    ArrayList<Device> roomDeviceList = new ArrayList<>();
    String roomName;
    MaterialButton historybtn;

    private EnergyViewModel viewModel;
    private List<Device> allDevices = new ArrayList<>();
    private List<LogEntry> allLogs = new ArrayList<>();
    private double userRate = 0.12;
    private double monthlySavingsTarget = 500.0;
    private boolean isMoneyView = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        roomName = getIntent().getStringExtra("roomName");
        if (roomName == null) {
            roomName = "Unknown Room";
        }

        initViews();
        setupViewModel();

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        historybtn.setOnClickListener(v -> {
            DetailsFragment fragment = new DetailsFragment();
            Bundle args = new Bundle();
            args.putString("roomName", roomName);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        tvFilterSelector.setOnClickListener(this::showFilterMenu);
    }

    private void initViews() {
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvTodayCost = findViewById(R.id.tvTodayCost);
        tvTodayBudget = findViewById(R.id.tvTodayBudget);
        tvFilterSelector = findViewById(R.id.tvFilterSelector);
        recyclerRoomDevices = findViewById(R.id.recyclerRoomDevices);

        tvRoomTitle.setText(roomName);

        deviceAdapter = new DeviceAdapter(this, roomDeviceList);
        recyclerRoomDevices.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerRoomDevices.setAdapter(deviceAdapter);
        historybtn = findViewById(R.id.btnHistory);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(EnergyViewModel.class);

        viewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                userRate = user.getElectricityRate() > 0 ? user.getElectricityRate() : 0.12;
                monthlySavingsTarget = user.getSavingsTarget() > 0 ? user.getSavingsTarget() : 500.0;
                calculateRoomStats();
            }
        });

        viewModel.getDevices().observe(this, devices -> {
            allDevices.clear();
            allDevices.addAll(devices);
            
            roomDeviceList.clear();
            for (Device d : devices) {
                if (roomName.equalsIgnoreCase(d.getRoomId())) {
                    roomDeviceList.add(d);
                }
            }
            deviceAdapter.notifyDataSetChanged();
            calculateRoomStats();
        });

        viewModel.getLogs().observe(this, logs -> {
            allLogs.clear();
            allLogs.addAll(logs);
            calculateRoomStats();
        });
    }

    private void showFilterMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add("Money");
        popup.getMenu().add("Electricity");
        popup.setOnMenuItemClickListener(item -> {
            isMoneyView = item.getTitle().toString().equals("Money");
            tvFilterSelector.setText(item.getTitle().toString() + " ⌵");
            calculateRoomStats();
            return true;
        });
        popup.show();
    }

    private void calculateRoomStats() {
        if (roomDeviceList.isEmpty()) return;

        double todayKWh = calculateTodayConsumption();
        
        // Calculate Today's Budget (Weighted by devices)
        Calendar cal = Calendar.getInstance();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        double dailyHouseBudgetMoney = monthlySavingsTarget / daysInMonth;

        int totalHouseDevices = 0;
        for (Device d : allDevices) totalHouseDevices += Math.max(1, d.getCount());
        
        int roomDevices = 0;
        for (Device d : roomDeviceList) roomDevices += Math.max(1, d.getCount());

        double roomWeight = totalHouseDevices > 0 ? (double) roomDevices / totalHouseDevices : 0;
        double todayRoomBudgetMoney = dailyHouseBudgetMoney * roomWeight;
        double todayRoomBudgetKWh = userRate > 0 ? todayRoomBudgetMoney / userRate : 0;

        boolean isOverBudget;
        if (isMoneyView) {
            double cost = todayKWh * userRate;
            tvTodayCost.setText(String.format(Locale.US, "$%.2f", cost));
            tvTodayBudget.setText(String.format(Locale.US, "$%.2f", todayRoomBudgetMoney));
            isOverBudget = cost > todayRoomBudgetMoney;
        } else {
            tvTodayCost.setText(String.format(Locale.US, "%.2f kWh", todayKWh));
            tvTodayBudget.setText(String.format(Locale.US, "%.2f kWh", todayRoomBudgetKWh));
            isOverBudget = todayKWh > todayRoomBudgetKWh;
        }

        // Change color if over budget
        if (isOverBudget) {
            tvTodayCost.setTextColor(ContextCompat.getColor(this, R.color.logoutRed));
        } else {
            tvTodayCost.setTextColor(ContextCompat.getColor(this, R.color.textDark));
        }
    }

    private double calculateTodayConsumption() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();
        long now = System.currentTimeMillis();

        Map<String, List<Interval>> intervalsMap = new HashMap<>();
        Map<String, Long> lastOn = new HashMap<>();

        List<LogEntry> sortedLogs = new ArrayList<>(allLogs);
        Collections.sort(sortedLogs, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

        for (LogEntry log : sortedLogs) {
            String dId = log.getDeviceId();
            if ("TURN_ON".equals(log.getAction())) {
                lastOn.put(dId, log.getTimestamp());
            } else if ("TURN_OFF".equals(log.getAction()) && lastOn.containsKey(dId)) {
                if (!intervalsMap.containsKey(dId)) intervalsMap.put(dId, new ArrayList<>());
                intervalsMap.get(dId).add(new Interval(lastOn.get(dId), log.getTimestamp()));
                lastOn.remove(dId);
            }
        }

        double roomTodayKWh = 0;
        for (Device d : roomDeviceList) {
            long totalDurationToday = 0;
            
            // 1. Check past intervals from logs
            List<Interval> intervals = intervalsMap.get(d.getDeviceId());
            if (intervals != null) {
                for (Interval inter : intervals) {
                    long overlapStart = Math.max(todayStart, inter.start);
                    long overlapEnd = Math.min(now, inter.end);
                    if (overlapEnd > overlapStart) {
                        totalDurationToday += (overlapEnd - overlapStart);
                    }
                }
            }

            // 2. Check current active session (Logic from fix-issues)
            if (d.isStatus()) {
                long sessionStart = lastOn.containsKey(d.getDeviceId()) ? lastOn.get(d.getDeviceId()) : d.getLastStatusChangeTimestamp();
                if (sessionStart < now) {
                    long overlapStart = Math.max(todayStart, sessionStart);
                    if (now > overlapStart) {
                        totalDurationToday += (now - overlapStart);
                    }
                }
            }

            // 3. Final math for this device
            double hours = totalDurationToday / (1000.0 * 60.0 * 60.0);
            roomTodayKWh += (hours * d.getPowerConsumption() * Math.max(1, d.getCount()));
        }

        // 4. Update the UI label (Logic from main)
        tvDevicesLabel.setText("Devices In This Room (" + roomDeviceList.size() + ")");

        return roomTodayKWh;
    }

    private static class Interval {
        long start, end;
        Interval(long s, long e) { start = s; end = e; }
    }
}
