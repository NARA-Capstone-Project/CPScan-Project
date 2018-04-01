package com.example.avendano.cp_scan.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Model.RequestInventory;
import com.example.avendano.cp_scan.R;

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
    SQLiteHandler db;
    android.app.AlertDialog progress;

    public InventoryAdapter(List<RequestInventory> inventoryList, Context mCtx, Activity act, SwipeRefreshLayout swiper) {
        this.inventoryList = inventoryList;
        this.mCtx = mCtx;
        this.act = act;
        this.swiper = swiper;
        db = new SQLiteHandler(mCtx);
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
        holder.category.setText(inventory.getCategory());
        holder.location.setText(getRoomLocation(inventory.getRoom_id()));
        holder.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog(inventory.getReq_id(), "done", position);
            }
        });
        holder.ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog(inventory.getReq_id(), "ignore", position);
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
                showDetails(msg_body);
            }
        });
    }

    private void updateDialog(final int req_id, final String button, final int position) {
        String msg = "";
        if(button.equalsIgnoreCase("ignore")){
            msg = "Are you sure you want to ignore this request?";
        }else{
            msg = "Click yes to confirm action";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setCancelable(false);
        builder.setMessage(msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Connection_Detector connection_detector = new Connection_Detector(mCtx);
                        if (connection_detector.isConnected()) {
                            progress = new SpotsDialog(mCtx, "Loading...");
                            progress.setCancelable(false);
                            progress.show();
                            updateRequest(req_id, button, position);
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

    private void updateRequest(final int req_id, final String button, final int position) {
        class UpdateRequest extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                update();
                return null;
            }

            private void update() {
                String query = "";
                if (button.equalsIgnoreCase("done"))
                    query = "UPDATE request_inventory SET req_status = 'Done' WHERE req_id = ?";
                else
                    query = "UPDATE request_inventory SET req_status = 'Ignored' WHERE req_id = ?";

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

    private String getRoomLocation(final int id) {
        Cursor c = db.getRoomDetails(id);
        if (c.moveToFirst()) {
            String room_name = c.getString(c.getColumnIndex(db.ROOMS_NAME));
            String building = c.getString(c.getColumnIndex(db.ROOMS_BUILDING));

            return room_name + " in " + building;
        } else {
            return "No data of Room";
        }
    }

    private void showDetails(String msg_body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setTitle("Request Details");
        builder.setMessage(msg_body);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    class InventoryViewHolder extends RecyclerView.ViewHolder {

        TextView category;
        TextView location;
        Button done, ignore;
        CardView card;

        public InventoryViewHolder(View itemView) {
            super(itemView);

            category = (TextView) itemView.findViewById(R.id.category);
            location = (TextView) itemView.findViewById(R.id.location);
            done = (Button) itemView.findViewById(R.id.done);
            ignore = (Button) itemView.findViewById(R.id.ignore);
            card = (CardView) itemView.findViewById(R.id.cardview);
        }
    }
}
