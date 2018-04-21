package com.example.avendano.cp_scan.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.Model.Task;
import com.example.avendano.cp_scan.Activities.ViewPc;
import com.example.avendano.cp_scan.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Avendano on 31 Mar 2018.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {
    private Context mCtx;
    private Activity act;
    private List<Task> taskList;
    SwipeRefreshLayout swiper;
    android.app.AlertDialog progress;
    VolleyRequestSingleton volley;

    public TaskAdapter(Context mCtx, Activity act, List<Task> taskList, SwipeRefreshLayout swiper) {
        this.mCtx = mCtx;
        this.act = act;
        this.taskList = taskList;
        this.swiper = swiper;
        volley = new VolleyRequestSingleton(mCtx);
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.task_item, parent, false);
        return new TaskHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, final int position) {
        final Task details = taskList.get(position);
        holder.title.setText(details.getTitle());
        if (details.getDesc().length() == 0)
            holder.desc.setText("'No Message'");
        else
            holder.desc.setText(details.getDesc());

        holder.datetime.setText(details.getDate() + " " + details.getTime());
        if (details.getStatus().equalsIgnoreCase("accepted"))
            holder.status.setText("Status: Pending");
        else
            holder.status.setText("Status: " + details.getStatus());

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if inventory and done -> view inventory report
                //if repair and done -> view requestrepair
            }
        });
        if (details.getStatus().equalsIgnoreCase("accepted") && details.getTitle().equalsIgnoreCase("repair"))
            holder.report.setVisibility(View.VISIBLE);

        holder.report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToViewPc(details.getReqid(), details.getRoom_pc_id());
            }
        });
    }

    private void goToViewPc(final int reqid, final int room_pc_id) {
        class getDetails extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                GoToViewPc();
                return null;
            }

            private void GoToViewPc() {
                volley.sendStringRequestGet(AppConfig.GET_COMPUTERS
                        , new VolleyCallback() {
                            @Override
                            public void onSuccessResponse(String response) {
                                Log.e("RESPONse", response);
                                try {
                                    JSONArray array = new JSONArray(response);
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject obj = array.getJSONObject(i);
                                        int comp_id = obj.getInt("comp_id");
                                        if (room_pc_id == comp_id) {
                                            int room_id = obj.getInt("room_id");
                                            intentToViewPc(room_id, comp_id, reqid);
                                            break;
                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Toast.makeText(mCtx, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            private void intentToViewPc(int room_id, int comp_id, int reqid) {
                Intent intent = new Intent(mCtx, ViewPc.class);
                intent.putExtra("comp_id", comp_id);
                intent.putExtra("room_id", room_id);
                intent.putExtra("req_id", reqid);
                intent.putExtra("request", 1);
                mCtx.startActivity(intent);
            }
        }
        new getDetails().execute();
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskHolder extends RecyclerView.ViewHolder {

        TextView title, desc, datetime, status;
        CardView card;
        Button report;

        public TaskHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            desc = (TextView) itemView.findViewById(R.id.description);
            datetime = (TextView) itemView.findViewById(R.id.datetime);
            status = (TextView) itemView.findViewById(R.id.status);
            card = (CardView) itemView.findViewById(R.id.cardview);
            report = (Button) itemView.findViewById(R.id.report);
        }
    }

}
