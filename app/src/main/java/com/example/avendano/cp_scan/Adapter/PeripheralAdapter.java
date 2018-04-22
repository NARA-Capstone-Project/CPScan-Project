package com.example.avendano.cp_scan.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.avendano.cp_scan.Activities.ViewRequestPeripheralsDetails;
import com.example.avendano.cp_scan.Model.RequestPeripherals;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.R;

import java.util.List;

/**
 * Created by Avendano on 21 Apr 2018.
 */

public class PeripheralAdapter extends RecyclerView.Adapter<PeripheralAdapter.AdapterViewHolder> {
    List<RequestPeripherals> peripheralsList;
    Context mCtx;
    Activity act;
    SwipeRefreshLayout refreshLayout;
    VolleyRequestSingleton volley;

    public PeripheralAdapter(List<RequestPeripherals> peripheralsList, Context mCtx, Activity act, SwipeRefreshLayout refreshLayout) {
        this.peripheralsList = peripheralsList;
        this.mCtx = mCtx;
        this.act = act;
        this.refreshLayout = refreshLayout;
        volley = new VolleyRequestSingleton(mCtx);
    }

    @Override
    public AdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View v = inflater.inflate(R.layout.req_list_item, null);
        return new AdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AdapterViewHolder holder, int position) {
        final RequestPeripherals requestPeripherals = peripheralsList.get(position);
        holder.btn_container.setVisibility(View.INVISIBLE);
        holder.status.setVisibility(View.VISIBLE);
        holder.status.setText("Status: " + requestPeripherals.getStatus());

        holder.location.setText(requestPeripherals.getRoom_name());
        holder.category.setText(requestPeripherals.getCategory());
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //intent to peripheral details
                Intent intent = new Intent(mCtx, ViewRequestPeripheralsDetails.class);
                intent.putExtra("req_id", requestPeripherals.getReq_id());
                act.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return peripheralsList.size();
    }

    class AdapterViewHolder extends RecyclerView.ViewHolder {
        TextView category;
        TextView location;
        CardView card;
        LinearLayout btn_container;
        TextView status;

        public AdapterViewHolder(View itemView) {
            super(itemView);
            category = (TextView) itemView.findViewById(R.id.category);
            location = (TextView) itemView.findViewById(R.id.location);
            card = (CardView) itemView.findViewById(R.id.cardview);
            btn_container = (LinearLayout) itemView.findViewById(R.id.linear);
            status = (TextView) itemView.findViewById(R.id.req_status);

        }
    }
}
