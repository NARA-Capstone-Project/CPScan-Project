package com.example.avendano.cp_scan.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Activities.TaskActivity;
import com.example.avendano.cp_scan.Activities.ViewPc;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Model.Task;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.RecyclerHolder.RecyclerHolder;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

/**
 * Created by Avendano on 31 Mar 2018.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {
    private Context mCtx;
    private Activity act;
    private List<Task> taskList;
    SwipeRefreshLayout swiper;
    android.app.AlertDialog progress;
    SQLiteHandler db;

    public TaskAdapter(Context mCtx, Activity act, List<Task> taskList, SwipeRefreshLayout swiper, SQLiteHandler db) {
        this.mCtx = mCtx;
        this.act = act;
        this.taskList = taskList;
        this.db= db;
        this.swiper = swiper;
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
        if(details.getDesc().length() == 0)
            holder.desc.setText("'No description'");
        else
            holder.desc.setText(details.getDesc());

        holder.datetime.setText(details.getDate() + " " + details.getTime());
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToTaskActivity(details.getSched_id());
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTask(details.getSched_id(), position);
            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetails(details.getSched_id());
            }
        });
    }

    private void goToTaskActivity(int sched_id) {
        Intent intent = new Intent(mCtx, TaskActivity.class);
        intent.putExtra("sched_id", sched_id);
        intent.putExtra("type", "edit");
        act.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskHolder extends RecyclerView.ViewHolder {

        TextView title, desc, datetime, edit, delete;
        CardView card;

        public TaskHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            desc = (TextView) itemView.findViewById(R.id.description);
            datetime = (TextView) itemView.findViewById(R.id.datetime);
            edit = (TextView) itemView.findViewById(R.id.edit);
            delete = (TextView) itemView.findViewById(R.id.delete);
            card = (CardView) itemView.findViewById(R.id.cardview);
        }
    }

    private void showDetails(final int sched_id) {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_TASK
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    if (array.length() > 0) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int id = obj.getInt("sched_id");
                            final String obj_title = obj.getString("category");
                            String obj_desc = obj.getString("desc");
                            String obj_date = obj.getString("date");
                            String obj_time = obj.getString("time");
                            final int room_pc_id = obj.getInt("id");

                            if (sched_id == id) {
                                if(obj_desc.isEmpty()){
                                    obj_desc = "No description";
                                }
                                showDialog(obj_title, obj_desc, obj_date, obj_time, room_pc_id);
                                break;
                            }
                        }
                    } else {
                        Toast.makeText(mCtx, "No Tasks", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(mCtx, "Error occurred", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mCtx, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                act.finish();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("tech_id", SharedPrefManager.getInstance(mCtx).getUserId());
                return param;
            }
        };
        RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);
    }

    private void showDialog(String obj_title, String obj_desc, String obj_date, String obj_time, int room_pc_id) {
        String room_pc_detail = room_pc_details(room_pc_id, obj_title);

        String msg ="Category: " + obj_title
                + "\nDescription: " + obj_desc
                + "\nRoom/PC: " + room_pc_detail
                + "\nDate: " + obj_date
                +"\nTime: " + obj_time;

        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setCancelable(false);
        builder.setTitle("Task Details...");
        builder.setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private String room_pc_details(int room_pc_id, String cat) {

        String value = "";
        if (cat.contains("Repair")) {
            Cursor c = db.getCompDetails(room_pc_id);
            if(c.moveToFirst()){
                String pc_name = "PC " + c.getString(c.getColumnIndex(db.COMP_NAME));
                int room_id = c.getInt(c.getColumnIndex(db.ROOMS_ID));
                String room_name = "";
                Cursor c1 = db.getRoomDetails(room_id);
                if(c1.moveToFirst()){
                    room_name = c1.getString(c1.getColumnIndex(db.ROOMS_NAME));
                }
                value = pc_name + " of room " + room_name;
            }
        } else {
            Cursor c = db.getRoomDetails(room_pc_id);
            String room_name = "";
            if(c.moveToFirst()){
                room_name = c.getString(c.getColumnIndex(db.ROOMS_NAME));
            }
            value = room_name + " Room";
        }
    return value;
    }

    private void deleteTask(final int sched_id, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setCancelable(false);
        builder.setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Connection_Detector connection_detector = new Connection_Detector(mCtx);
                        if (connection_detector.isConnected()) {
                            progress = new SpotsDialog(mCtx, "Deleting...");
                            progress.setCancelable(false);
                            progress.show();
                            delete(sched_id, position);
                        } else
                            Toast.makeText(mCtx, "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void delete(final int sched_id, final int position) {
        class cancel {
            void callCancel() {
                new cancelling().execute();
            }

            class cancelling extends AsyncTask<Void, Void, Void> {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    cancelRequest();
                    return null;
                }
            }

            private void cancelRequest() {
                StringRequest str = new StringRequest(Request.Method.POST
                        , AppConfig.URL_CANCEL_SCHEDULE
                        , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            //update sqlite
                            if (!obj.getBoolean("error")) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        progress.dismiss();
                                        taskList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeRemoved(position, taskList.size());
                                    }
                                }, 5000);
                            } else {
                                Toast.makeText(mCtx, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(mCtx, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                            Log.e("JSONERROR", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        Toast.makeText(mCtx, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> param = new HashMap<>();
                        param.put("id", String.valueOf(sched_id));
                        param.put("req_type", "schedule");
                        return param;
                    }
                };
                RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);
            }
        }
        new cancel().callCancel();
    }
}
