package com.example.avendano.cp_scan.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.avendano.cp_scan.Network_Handler.Connection_Detector;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.RequestQueueHandler;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.Model.RequestInventory;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

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

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    List<RequestInventory> inventoryList;
    Context mCtx;
    Activity act;
    SwipeRefreshLayout swiper;
    //    SQLiteHandler db;
    android.app.AlertDialog progress;
    VolleyRequestSingleton volley;
    Connection_Detector connection_detector;
    String reason;

    public InventoryAdapter(List<RequestInventory> inventoryList, Context mCtx, Activity act, SwipeRefreshLayout swiper) {
        this.inventoryList = inventoryList;
        this.mCtx = mCtx;
        this.act = act;
        this.swiper = swiper;
        volley = new VolleyRequestSingleton(mCtx);
        connection_detector = new Connection_Detector(mCtx);
//        db = new SQLiteHandler(mCtx);
        reason = "";
    }

    @Override
    public InventoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View v = inflater.inflate(R.layout.req_list_item, null);
        return new InventoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InventoryViewHolder holder, final int position) {
        final RequestInventory inventory = inventoryList.get(position);
        if (SharedPrefManager.getInstance(mCtx).getUserRole().equalsIgnoreCase("custodian")) {
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setText("Status: " + inventory.getReq_status());
            holder.btn_container.setVisibility(View.GONE);
        }
        getRoomName(inventory.getRoom_id(), holder.location);
        holder.category.setText(inventory.getCategory());
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new SpotsDialog(mCtx, "Loading...");
                progress.setCancelable(false);
                updateRequest(inventory.getReq_id(), "accept", position);
            }
        });
        holder.ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog(inventory.getReq_id(), position);
            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = inventory.getMsg();
                String msg_body = "";
                if (msg.length() > 0) {
                    msg_body = "Date requested: " + inventory.getDate_req() + "\nTime Requested: " + inventory.getTime_req()
                            + "\nAssigned Date: " + inventory.getDate() + "\nAssigned Time: " + inventory.getTime() + "\nRequest Status: "
                            + inventory.getReq_status() + "\n\nMessage: " + msg;
                } else {
                    msg_body = "Date requested: " + inventory.getDate_req() + "\nTime Requested: " + inventory.getTime_req()
                            + "\nAssigned Date: " + inventory.getDate() + "\nAssigned Time: " + inventory.getTime()
                            + "\nRequest Status: " + inventory.getReq_status();
                }
                showDetails(msg_body, inventory.getReq_id(), inventory.getRoom_id(), position, inventory.getReq_status());
            }
        });
    }

    private void getRoomName(final int room_id, final TextView location) {
        volley.sendStringRequestGet(AppConfig.GET_ROOMS, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        if (room_id == obj.getInt("room_id")) {
                            if (obj.isNull("dept_id")) {
                                location.setText(obj.getString("room_name"));
                            } else {
                                location.setText(obj.getString("dept_name") + " " + obj.getString("room_name"));
                            }
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
        diag_msg.setText("Input the reason of ignoring this request: ");

        String items[] = new String[]{"I'm Busy", "I'm not in University", "I'm not available", "Others..."};
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
                    if (reason.trim().isEmpty()) {
                        custom.setError("Empty Field!");
                    } else {
                        updateRequest(req_id, "ignore", position);
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setTitle("Ignore Request");
        dialog.show();
    }

    private void updateRequest(final int req_id, final String button, final int position) {
        class UpdateRequest extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                updateRequest(button);
                return null;
            }

            private void updateRequest(String btn_clicked) {
                String query = "";
                if (btn_clicked.equalsIgnoreCase("ignore"))
                    query = "UPDATE request_inventory SET req_status = 'Ignored', cancel_remarks = '"+reason+"' WHERE req_id = ?";
                else
                    query = "UPDATE request_inventory SET req_status = 'Accepted' WHERE req_id = ?";

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
                                inventoryList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeRemoved(position, inventoryList.size());
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        progress.dismiss();
                                    }
                                }, 2000);

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

    private void showDetails(String msg_body, final int req_id, final int room_id, final int position, final String stat) {
        //pending ignored accepted resched missed
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setTitle("Request Details");
        builder.setMessage(msg_body);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (SharedPrefManager.getInstance(mCtx).getUserRole().equalsIgnoreCase("custodian")) {
            if (stat.equalsIgnoreCase("pending")) //pending
                builder.setNeutralButton("Cancel Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        cancelRequestInventory(req_id, position);
                    }
                });
            //missed need to resched pending ignored
            if (!(stat.equalsIgnoreCase("Accepted") || stat.equalsIgnoreCase("done"))) {
                builder.setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(mCtx, EditRequestSchedule.class);
                        intent.putExtra("type", "inventory");
                        intent.putExtra("room_pc_id", room_id);
                        intent.putExtra("id", req_id);
                        intent.putExtra("status", stat);
                        act.startActivity(intent);
                    }
                });
            }
        }
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void cancelRequestInventory(final int req_id, final int position) {
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
                                inventoryList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeRemoved(position, inventoryList.size());
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
                        param.put("req_type", "inventory");
                        return param;
                    }
                };
                RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);
            }
        }
        new cancel().callCancel();
    }


    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    class InventoryViewHolder extends RecyclerView.ViewHolder {

        TextView category;
        TextView location;
        Button accept, ignore;
        CardView card;
        LinearLayout btn_container;
        TextView status;

        public InventoryViewHolder(View itemView) {
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
}
