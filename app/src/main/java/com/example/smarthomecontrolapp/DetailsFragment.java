package com.example.smarthomecontrolapp;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class DetailsFragment extends Fragment {

    private TextView tvTodayCost, tvTodayKWh, btnMonthly, btnYearly;
    private RecyclerView rvExpenses;
    private LinearLayout barContainer, xAxisContainer;
    private ExpensesAdapter adapter;
    private List<Expense> expenseList = new ArrayList<>();
    
    private EnergyViewModel viewModel;
    private List<Device> allDevices = new ArrayList<>();
    private List<LogEntry> allLogs = new ArrayList<>();
    private double userRate = 0.12;
    private boolean isMonthlyMode = true;

    public DetailsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        
        initViews(view);
        setupViewModel();
        setupTabs();

        ImageView ivMenu = view.findViewById(R.id.ivMenu);
        ivMenu.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });
        
        return view;
    }

    private void initViews(View view) {
        tvTodayCost = view.findViewById(R.id.tvTodayCost);
        tvTodayKWh = view.findViewById(R.id.tvTodayKWh);
        btnMonthly = view.findViewById(R.id.btnMonthly);
        btnYearly = view.findViewById(R.id.btnYearly);
        barContainer = view.findViewById(R.id.barContainer);
        xAxisContainer = view.findViewById(R.id.xAxisContainer);
        rvExpenses = view.findViewById(R.id.rvExpenses);
        
        rvExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpensesAdapter(expenseList);
        rvExpenses.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(EnergyViewModel.class);
        
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                userRate = user.getElectricityRate() > 0 ? user.getElectricityRate() : 0.12;
                processData();
            }
        });

        viewModel.getDevices().observe(getViewLifecycleOwner(), devices -> {
            allDevices.clear();
            allDevices.addAll(devices);
            processData();
        });

        viewModel.getLogs().observe(getViewLifecycleOwner(), logs -> {
            allLogs.clear();
            allLogs.addAll(logs);
            processData();
        });
    }

    private void setupTabs() {
        btnMonthly.setOnClickListener(v -> {
            isMonthlyMode = true;
            updateTabUI();
            processData();
        });
        btnYearly.setOnClickListener(v -> {
            isMonthlyMode = false;
            updateTabUI();
            processData();
        });
    }

    private void updateTabUI() {
        if (isMonthlyMode) {
            btnMonthly.setBackgroundResource(R.drawable.bg_tab_selected);
            btnMonthly.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary));
            btnYearly.setBackground(null);
            btnYearly.setTextColor(ContextCompat.getColor(requireContext(), R.color.textSecondary));
        } else {
            btnYearly.setBackgroundResource(R.drawable.bg_tab_selected);
            btnYearly.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary));
            btnMonthly.setBackground(null);
            btnMonthly.setTextColor(ContextCompat.getColor(requireContext(), R.color.textSecondary));
        }
    }

    private void processData() {
        if (allDevices.isEmpty() || !isAdded()) return;

        Map<String, List<Interval>> deviceIntervals = calculateIntervals();
        calculateTodayStats(deviceIntervals);
        
        if (isMonthlyMode) {
            groupDataByMonth(deviceIntervals);
        } else {
            groupDataByYear(deviceIntervals);
        }
    }

    private Map<String, List<Interval>> calculateIntervals() {
        Map<String, List<Interval>> intervals = new HashMap<>();
        Map<String, Long> lastOn = new HashMap<>();
        
        List<LogEntry> sortedLogs = new ArrayList<>(allLogs);
        Collections.sort(sortedLogs, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

        for (LogEntry log : sortedLogs) {
            String dId = log.getDeviceId();
            if ("TURN_ON".equals(log.getAction())) {
                lastOn.put(dId, log.getTimestamp());
            } else if ("TURN_OFF".equals(log.getAction()) && lastOn.containsKey(dId)) {
                if (!intervals.containsKey(dId)) intervals.put(dId, new ArrayList<>());
                intervals.get(dId).add(new Interval(lastOn.get(dId), log.getTimestamp()));
                lastOn.remove(dId);
            }
        }
        
        long now = System.currentTimeMillis();
        for (Device d : allDevices) {
            if (d.isStatus()) {
                long start = lastOn.containsKey(d.getDeviceId()) ? lastOn.get(d.getDeviceId()) : d.getLastStatusChangeTimestamp();
                if (start > 0 && start < now) {
                    if (!intervals.containsKey(d.getDeviceId())) intervals.put(d.getDeviceId(), new ArrayList<>());
                    intervals.get(d.getDeviceId()).add(new Interval(start, now));
                }
            }
        }
        return intervals;
    }

    private void calculateTodayStats(Map<String, List<Interval>> deviceIntervals) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();
        long todayEnd = System.currentTimeMillis();

        double todayKWh = 0;
        for (Device d : allDevices) {
            List<Interval> intervals = deviceIntervals.get(d.getDeviceId());
            if (intervals == null) continue;
            
            long durationMillis = 0;
            for (Interval inter : intervals) {
                long overlapStart = Math.max(todayStart, inter.start);
                long overlapEnd = Math.min(todayEnd, inter.end);
                if (overlapEnd > overlapStart) {
                    durationMillis += (overlapEnd - overlapStart);
                }
            }
            double hours = durationMillis / (1000.0 * 60.0 * 60.0);
            int count = d.getCount() > 0 ? d.getCount() : 1;
            todayKWh += (hours * d.getPowerConsumption() * count);
        }

        tvTodayKWh.setText(String.format(Locale.US, "%.1f KWH", todayKWh));
        tvTodayCost.setText(String.format(Locale.US, "$%.3f", todayKWh * userRate));
    }

    private void groupDataByMonth(Map<String, List<Interval>> deviceIntervals) {
        TreeMap<String, Expense> monthlyMap = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM", Locale.US);
        SimpleDateFormat keySdf = new SimpleDateFormat("yyyy-MM", Locale.US);
        
        // Ensure 8 slots for chart spacing
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -7);
        for (int i = 0; i < 8; i++) {
            String key = keySdf.format(cal.getTime());
            monthlyMap.put(key, new Expense(sdf.format(cal.getTime()), 0, 0));
            cal.add(Calendar.MONTH, 1);
        }

        processBuckets(deviceIntervals, monthlyMap, keySdf, sdf, Calendar.MONTH);
        updateUIWithStats(monthlyMap);
    }

    private void groupDataByYear(Map<String, List<Interval>> deviceIntervals) {
        TreeMap<String, Expense> yearlyMap = new TreeMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.US);
        
        // Ensure 5 slots for chart spacing
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -4);
        for (int i = 0; i < 5; i++) {
            String key = sdf.format(cal.getTime());
            yearlyMap.put(key, new Expense(key, 0, 0));
            cal.add(Calendar.YEAR, 1);
        }

        processBuckets(deviceIntervals, yearlyMap, sdf, sdf, Calendar.YEAR);
        updateUIWithStats(yearlyMap);
    }

    private void processBuckets(Map<String, List<Interval>> deviceIntervals, TreeMap<String, Expense> map, SimpleDateFormat keySdf, SimpleDateFormat displaySdf, int calendarField) {
        for (Device d : allDevices) {
            List<Interval> intervals = deviceIntervals.get(d.getDeviceId());
            if (intervals == null) continue;
            for (Interval inter : intervals) {
                // Split intervals across period boundaries for substance/accuracy
                long tempStart = inter.start;
                while (tempStart < inter.end) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(tempStart);
                    
                    Calendar periodEnd = (Calendar) cal.clone();
                    if (calendarField == Calendar.MONTH) {
                        periodEnd.set(Calendar.DAY_OF_MONTH, 1);
                        periodEnd.add(Calendar.MONTH, 1);
                    } else {
                        periodEnd.set(Calendar.DAY_OF_YEAR, 1);
                        periodEnd.add(Calendar.YEAR, 1);
                    }
                    periodEnd.set(Calendar.HOUR_OF_DAY, 0);
                    periodEnd.set(Calendar.MINUTE, 0);
                    periodEnd.set(Calendar.SECOND, 0);
                    periodEnd.set(Calendar.MILLISECOND, 0);
                    
                    long endOfPeriod = Math.min(inter.end, periodEnd.getTimeInMillis());
                    long duration = endOfPeriod - tempStart;
                    
                    String key = keySdf.format(cal.getTime());
                    String displayName = displaySdf.format(cal.getTime());
                    double hours = duration / (1000.0 * 60.0 * 60.0);
                    int count = d.getCount() > 0 ? d.getCount() : 1;
                    double consumption = hours * d.getPowerConsumption() * count;
                    
                    if (map.containsKey(key)) {
                        Expense e = map.get(key);
                        e.setConsumption(e.getConsumption() + consumption);
                        e.setAmount(e.getAmount() + (consumption * userRate));
                    }
                    tempStart = endOfPeriod;
                }
            }
        }
    }

    private void updateUIWithStats(TreeMap<String, Expense> map) {
        expenseList.clear();
        List<Expense> sortedList = new ArrayList<>(map.values());
        Collections.reverse(sortedList);
        for (Expense e : sortedList) {
            if (e.getConsumption() > 0) expenseList.add(e);
        }
        adapter.notifyDataSetChanged();
        
        drawChart(new ArrayList<>(map.values()));
    }

    private void drawChart(List<Expense> chartData) {
        barContainer.removeAllViews();
        xAxisContainer.removeAllViews();
        if (chartData.isEmpty()) return;

        double maxVal = 0;
        for (Expense e : chartData) if (e.getConsumption() > maxVal) maxVal = e.getConsumption();
        if (maxVal < 10) maxVal = 10; 

        // Update Y-Axis labels dynamically
        View root = getView();
        if (root != null) {
            ((TextView)root.findViewById(R.id.tvY1)).setText(String.format(Locale.US, "%.0f", maxVal/3));
            ((TextView)root.findViewById(R.id.tvY2)).setText(String.format(Locale.US, "%.0f", 2*maxVal/3));
            ((TextView)root.findViewById(R.id.tvY3)).setText(String.format(Locale.US, "%.0f", maxVal));
        }

        float density = getResources().getDisplayMetrics().density;
        int maxPixelHeight = (int) (180 * density);
        int barWidth = (int) (16 * density);

        for (Expense e : chartData) {
            // Weighted slot ensures equal spacing
            LinearLayout slot = new LinearLayout(getContext());
            LinearLayout.LayoutParams slotLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            slotLp.weight = 1;
            slot.setLayoutParams(slotLp);
            slot.setGravity(android.view.Gravity.CENTER_HORIZONTAL | android.view.Gravity.BOTTOM);

            // Bar
            View bar = new View(getContext());
            int barHeight = (int) ((e.getConsumption() / maxVal) * maxPixelHeight);
            if (e.getConsumption() > 0 && barHeight < (4 * density)) barHeight = (int) (4 * density);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(barWidth, barHeight);
            bar.setLayoutParams(lp);
            bar.setBackgroundResource(R.drawable.bg_bar_active);
            
            if (e.getConsumption() > 0) slot.addView(bar);
            barContainer.addView(slot);

            // Label
            TextView tv = new TextView(getContext());
            LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpText.weight = 1;
            tv.setLayoutParams(lpText);
            
            String label = e.getMonth();
            if (isMonthlyMode && label.length() > 3) label = label.substring(0, 3);
            
            tv.setText(label);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.textSecondary));
            xAxisContainer.addView(tv);
        }
    }

    private static class Interval {
        long start, end;
        Interval(long s, long e) { start = s; end = e; }
    }
}
