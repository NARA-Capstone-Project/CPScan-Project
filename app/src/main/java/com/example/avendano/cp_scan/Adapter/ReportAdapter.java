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
import android.widget.Toast;

import com.example.avendano.cp_scan.Activities.ViewInventoryReport;
import com.example.avendano.cp_scan.Model.Reports;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.RecyclerHolder.RecyclerHolder;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class ReportAdapter extends RecyclerView.Adapter<RecyclerHolder>{

    private Context mCtx;
    private Activity act;
    private List<Reports> reportList;
    SwipeRefreshLayout swiper;

    public ReportAdapter(Activity act, Context mCtx, List<Reports> reportList, SwipeRefreshLayout swiper) {
        this.mCtx = mCtx;
        this.act = act;
        this.reportList = reportList;
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
        holder.headTxt.setText(reportList.get(position).getCategory());
        holder.midTxt.setText(reportList.get(position).getRoom_name());
        holder.subTxt.setText("Date: " + reportList.get(position).getDate());
        holder.img.setBackgroundResource(R.drawable.ic_report_orange);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //intent to ViewReport
                Intent intent = new Intent(mCtx, ViewInventoryReport.class);
                intent.putExtra("rep_id", reportList.get(position).getRep_id());
                intent.putExtra("type", "local");
                act.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }
}
