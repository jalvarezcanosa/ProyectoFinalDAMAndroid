package com.example.contapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contapp.R;
import com.example.contapp.models.Participant;

import java.util.ArrayList;
import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder> {

    private List<Participant> participantList = new ArrayList<>();

    public void setParticipantList(List<Participant> participantList){
        this.participantList = participantList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        Participant participant = participantList.get(position);

        holder.tvPosition.setText(String.valueOf(position + 1));
        holder.tvUsername.setText(participant.getUsername());
        holder.tvClicks.setText(String.valueOf(participant.getTotalClicks()));
    }

    @Override
    public int getItemCount() {
        return participantList.size();
    }

    public static class ParticipantViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition;
        TextView tvUsername;
        TextView tvClicks;

        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvClicks = itemView.findViewById(R.id.tvCLicks);
        }
    }
}
