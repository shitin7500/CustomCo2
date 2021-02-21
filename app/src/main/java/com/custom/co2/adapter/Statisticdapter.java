package com.custom.co2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.custom.co2.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;


public class Statisticdapter extends RecyclerView.Adapter<Statisticdapter.ViewHolder> {
    ArrayList<DocumentSnapshot> arrayListData;
    Context context;

    public Statisticdapter(Context context, ArrayList<DocumentSnapshot> arrayListData) {
        this.arrayListData = arrayListData;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.txtsource.setText(arrayListData.get(position).getString("Source"));
        holder.txtDestination.setText(arrayListData.get(position).getString("Destination"));
        holder.txtDistance.setText(arrayListData.get(position).getString("Distance"));
        holder.txtCO2.setText(arrayListData.get(position).getString("Co2"));
    }


    @Override
    public int getItemCount() {
        return arrayListData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtsource, txtDestination, txtDistance, txtCO2;

        public ViewHolder(View itemView) {
            super(itemView);
            txtsource = (TextView) itemView.findViewById(R.id.txtsource);
            txtDestination = (TextView) itemView.findViewById(R.id.txtDestination);
            txtDistance = (TextView) itemView.findViewById(R.id.txtDistance);
            txtCO2 = (TextView) itemView.findViewById(R.id.txtCO2);
        }
    }
}