package com.example.avendano.cp_scan.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.CamcorderProfile;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Activities.EditRequestSchedule;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.Network_Handler.Connection_Detector;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.RequestQueueHandler;
import com.example.avendano.cp_scan.Model.RequestRepair;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;
import com.google.zxing.client.result.ResultParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

/**
 * Created by Avendano on 1 Apr 2018.
 */

public class RepairAdapter extends RecyclerView.Adapter<RepairAdapter.RepairViewHolder> {
    List<RequestRepair> repairList;
    Context mCtx;
    Activity act;
    SwipeRefreshLayout swiper;
    String name = "";
    Connection_Detector connection_detector;
    //    SQLiteHandler db;
    android.app.AlertDialog progress;
    VolleyRequestSingleton volley;
    String reason;

    public RepairAdapter(List<RequestRepair> repairList, Context mCtx, Activity act, SwipeRefreshLayout swiper) {
        this.repairList = repairList;
        this.mCtx = mCtx;
        this.act = act;
        this.swiper = swiper;
//        db = new SQLiteHandler(mCtx);
        volley = new VolleyRequestSingleton(mCtx);
        connection_detector = new Connection_Detector(mCtx);
        reason = "";
    }

    @Override
    public RepairViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View v = inflater.inflate(R.layout.req_list_item, null);
        return new RepairViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RepairViewHolder holder, final int position) {
        final RequestRepair repair = repairList.get(position);
        holder.category.setText(repair.getCategory());
        if (SharedPrefManager.getInstance(mCtx).getUserRole().equalsIgnoreCase("custodian")) {
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setText("Status: " + repair.getReq_status());
            holder.btn_container.setVisibility(View.GONE);
        }
        getPcDetails(repair.getComp_id(), holder.location);
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new SpotsDialog(mCtx, "Loading...");
                progress.setCancelable(false);
                updateRequest(repair.getReq_id(), "accept", position);
            }
        });
        holder.ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog(repair.getReq_id(), position);
            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = repair.getMsg();
                String msg_body = "", cancel_rem = "";
                if(!repair.getCancel_remarks().isEmpty())
                    cancel_rem = "\n\nCancellation Note: " + repair.getCancel_remarks();

                if (msg.length() == 0) {
                    msg_body = "Date requested: " + repair.getDate_req() + "\nTime Requested: " + repair.getTime_req()
                            + "\nAssigned Date: " + repair.getDate() + "\nAssigned Time: " + repair.getTime() + "\nReport Details: "
                            + repair.getRep_details() + "\nRequest Status: " + repair.getReq_status() + cancel_rem;
                } else {
                    msg_body = "Date requested: " + repair.getDate_req() + "\nTime Requested: " + repair.getTime_req()
                            + "\nAssigned Date: " + repair.getDate() + "\nAssigned Time: " + repair.getTime() + "\nReport Details: "
                            + repair.getRep_details() + "\nRequest Status: " + repair.getReq_status()
                            + "\n\nMessage: " + msg  + cancel_rem;
                }
                    showDetails(msg_body, repair.getImage_path(), repair.getReq_id(), repair.getReq_status(), position, repair.getComp_id());
            }
        });
    }

    private void updateDialog(final int req_id, final int position) {
        final Spinner reasons;
        final EditText custom;
        final Dialog dialog = new Dialog(mCtx);
        TextView diag_msg;
        dialog.setContentView(R.layout.cancel_dialog);
        custom = (EditText) dialog.findViewById(R.id.custom);
        reasons = (Spinner) dialog.findViewById(R.id.reasons);
        diag_msg = (TextView) dialog.findViewById(R.id.txt_msg);
        Button save = (Button) dialog.findViewById(R.id.save);
        Button cancel = (Button) dialog.findViewById(R.id.cancel);
        diag_msg.setText("Input the reason of declining this request: ");

        String items[] = new String[]{"Computer is still working", "Can't Repair", "Computer is Missing", "Others..."};
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
                //if pos == 3 = check if may laman ung custom text, update status to ignored
                if (reasons.getSelectedItemPosition() == 3) {
                    if (custom.getText().toString().trim().isEmpty()) {
                        custom.setError("Empty Field!");
                    } else {
                        dialog.dismiss();
                        progress = new SpotsDialog(mCtx, "Loading...");
                        progress.setCancelable(false);
                        reason = custom.getText().toString().trim();
                        updateRequest(req_id, "decline", position);
                    }
                }else
                {
                    dialog.dismiss();
                    progress = new SpotsDialog(mCtx, "Loading...");
                    progress.setCancelable(false);
                    updateRequest(req_id, "decline", position);
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setTitle("Decline Request");
        dialog.show();
    }

    private void updateRequest(final int req_id, final String btn_clicked, final int position) {
        class UpdateRequest extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                update();
                return null;
            }

            private void update() {
                String query = "";
                if(reason.contains("'"))
                    reason = reason.replace("'", "\''");

                if (btn_clicked.equalsIgnoreCase("decline"))
                    query = "UPDATE request_repair SET req_status = 'Declined', cancel_remarks = '"+reason+"' WHERE req_id = ?";
                else
                    query = "UPDATE request_repair SET req_status = 'Accepted' WHERE req_id = ?";

                final String finalQuery = query;
                StringRequest str = new StringRequest(Request.Method.POST
                        , AppConfig.URL_UPDATE_SCHEDULE
                        , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("ERROR", response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                repairList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeRemoved(position, repairList.size());
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        progress.dismiss();
                                    }
                                }, 3000);

                            } else
                                Toast.makeText(mCtx, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mCtx, "Can't Connect to the server", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("query", finalQuery);
                        params.put("id", String.valueOf(req_id));
                        return params;
                    }
                };
                RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);
            }
        }

        new UpdateRequest().execute();
    }

    private void getPcDetails(final int comp_id, final TextView location) {
        //comp name and room id
        volley.sendStringRequestGet(AppConfig.GET_COMPUTERS, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int c_id = obj.getInt("comp_id");
                        if (c_id == comp_id) {
                            int pc_no = obj.getInt("pc_no");
                            String room_name = obj.getString("room_name");
                            location.setText("PC " + pc_no + " of Room " + room_name);
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
            }
        });
    }

    private void showDetails(String msg_body, final String image_path, final int req_id, final String stat, final int position, final int comp_id) {
        //if custodian may edit request
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setTitle("Request Details");
        builder.setMessage(msg_body);

        if (!image_path.isEmpty()) {
            builder.setNeutralButton("View Image", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.ROOT + image_path))); /** replace with your own uri */
                }
            });
        }

        if (SharedPrefManager.getInstance(mCtx).getUserRole().equalsIgnoreCase("custodian")) {
            if (stat.equalsIgnoreCase("pending")) {
                builder.setNegativeButton("Cancel Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelRequestRepair(req_id, position);
                    }
                });
            }
            if (!(stat.equalsIgnoreCase("accepted")) || stat.equalsIgnoreCase("done")) {
                builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //intent to edit request
                        dialog.dismiss();
                        Intent intent = new Intent(mCtx, EditRequestSchedule.class);
                        intent.putExtra("type", "repair");
                        intent.putExtra("room_pc_id", comp_id);
                        intent.putExtra("id", req_id);
                        intent.putExtra("status", stat);
                        act.startActivity(intent);
                    }
                });
            }
            if (stat.equalsIgnoreCase("accepted") || stat.equalsIgnoreCase("done")) {

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        } else {
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }


        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public int getItemCount() {
        return repairList.size();
    }

    class RepairViewHolder extends RecyclerView.ViewHolder {

        TextView category;
        TextView location;
        Button accept, ignore;
        CardView card;
        LinearLayout btn_container;
        TextView status;

        public RepairViewHolder(View itemView) {
            super(itemView);

            category = (TextView) itemView.findViewById(R.id.category);
            location = (TextView) itemView.findViewById(R.id.location);
            accept = (Button) itemView.findViewById(R.id.accept);
            ignore = (Button) itemView.findViewById(R.id.negative);
            card = (CardView) itemView.findViewById(R.id.cardview);
            btn_container = (LinearLayout) itemView.findViewById(R.id.linear);
            status = (TextView) itemView.findViewById(R.id.req_status);
        }
    }

    private void cancelRequestRepair(final int req_id, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setTitle("Cancel Request");
        builder.setMessage("Are you sure you want to cancel your request?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (connection_detector.isConnected())
                            cancelRequest(req_id, position);
                        else
                            Toast.makeText(mCtx, "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void cancelRequest(final int req_id, final int position) {
        class cancel {
            void callCancel() {
                new cancelling().execute();
            }

            class cancelling extends AsyncTask<Void, Void, Void> {


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
                                repairList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeRemoved(position, repairList.size());
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
                        Toast.makeText(mCtx, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> param = new HashMap<>();
                        param.put("id", String.valueOf(req_id));
                        param.put("req_type", "repair");
                        return param;
                    }
                };
                RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);
            }
        }
        new cancel().callCancel();
    }

}
