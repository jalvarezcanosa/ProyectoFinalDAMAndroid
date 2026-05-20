package com.example.contapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.contapp.R;
import com.example.contapp.activities.CounterDetailActivity;
import com.example.contapp.models.Counter;

import java.util.ArrayList;
import java.util.List;

public class CounterAdapter extends RecyclerView.Adapter<CounterAdapter.CounterViewHolder> {
    private Context context;
    private List<Counter> counterList = new ArrayList<>();

    public CounterAdapter(Context context){
        this.context = context;
    }
    public void setCounterList(List<Counter> counterList){
        this.counterList = counterList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CounterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_counter, parent, false);
        return new CounterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CounterViewHolder holder, int position) {
        Counter counter = counterList.get(position);

        holder.tvCounterTitle.setText(counter.getTitle());
        holder.tvGlobalCount.setText("Global: " + counter.getGlobalCount());
        holder.tvIndividualCount.setText("Mío "+ counter.getIndividualCount());

        if("closed".equalsIgnoreCase(counter.getStatus())) {
            holder.tvCounterStatus.setText("Estado: Cerrado");
            holder.tvCounterStatus.setTextColor(Color.RED);
        } else {
            holder.tvCounterStatus.setText("Estado: Abierto");
            holder.tvCounterStatus.setTextColor(Color.parseColor("#2E7D32"));
        }

        String imageUrl = counter.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).placeholder(R.drawable.ic_launcher_background).into(holder.ivCounterImage);
        } else {
            holder.ivCounterImage.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v-> {
            Intent intent = new Intent(context, CounterDetailActivity.class);
            intent.putExtra("COUNTER_ID", counter.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return counterList.size();
    }

    public static class CounterViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCounterImage;
        TextView tvCounterTitle, tvCounterStatus, tvGlobalCount, tvIndividualCount;

        public CounterViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCounterImage = itemView.findViewById(R.id.ivCounterImage);
            tvCounterTitle = itemView.findViewById(R.id.tvCounterTitle);
            tvCounterStatus = itemView.findViewById(R.id.tvCounterStatus);
            tvGlobalCount = itemView.findViewById(R.id.tvGlobalCount);
            tvIndividualCount = itemView.findViewById(R.id.tvIndividualCount);
        }
    }
}
