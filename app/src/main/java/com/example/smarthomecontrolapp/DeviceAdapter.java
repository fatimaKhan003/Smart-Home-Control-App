package com.example.smarthomecontrolapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Device> list;

    public DeviceAdapter(Context context, ArrayList<Device> list) {
        this.context = context;
        this.list = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDevice;
        TextView tvDeviceName, tvConsumption, tvRoomname, tvDeviceCount;
        SwitchMaterial switchDevice;
        MaterialCardView deviceCard;

        public ViewHolder(View itemView) {
            super(itemView);
            ivDevice = itemView.findViewById(R.id.imgDevice);
            tvDeviceName = itemView.findViewById(R.id.txtDeviceName);
            tvConsumption = itemView.findViewById(R.id.txtConsumption);
            switchDevice = itemView.findViewById(R.id.switchDevice);
            tvRoomname = itemView.findViewById(R.id.txtRoomName);
            tvDeviceCount = itemView.findViewById(R.id.txtDeviceCount);
            deviceCard = itemView.findViewById(R.id.deviceCard);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Device device = list.get(pos);
        holder.tvDeviceName.setText(device.getDeviceName());
        holder.tvConsumption.setText(device.getPowerConsumption() + " kW");
        holder.tvRoomname.setText(device.getRoomId());
        holder.tvDeviceCount.setText(device.getCount() + " Devices");

        switch (device.getType()) {
            case "Smart TV": holder.ivDevice.setImageResource(R.drawable.ic_tv); break;
            case "Smart Fridge": holder.ivDevice.setImageResource(R.drawable.ic_fridge); break;
            case "Lighting": holder.ivDevice.setImageResource(R.drawable.ic_bulb); break;
            case "Air Condition": holder.ivDevice.setImageResource(R.drawable.ic_ac); break;
            case "Blinds": holder.ivDevice.setImageResource(R.drawable.ic_blinds); break;
            default: holder.ivDevice.setImageResource(R.drawable.icon_home); break;
        }

        holder.switchDevice.setOnCheckedChangeListener(null);
        holder.switchDevice.setChecked(device.isStatus());

        holder.switchDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (device.isStatus() == isChecked) return;

            // 1. Update local logic using toggleStatus to preserve/update timestamps correctly
            device.toggleStatus(isChecked);

            // 2. Persist to Firebase
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId != null) {
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("users").child(userId);
                
                // Update the specific device state
                db.child("devices").child(device.getDeviceId()).setValue(device);

                // 3. Create a detailed log entry for historical calculation
                Map<String, Object> log = new HashMap<>();
                log.put("deviceId", device.getDeviceId());
                log.put("deviceName", device.getDeviceName());
                log.put("deviceType", device.getType());
                log.put("timestamp", System.currentTimeMillis());
                log.put("action", isChecked ? "TURN_ON" : "TURN_OFF");
                log.put("powerConsumption", device.getPowerConsumption());
                log.put("roomId", device.getRoomId());
                
                db.child("logs").push().setValue(log);
            }
        });
    }

    @Override
    public int getItemCount() { return list.size(); }
}
