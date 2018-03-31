package com.example.avendano.cp_scan.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.avendano.cp_scan.Model.Task;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.RecyclerHolder.RecyclerHolder;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Avendano on 31 Mar 2018.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder>{
    private Context mCtx;
    private Activity act;
    private List<Task> taskList;
    SwipeRefreshLayout swiper;
    public TaskAdapter(Context mCtx, Activity act, List<Task> taskList, SwipeRefreshLayout swiper) {
        this.mCtx = mCtx;
        this.act = act;
        this.taskList = taskList;
        this.swiper = swiper;
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.task_item, parent, false);
        return new TaskHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {
        Task details = taskList.get(position);
        holder.title.setText(details.getTitle());
        holder.desc.setText(details.getDesc());
        holder.datetime.setText(details.getDate() + " " + details.getTime());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskHolder extends RecyclerView.ViewHolder{

        TextView title,desc, datetime;
        CardView card;
        public TaskHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            desc = (TextView) itemView.findViewById(R.id.description);
            datetime = (TextView) itemView.findViewById(R.id.datetime);
            card = (CardView) itemView.findViewById(R.id.cardview);
        }
    }
}
