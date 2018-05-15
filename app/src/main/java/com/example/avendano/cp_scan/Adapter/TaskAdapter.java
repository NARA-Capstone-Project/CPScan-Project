package com.example.avendano.cp_scan.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.Model.Task;
import com.example.avendano.cp_scan.Activities.ViewPc;
import com.example.avendano.cp_scan.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Avendano on 31 Mar 2018.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {
    private Context mCtx;
    private Activity act;
    private List<Task> taskList;
    private String report_type;
    SwipeRefreshLayout swiper;
    android.app.AlertDialog progress;
    VolleyRequestSingleton volley;
    String reason;

    public TaskAdapter(Context mCtx, Activity act, List<Task> taskList, SwipeRefreshLayout swiper) {
        this.mCtx = mCtx;
        this.act = act;
        this.taskList = taskList;
        this.swiper = swiper;
        volley = new VolleyRequestSingleton(mCtx);
        reason = "";
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
        if (details.getStatus().equalsIgnoreCase("accepted") && details.getTitle().contains("PC")) {
            //repair
            report_type = "repair";
        } else {
            report_type = "inventory";
        }
        holder.title.setText(details.getTitle());
        if (details.getDesc().length() == 0)
            holder.desc.setText("'No Message'");
        else
            holder.desc.setText(details.getDesc());

        holder.datetime.setText(details.getDate() + " " + details.getTime());
        if (details.getStatus().equalsIgnoreCase("accepted"))
            holder.status.setText("Status: Pending");

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if inventory and done -> view inventory report
                //if repair and done -> view requestrepair
                //details
                String msg = "";
                String det_msg = (details.getDesc().trim().isEmpty()) ? "'No Message'" : details.getDesc().trim();
                msg = (report_type.equalsIgnoreCase("repair")) ?
                        "Assigned Date: " + details.getDate() + "\nAssigned Time: " + details.getTime() + "\nComputer: "
                                + details.getTitle() + "\nStatus: " + details.getStatus()
                                + "\n\nMessage: " + det_msg
                        :
                        "Assigned Date: " + details.getDate() + "\nAssigned Time: " + details.getTime() + "\nRoom: "
                                + details.getTitle() + "\nStatus: " + details.getStatus()
                                + "\n\nMessage: " + det_msg;
                showDetails(msg, report_type, details.getStatus(), details.getReqid(), position);
            }
        });
        if (report_type.equalsIgnoreCase("repair")) {
            holder.report.setVisibility(View.VISIBLE);
        }

        holder.report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToViewPc(details.getReqid(), details.getRoom_pc_id());
            }
        });
    }

    private void showDetails(String message, final String type, String stat, final int req_id, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

        builder.setTitle("Schedule Details")
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel Schedule", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showCancelDialog(type, req_id, position);

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showCancelDialog(final String type, final int req_id, final int position) {
        final Spinner reasons;
        final EditText custom;
        TextView diag_msg;
        final Dialog dialog = new Dialog(mCtx);
        dialog.setContentView(R.layout.cancel_dialog);
        custom = (EditText) dialog.findViewById(R.id.custom);
        diag_msg = (TextView) dialog.findViewById(R.id.txt_msg);
        reasons = (Spinner) dialog.findViewById(R.id.reasons);
        Button save = (Button) dialog.findViewById(R.id.save);
        Button cancel = (Button) dialog.findViewById(R.id.cancel);

        diag_msg.setText("Input the reason of cancelling schedule: ");

        String items[];
        if (type.equalsIgnoreCase("repair"))
            items = new String[]{"Computer is still working", "Can't Repair", "PC Missing", "Others..."};
        else
            items = new String[]{"I'm Busy", "I'm not in University", "I'm not available", "Others..."};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mCtx, R.layout.support_simple_spinner_dropdown_item, items);
        reasons.setAdapter(adapter);
        reasons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 3)
                    custom.setVisibility(View.VISIBLE);
                else {
                    reason = reasons.getSelectedItem().toString().trim();
                    custom.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (reasons.getSelectedItemPosition() == 3)
            reason = custom.getText().toString().trim();
        else
            reason = reasons.getSelectedItem().toString().trim();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mCtx, "Reason: " + reason, Toast.LENGTH_SHORT).show();
                //if pos == 3 = check if may laman ung custom text, update status to ignored
                if (reasons.getSelectedItemPosition() == 3) {
                    if (custom.getText().toString().trim().isEmpty()) {
                        custom.setError("Empty Field!");
                    } else {
                        dialog.dismiss();
                        reason = custom.getText().toString().trim();
                        ignoreSchedule(req_id, position);
                    }
                }else
                {
                    dialog.dismiss();
                    ignoreSchedule(req_id, position);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setTitle("Cancel Schecule");
        dialog.show();
    }

    private void ignoreSchedule(final int req_id, final int position) {

        if(reason.contains("'"))
            reason = reason.replace("'", "\''");

        class IgnoringProcess extends AsyncTask<Void, Void, String> {
            String query = "";
            Map<String, String> param = new HashMap<>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                query = "UPDATE request_repair SET req_status = 'Ignored', cancel_remarks = '" + reason + "' WHERE req_id = " + req_id + "";
                param.put("query", query);
            }

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLCon con = new HttpURLCon();
                String response = con.sendPostRequest(AppConfig.UPDATE_QUERY, param);
                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (!obj.getBoolean("error")) {
                        //taskadapter notifydatachange
                        taskList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeRemoved(position, taskList.size());
                    } else {
                        Toast.makeText(mCtx, "An error occurred while processing your request.", Toast.LENGTH_SHORT).show();
                        Log.e("RESPONSE", obj.getString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mCtx, "An error occurred while processing your request.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        new IgnoringProcess().execute();
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
