package com.example.avendano.cp_scan.Adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.avendano.cp_scan.Model.ReportDetails;
import com.example.avendano.cp_scan.R;
import com.github.aakira.expandablelayout.ExpandableLayout;
import com.github.aakira.expandablelayout.ExpandableLayoutListener;
import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.github.aakira.expandablelayout.Utils;
import com.journeyapps.barcodescanner.Util;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Avendano on 21 Mar 2018.
 */

public class ReportDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    List<ReportDetails> reportDetailsList;
    Context mCtx;
    SparseBooleanArray expandState = new SparseBooleanArray();

    public ReportDetailsAdapter(List<ReportDetails> reportDetailsList) {
        this.reportDetailsList = reportDetailsList;
        for(int i=0; i< reportDetailsList.size(); i++){
            expandState.append(i, false);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(reportDetailsList.get(position).isExpandable())
            return 1;
        else
            return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mCtx = parent.getContext();
        if(viewType == 0){ // without children
            LayoutInflater inflater = LayoutInflater.from(mCtx);
            View v = inflater.inflate(R.layout.report_details_without_children, null);
            return new ViewHolderWithoutChildren(v);
        }else{
            LayoutInflater inflater = LayoutInflater.from(mCtx);
            View v = inflater.inflate(R.layout.report_details_with_children, null);
            return new ViewHolderWithChildren(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()){
            case 0:{
                ViewHolderWithoutChildren viewHolder = (ViewHolderWithoutChildren)holder;
                ReportDetails details = reportDetailsList.get(position);
                viewHolder.setIsRecyclable(false);

                viewHolder.pc_no.setText("PC " + details.getPc_no());
                break;
            }
            case 1:{
                final ViewHolderWithChildren viewHolder = (ViewHolderWithChildren)holder;
                ReportDetails details = reportDetailsList.get(position);
                viewHolder.setIsRecyclable(false);

                viewHolder.pcno.setText("PC " + details.getPc_no());
                viewHolder.expandableLayout.setInRecyclerView(true);
                viewHolder.expandableLayout.setExpanded(expandState.get(position));
                viewHolder.expandableLayout.setListener(new ExpandableLayoutListenerAdapter() {
                    @Override
                    public void onPreOpen() {
                        changeRotate(viewHolder.button, 0f, 180f).start();
                        expandState.put(position, true);
                    }

                    @Override
                    public void onPreClose() {
                        changeRotate(viewHolder.button, 180f, 0f).start();
                        expandState.put(position, false);
                    }
                });
                viewHolder.button.setRotation(expandState.get(position)?180f:0f);
                viewHolder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewHolder.expandableLayout.toggle();
                    }
                });

                viewHolder.pc_kb.setText(reportDetailsList.get(position).getKb());
                viewHolder.pc_mb.setText(reportDetailsList.get(position).getMb());
                viewHolder.pc_monitor.setText(reportDetailsList.get(position).getMonitor());
                viewHolder.pc_model.setText(reportDetailsList.get(position).getModel());
                viewHolder.pc_mouse.setText(reportDetailsList.get(position).getMouse());
                viewHolder.pc_vga.setText(reportDetailsList.get(position).getVga());
                viewHolder.pc_hdd.setText(reportDetailsList.get(position).getHdd());
                viewHolder.pc_ram.setText(reportDetailsList.get(position).getRam());
                viewHolder.comp_status.setText(reportDetailsList.get(position).getStatus());
                viewHolder.pc_processor.setText(reportDetailsList.get(position).getPr());
                break;
            }
            default:
                break;
        }
    }

    private ObjectAnimator changeRotate(RelativeLayout button, float from, float to) {
        ObjectAnimator obj = ObjectAnimator.ofFloat(button, "rotation", from, to);
        obj.setDuration(300);
        obj.setInterpolator(Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR));
        return obj;
    }

    @Override
    public int getItemCount() {
        return reportDetailsList.size();
    }
}

class ViewHolderWithChildren extends RecyclerView.ViewHolder{

    TextView pcno, comp_status;
    TextView pc_model, pc_mb, pc_monitor, pc_processor, pc_ram, pc_hdd, pc_mouse, pc_vga, pc_kb;
    RelativeLayout button;
    ExpandableLinearLayout expandableLayout;

    public ViewHolderWithChildren(View itemView) {
        super(itemView);
        pcno = (TextView) itemView.findViewById(R.id.pc);
        comp_status = (TextView) itemView.findViewById(R.id.status);
        pc_monitor = (TextView) itemView.findViewById(R.id.pc_mon);
        pc_model = (TextView) itemView.findViewById(R.id.pc_model);
        pc_mb = (TextView) itemView.findViewById(R.id.pc_mb);
        pc_processor = (TextView) itemView.findViewById(R.id.pc_pr);
        pc_ram = (TextView) itemView.findViewById(R.id.pc_ram);
        pc_hdd = (TextView) itemView.findViewById(R.id.pc_hdd);
        pc_mouse = (TextView) itemView.findViewById(R.id.pc_mouse);
        pc_vga = (TextView) itemView.findViewById(R.id.pc_vga);
        pc_kb = (TextView) itemView.findViewById(R.id.pc_kb);
        button = (RelativeLayout) itemView.findViewById(R.id.button);
        expandableLayout = (ExpandableLinearLayout) itemView.findViewById(R.id.expandableLayout);

    }
}

class ViewHolderWithoutChildren extends RecyclerView.ViewHolder{

    public TextView pc_no;

    public ViewHolderWithoutChildren(View itemView) {
        super(itemView);
        pc_no = (TextView) itemView.findViewById(R.id.pc);
    }

}
