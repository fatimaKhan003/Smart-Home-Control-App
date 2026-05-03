package com.example.smarthomecontrolapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomecontrolapp.R;

import java.util.List;
import java.util.Locale;

public class EnergyDeviceAdapter extends RecyclerView.Adapter<EnergyDeviceAdapter.ViewHolder> {

    private final Context context;
    private final List<EnergyCategoryData> categoryDataList;
    private double totalBudget;

    public static class EnergyCategoryData {
        public String type;
        public String room;             
        public double totalExpenditure; 
        public double roomExpenditure;  
        public double budget;           

        public EnergyCategoryData(String type, String room, double totalExpenditure, double roomExpenditure, double budget) {
            this.type = type;
            this.room = room;
            this.totalExpenditure = totalExpenditure;
            this.roomExpenditure = roomExpenditure;
            this.budget = budget;
        }
    }

    public EnergyDeviceAdapter(Context context, List<EnergyCategoryData> categoryDataList, double totalBudget) {
        this.context = context;
        this.categoryDataList = categoryDataList;
        this.totalBudget = totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_energy_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnergyCategoryData data = categoryDataList.get(position);
        
        // Just show Device Type
        holder.tvDeviceType.setText(data.type);
        
        // Show Savings / Target
        holder.tvDeviceSaving.setText(String.format(Locale.US, "$%.2f / $%.2f", data.roomExpenditure, data.budget));

        // Use a multiplier for precision so the bar moves for small values
        double multiplier = 1000.0;
        double maxForThisType = data.budget > 0 ? data.budget : 1.0; 
        
        holder.progressBar.setMax((int) (maxForThisType * multiplier));
        
        // Secondary progress for total across rooms
        holder.progressBar.setSecondaryProgress((int) (data.totalExpenditure * multiplier));
        
        // Main progress for this specific room
        holder.progressBar.setProgress((int) (data.roomExpenditure * multiplier));
    }

    @Override
    public int getItemCount() {
        return categoryDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceType, tvDeviceSaving;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceType = itemView.findViewById(R.id.tvDeviceType);
            tvDeviceSaving = itemView.findViewById(R.id.tvDeviceSaving);
            progressBar = itemView.findViewById(R.id.deviceProgressBar);
        }
    }
}
