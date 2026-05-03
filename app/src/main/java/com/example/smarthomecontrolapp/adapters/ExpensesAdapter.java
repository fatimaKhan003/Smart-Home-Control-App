package com.example.smarthomecontrolapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomecontrolapp.models.Expense;
import com.example.smarthomecontrolapp.R;

import java.util.List;
import java.util.Locale;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ViewHolder> {

    private final List<Expense> expenseList;

    public ExpensesAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.tvMonth.setText(expense.getMonth());
        holder.tvConsumption.setText(String.format(Locale.US, "%.1f KWH", expense.getConsumption()));
        holder.tvAmount.setText(String.format(Locale.US, "$%.3f", expense.getAmount()));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonth, tvConsumption, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvConsumption = itemView.findViewById(R.id.tvConsumption);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
