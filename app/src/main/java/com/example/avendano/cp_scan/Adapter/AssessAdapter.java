package com.example.avendano.cp_scan.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    public AssessAdapter(Context mCtx, List<Assess_Computers> pcList) {
        this.mCtx = mCtx;
        this.pcList = pcList;
    }

    @Override
    public AssessHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.assess_layout_item, null);

        return new AssessHolder(view, mCtx, pcList);

    }

    @Override
    public void onBindViewHolder(AssessHolder holder, int position) {
        Assess_Computers comps = pcList.get(position);
        holder.pc.setText("PC " + comps.getPc_no());
        if (comps.getScanned() == 0) {
            holder.img.setBackgroundResource(R.drawable.ic_pending);
        } else {
            holder.img.setBackgroundResource(R.drawable.ic_check);
        }

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

        public AssessHolder(View itemView, final Context ctx, final List<Assess_Computers> scan_computers) {
            super(itemView);
            this.ctx = ctx;
            this.scan_computers = scan_computers;
            pc = (TextView) itemView.findViewById(R.id.pc_name);
            img = (ImageView) itemView.findViewById(R.id.check);
        }
    }
}
