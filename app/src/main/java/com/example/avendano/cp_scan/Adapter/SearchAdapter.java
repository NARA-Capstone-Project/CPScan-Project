package com.example.avendano.cp_scan.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Model.Search;
import com.example.avendano.cp_scan.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Avendano on 1 Apr 2018.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchHolder> {
    List<Search> searchList;
    private Context mCtx;
    private Activity act;
    String type;

    public SearchAdapter(List<Search> searchList, Context mCtx, Activity act, String type) {
        this.searchList = searchList;
        this.mCtx = mCtx;
        this.act = act;
        this.type = type;
    }

    @Override
    public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
//        View view = inflater.inflate(R.layout.search_item, parent, false);
        View view = inflater.inflate(R.layout.assess_layout_item, parent, false);
        return new SearchHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchHolder holder, int position) {
        final Search details = searchList.get(position);
        holder.name.setText(details.getName());
        holder.image.setVisibility(View.GONE);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.contains("Repair")) {
                    //popup ng pc list
                    popupPcList(details.getId());
                } else {
                    //return sa task activity
                    goToTaskActivity(details.getId());
                }
            }
        });
    }

    private void popupPcList(final int id) {
        final ArrayList<Integer> comp_ids = new ArrayList();
        final ArrayList<Integer> pc_names = new ArrayList();

        class getComps extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                getComputers();
                return null;
            }

            private void getComputers() {
                StringRequest str = new StringRequest(Request.Method.GET
                        , AppConfig.URL_GET_ALL_PC
                        , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray array = new JSONArray(response);
                            if (array.length() > 0) {
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    int comp_id = obj.getInt("comp_id");
                                    int room_id = 0;
                                    if (!obj.isNull("room_id")) {
                                        room_id = obj.getInt("room_id");
                                    }
                                    int pc_no = obj.getInt("pc_no");

                                    if (room_id == id) {
                                        comp_ids.add(comp_id);
                                        pc_names.add(pc_no);
                                    }
                                }
                                if(pc_names.size() > 0){
                                    popup();
                                }else{
                                    Toast.makeText(mCtx, "No Computer", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("RESULT", "Error: " + error.getMessage());
                    }
                });
                RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);
            }

            private void popup() {
                Integer[] comps = pc_names.toArray(new Integer[pc_names.size()]);
                final Integer[] ids = comp_ids.toArray(new Integer[pc_names.size()]);

                String item= Arrays.toString(comps);

                String items[]=item.substring(1,item.length()-1).split(", ");
                int x= 0;
                for (String i : items){
                    i = "PC " + i;
                    items[x] = i;
                    x++;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
                builder.setTitle("Select Computer...")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goToTaskActivity(ids[which]);
                            }
                        })
                        .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.setCancelable(false);
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        new getComps().execute();

    }

    private void goToTaskActivity(int id) {
        Intent intent = new Intent();
        intent.putExtra("id", id);
        act.setResult(Activity.RESULT_OK, intent);
        act.finish();
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }


    class SearchHolder extends RecyclerView.ViewHolder {

        TextView name;
        CardView card;
        ImageView image;

        public SearchHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.pc_name);
            image = (ImageView) itemView.findViewById(R.id.check);
            card = (CardView) itemView.findViewById(R.id.cardview);
        }
    }
}
