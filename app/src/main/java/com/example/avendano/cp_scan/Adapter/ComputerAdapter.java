package com.example.avendano.cp_scan.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.avendano.cp_scan.Activities.ViewPc;
import com.example.avendano.cp_scan.Getter_Setter.Computers;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.RecyclerHolder.RecyclerHolder;

import java.util.List;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class ComputerAdapter extends RecyclerView.Adapter<RecyclerHolder> {

    private Context mCtx;
    private List<Computers> computersList;
    SwipeRefreshLayout swiper;

    public ComputerAdapter(Context mCtx, List<Computers> computersList, SwipeRefreshLayout swiper) {
        this.mCtx = mCtx;
        this.computersList = computersList;
        this.swiper = swiper;
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.recycler_item, parent, false);
        RecyclerHolder holder = new RecyclerHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, final int position) {
        holder.headTxt.setText("PC " + computersList.get(position).getPc_no());
        holder.midTxt.setText("Model: " + computersList.get(position).getModel());
        holder.subTxt.setText("Status: " + computersList.get(position).getPc_status());
        holder.img.setBackgroundResource(R.drawable.ic_computer_orange);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass comp id (for report)
                Intent intent = new Intent(mCtx, ViewPc.class);
                intent.putExtra("comp_id", computersList.get(position).getComp_id());
                mCtx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return computersList.size();
    }
}
