package com.example.avendano.cp_scan.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import com.example.avendano.cp_scan.Model.RequestRepair;
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

public class RepairAdapter extends RecyclerView.Adapter<RepairAdapter.RepairViewHolder> {
    List<RequestRepair> repairList;
    Context mCtx;
    Activity act;
    SwipeRefreshLayout swiper;
    SQLiteHandler db;
    android.app.AlertDialog progress;

    public RepairAdapter(List<RequestRepair> repairList, Context mCtx, Activity act, SwipeRefreshLayout swiper) {
        this.repairList = repairList;
        this.mCtx = mCtx;
        this.act = act;
        this.swiper = swiper;
        db = new SQLiteHandler(mCtx);
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
        holder.location.setText(getPcDetails(repair.getComp_id()));
        holder.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog(repair.getReq_id(), "done", position);
            }
        });
        holder.ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog(repair.getReq_id(), "ignore", position);
            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = repair.getMsg();
                String msg_body = "";
                if (msg.length() == 0) {
                    msg_body = "Date requested: " + repair.getDate_req() + "\nTime Requested: " + repair.getTime_req()
                            + "\nAssigned Date: " + repair.getDate() + "\nAssigned Time: " + repair.getTime() + "\nReport Details: "
                            + repair.getRep_details() + "\nRequest Status: " + repair.getReq_status();
                } else {
                    msg_body = "Date requested: " + repair.getDate_req()  + "\nTime Requested: " + repair.getTime_req()
                            + "\nAssigned Date: " + repair.getDate() + "\nAssigned Time: " + repair.getTime() + "\nReport Details: "
                            + repair.getRep_details() + "\nRequest Status: " + repair.getReq_status()
                            + "\n\nMessage: " + msg;
                }
                showDetails(msg_body, repair.getImage_path());
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
                    query = "UPDATE request_repair SET req_status = 'Done' WHERE req_id = ?";
                else
                    query = "UPDATE request_repair SET req_status = 'Ignored' WHERE req_id = ?";

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

    private String getPcDetails(final int id) {
        Cursor c = db.getCompDetails(id);
        if (c.moveToFirst()) {
            int pc_name = c.getInt(c.getColumnIndex(db.COMP_NAME));
            int room_id = c.getInt(c.getColumnIndex(db.ROOMS_ID));
            String room_name = "";

            Cursor r = db.getRoomDetails(room_id);
            if(r.moveToFirst()){
                room_name = " in " + r.getString(r.getColumnIndex(db.ROOMS_NAME));
            }
            return "PC " + pc_name +  room_name;
        } else {
            return "No data of Computer";
        }
    }

    private void showDetails(String msg_body, final String image_path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setTitle("Request Details");
        builder.setMessage(msg_body);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if(!image_path.isEmpty()){
            builder.setNeutralButton("View Image", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.ROOT_URL + image_path))); /** replace with your own uri */
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
        Button done, ignore;
        CardView card;

        public RepairViewHolder(View itemView) {
            super(itemView);

            category = (TextView) itemView.findViewById(R.id.category);
            location = (TextView) itemView.findViewById(R.id.location);
            done = (Button) itemView.findViewById(R.id.done);
            ignore = (Button) itemView.findViewById(R.id.ignore);
            card = (CardView) itemView.findViewById(R.id.cardview);
        }
    }
}
