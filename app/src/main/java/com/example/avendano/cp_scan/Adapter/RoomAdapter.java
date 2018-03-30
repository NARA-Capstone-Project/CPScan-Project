package com.example.avendano.cp_scan.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.avendano.cp_scan.Activities.ViewRoom;
import com.example.avendano.cp_scan.Model.Rooms;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.RecyclerHolder.RecyclerHolder;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class RoomAdapter extends RecyclerView.Adapter<RecyclerHolder>{

    private Context mCtx;
    private Activity act;
    private List<Rooms> roomList;
    SwipeRefreshLayout swiper;

    public RoomAdapter(Activity act,Context mCtx, List<Rooms> roomList, SwipeRefreshLayout swiper) {
        this.mCtx = mCtx;
        this.act = act;
        this.roomList = roomList;
        this.swiper = swiper;
    }

    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_item, parent, false);
        RecyclerHolder holder = new RecyclerHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerHolder holder, final int position) {
        holder.headTxt.setText(roomList.get(position).getRoom_name());
        holder.midTxt.setText("Room Custodian: " + roomList.get(position).getRoom_custodian());
        holder.subTxt.setText("Location: " + roomList.get(position).getRoom_building());
        holder.img.setBackgroundResource(R.drawable.ic_room_orange);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //intent to ViewRoom
                Intent intent = new Intent(mCtx.getApplicationContext(), ViewRoom.class);
                intent.putExtra("room_id", roomList.get(position).getRoom_id());
                act.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }
}
