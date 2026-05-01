package com.example.smarthomecontrolapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

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
        TextView tvDeviceName, tvConsumption;
        SwitchMaterial switchDevice;
        public ViewHolder(View itemView)
        {super(itemView);
            ivDevice=itemView.findViewById(R.id.imgDevice);
            tvDeviceName=itemView.findViewById(R.id.txtDeviceName);
            tvConsumption=itemView.findViewById(R.id.txtConsumption);
            switchDevice=itemView.findViewById(R.id.switchDevice);

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
    holder.switchDevice.setChecked(device.isStatus());
}
@Override
    public int getItemCount()
{
    return list.size();
}

}
