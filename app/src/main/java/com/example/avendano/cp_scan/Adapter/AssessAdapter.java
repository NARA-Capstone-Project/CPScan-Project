package com.example.avendano.cp_scan.Adapter;

import android.content.Context;
import android.content.DialogInterface;
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

import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Database.SQLiteHelper;
import com.example.avendano.cp_scan.Model.Assess_Computers;
import com.example.avendano.cp_scan.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Avendano on 8 Mar 2018.
 */

public class AssessAdapter extends RecyclerView.Adapter<AssessAdapter.AssessHolder> {

    private Context mCtx;
    private List<Assess_Computers> pcList;
    private SQLiteHelper db;

    public AssessAdapter(Context mCtx, List<Assess_Computers> pcList) {
        this.mCtx = mCtx;
        this.pcList = pcList;
        db = new SQLiteHelper(mCtx);
    }

    @Override
    public AssessHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.assess_layout_item, null);

        return new AssessHolder(view, mCtx, pcList);

    }

    @Override
    public void onBindViewHolder(final AssessHolder holder, final int position) {
        final Assess_Computers comps = pcList.get(position);
        holder.pc.setText("PC " + comps.getPc_no());
        if (comps.getScanned() == 0) {
            holder.img.setBackgroundResource(R.drawable.ic_pending);
        } else {
            holder.img.setBackgroundResource(R.drawable.ic_check);
        }
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comps.getScanned() == 0) {
                    //alert kung missing ung computer
                    AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
                    builder.setTitle("Missing Computer")
                            .setMessage("PC " + comps.getPc_no() + " with serial number: " + comps.getMb()
                                    + " or " + comps.getMonitor() + " missing?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    long in = db.addAssessedPc(comps.getComp_id(), comps.getPc_no(), comps.getModel(),
                                            comps.getComp_serial(),
                                            "PC MISSING", comps.getMb()
                                            , "PC MISSING", "PC MISSING", comps.getMonitor(),
                                            "PC MISSING", "PC MISSING",
                                            "PC MISSING", "MISSING"
                                            , "PC MISSING", "PC MISSING");

                                    db.updateScannedStatus(1, comps.getComp_id());
                                    comps.setScanned(1);
                                    notifyDataSetChanged();
                                    Log.e("MISSING PC", " STATUS: " + in);
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
            }
        });
    }

    @Override
    public int getItemCount() {
        return pcList.size();
    }

    public class AssessHolder extends RecyclerView.ViewHolder {

        TextView pc;
        ImageView img;
        List<Assess_Computers> scan_computers = new ArrayList<Assess_Computers>();
        Context ctx;
        CardView card;

        public AssessHolder(View itemView, final Context ctx, final List<Assess_Computers> scan_computers) {
            super(itemView);
            this.ctx = ctx;
            this.scan_computers = scan_computers;
            card = (CardView) itemView.findViewById(R.id.cardview);
            pc = (TextView) itemView.findViewById(R.id.pc_name);
            img = (ImageView) itemView.findViewById(R.id.check);
        }
    }
}
