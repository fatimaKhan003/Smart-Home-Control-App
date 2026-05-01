package com.example.smarthomecontrolapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    Context context;
    ArrayList<Device>list;
    public DeviceAdapter(Context context, ArrayList<Device> list)
    {
        this.context=context;
        this.list=list;
    }
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView ivDevice;
        TextView tvDeviceName, tvConsumption, tvRoomname,tvDeviceCount;
        SwitchMaterial switchDevice;
        MaterialCardView deviceCard;
        public ViewHolder(View itemView)
        {super(itemView);
            ivDevice=itemView.findViewById(R.id.imgDevice);
            tvDeviceName=itemView.findViewById(R.id.txtDeviceName);
            tvConsumption=itemView.findViewById(R.id.txtConsumption);
            switchDevice=itemView.findViewById(R.id.switchDevice);
            tvRoomname=itemView.findViewById(R.id.txtRoomName);
            tvDeviceCount=itemView.findViewById(R.id.txtDeviceCount);
            deviceCard=itemView.findViewById(R.id.deviceCard);

        }

    }

@Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
{
    View view= LayoutInflater.from(context).inflate(R.layout.item_device,parent,false);
    return new ViewHolder(view);
}
@Override
    public void onBindViewHolder(ViewHolder holder, int pos)
{
    Device device= list.get(pos);
    holder.tvDeviceName.setText(device.getDeviceName());
    holder.tvConsumption.setText(device.getPowerConsumption()+ " W");

    holder.tvRoomname.setText(device.getRoomId());
    holder.tvDeviceCount.setText(device.getCount()+" Devices");

    switch(device.getType())
    {
        case "Smart TV":
            holder.ivDevice.setImageResource(R.drawable.ic_tv);
            break;
        case "Smart Fridge":
            holder.ivDevice.setImageResource(R.drawable.ic_fridge);
            break;
        case "Lightings":
            holder.ivDevice.setImageResource(R.drawable.ic_bulb);
            break;
        case "Air Condition":
            holder.ivDevice.setImageResource(R.drawable.ic_ac);
            break;
        case "Blinds":
            holder.ivDevice.setImageResource(R.drawable.ic_blinds);
            break;
        default:
            holder.ivDevice.setImageResource(R.drawable.icon_home);
            break;
    }
    holder.switchDevice.setChecked(device.isStatus());
    if(device.isStatus())
    {
        holder.deviceCard.setCardBackgroundColor(android.graphics.Color.parseColor("#7B61FF"));
        holder.tvDeviceName.setTextColor(android.graphics.Color.WHITE);
        holder.tvDeviceCount.setTextColor(android.graphics.Color.parseColor("#E0E0E0"));
        holder.ivDevice.setColorFilter(android.graphics.Color.WHITE);
    } else {
        holder.deviceCard.setCardBackgroundColor(android.graphics.Color.WHITE);
        holder.tvDeviceName.setTextColor(android.graphics.Color.BLACK);
        holder.tvDeviceCount.setTextColor(android.graphics.Color.GRAY);
        holder.ivDevice.setColorFilter(android.graphics.Color.BLACK);
    }

}
@Override
    public int getItemCount()
{
    return list.size();
}

}
