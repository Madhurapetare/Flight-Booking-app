package com.example.flightapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flightapp.model.Flight;

import java.util.ArrayList;
import java.util.List;

public class FlightsAdapter extends RecyclerView.Adapter<FlightsAdapter.VH> {
    interface OnClick { void onClick(Flight f); }
    private List<Flight> list = new ArrayList<>();
    private Context ctx;
    private OnClick listener;

    public FlightsAdapter(Context c, OnClick l) { ctx = c; listener = l; }

    public void setFlights(List<Flight> flights) { list.clear(); if (flights!=null) list.addAll(flights); notifyDataSetChanged(); }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Flight f = list.get(position);
        holder.title.setText(f.airline + " " + f.flight_number + " ₹" + f.price);
        holder.sub.setText(f.src + " → " + f.dest + " | Dep: " + f.depart_datetime + " Seats: " + f.seats_available);
        holder.itemView.setOnClickListener(v -> listener.onClick(f));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, sub;
        VH(View v) {
            super(v);
            title = v.findViewById(android.R.id.text1);
            sub = v.findViewById(android.R.id.text2);
        }
    }
}
