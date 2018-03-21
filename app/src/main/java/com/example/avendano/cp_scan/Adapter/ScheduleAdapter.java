package com.example.avendano.cp_scan.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.avendano.cp_scan.Model.RoomSchedule;
import com.example.avendano.cp_scan.R;

import java.util.List;

/**
 * Created by Avendano on 7 Mar 2018.
 */

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.SchedViewHolder>{

    Context mCtx;
    List<RoomSchedule> schedList;

    public ScheduleAdapter(Context mCtx, List<RoomSchedule> schedList) {
        this.mCtx = mCtx;
        this.schedList = schedList;
    }

    @Override
    public SchedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.schedule_item, null);

        return new SchedViewHolder(view);

    }

    @Override
    public void onBindViewHolder(SchedViewHolder holder, int position) {
        RoomSchedule sched = schedList.get(position);
        holder.sched_time.setText(""+sched.getFromTime() + " - " + sched.getToTime());
        holder.sched_prof.setText(sched.getProf());
    }

    @Override
    public int getItemCount() {
        return schedList.size();
    }

    public class SchedViewHolder extends RecyclerView.ViewHolder{
        TextView sched_prof;
        TextView sched_time;

        public SchedViewHolder(View itemView) {
            super(itemView);
            sched_prof = (TextView) itemView.findViewById(R.id.sched_prof);
            sched_time = (TextView) itemView.findViewById(R.id.sched_time);
        }
    }
}
