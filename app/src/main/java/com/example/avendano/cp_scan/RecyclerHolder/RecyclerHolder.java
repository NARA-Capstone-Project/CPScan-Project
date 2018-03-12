package com.example.avendano.cp_scan.RecyclerHolder;

import android.content.ClipData;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.avendano.cp_scan.R;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class RecyclerHolder extends RecyclerView.ViewHolder {
    public TextView headTxt;
    public TextView midTxt;
    public TextView subTxt;
    public ImageView img;
    public CardView card;
    public RecyclerHolder(View itemView) {
        super(itemView);
         headTxt = (TextView) itemView.findViewById(R.id.head_txt);
         midTxt = (TextView) itemView.findViewById(R.id.midtxt);
         subTxt = (TextView) itemView.findViewById(R.id.subtxt);
         img = (ImageView) itemView.findViewById(R.id.recycler_icon);
         card = (CardView) itemView.findViewById(R.id.cardview);
    }
}
