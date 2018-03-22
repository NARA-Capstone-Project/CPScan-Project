package com.example.avendano.cp_scan.Activities;

import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avendano.cp_scan.Adapter.ReportDetailsAdapter;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Model.ReportDetails;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class ViewInventoryReport extends AppCompatActivity {

    RecyclerView recyclerView;
    SQLiteHandler db;
    RecyclerView.LayoutManager layoutManager;
    List<ReportDetails> reportDetailsList;
    Button sign;
    TextView tech, cust,dean, technician, custodian;
    int rep_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_inventory_report);
        db = new SQLiteHandler(ViewInventoryReport.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reportDetailsList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.inventory_details);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        sign = (Button) findViewById(R.id.sign);
        rep_id = getIntent().getIntExtra("rep_id", 0);
        tech = (TextView) findViewById(R.id.tech);
        cust = (TextView) findViewById(R.id.cust);
        dean = (TextView) findViewById(R.id.dean);
        technician = (TextView) findViewById(R.id.reporter);
        custodian = (TextView) findViewById(R.id.custodian);

        if(SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("custodian")){
            sign.setVisibility(View.VISIBLE);
        }

        setData();
    }

    private void setData() {
        reportDetailsList.clear();
        Cursor c = db.getReportDetailsById(rep_id);
        Cursor signs = db.getReportByRepId(rep_id);
        if (c.moveToFirst()) {
            do {
                //COMP_ID,COMP_NAME, COMP_MODEL, COMP_MB, COMP_PR, COMP_MONITOR,
//                COMP_RAM, COMP_KBOARD, COMP_MOUSE, COMP_VGA, COMP_HDD, COMP_STATUS, REPORT_MB_SERIAL,REPORT_MON_SERIAL
                int pc_no = c.getInt(c.getColumnIndex(db.COMP_NAME));
                String status =c.getString(c.getColumnIndex(db.COMP_STATUS));
                String monitor =c.getString(c.getColumnIndex(db.COMP_MONITOR));
                String mb =c.getString(c.getColumnIndex(db.COMP_MB));
                String model =c.getString(c.getColumnIndex(db.COMP_MODEL));
                String pr =c.getString(c.getColumnIndex(db.COMP_PR));
                String ram =c.getString(c.getColumnIndex(db.COMP_RAM));
                String hdd =c.getString(c.getColumnIndex(db.COMP_HDD));
                String vga =c.getString(c.getColumnIndex(db.COMP_VGA));
                String mouse =c.getString(c.getColumnIndex(db.COMP_MOUSE));
                String kb =c.getString(c.getColumnIndex(db.COMP_KBOARD));

                ReportDetails reports = new ReportDetails(pc_no, monitor,mb, pr,ram,hdd, vga
                ,mouse, kb, status, model,true);
                reportDetailsList.add(reports);
            } while (c.moveToNext());
            ReportDetailsAdapter adapter = new ReportDetailsAdapter(reportDetailsList);
            recyclerView.setAdapter(adapter);
        }
        if(signs.moveToFirst()){
            int cust_sign = signs.getInt(signs.getColumnIndex(db.REPORT_CUST_SIGNED));
            int tech_sign = signs.getInt(signs.getColumnIndex(db.REPORT_HTECH_SIGNED));
            int dean_sign = signs.getInt(signs.getColumnIndex(db.REPORT_ADMIN_SIGNED));
            String cust_id = signs.getString(signs.getColumnIndex(db.COLUMN_CUST_ID));
            String tech_id = signs.getString(signs.getColumnIndex(db.COLUMN_TECH_ID));

            Cursor custName = db.getCustName(cust_id);
            Cursor techName = db.getTechName(tech_id);
            if(custName.moveToFirst()){
                custodian.setText(custName.getString(custName.getColumnIndex(db.ROOMS_CUSTODIAN)));
            }
            if(techName.moveToFirst()){
                technician.setText(techName.getString(techName.getColumnIndex(db.ROOMS_TECHNICIAN)));
            }
            if(cust_sign != 0)
                cust.setText("Yes");
            else
                cust.setText("No");
            if(tech_sign != 0)
                tech.setText("Yes");
            else
                tech.setText("No");
            if(dean_sign != 0)
                dean.setText("Yes");
            else
                dean.setText("No");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                ViewInventoryReport.this.finish();
                return true;
            }
            case R.id.remarks:{
                showRemark();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showRemark() {
        String msg = "";
        Cursor c = db.getReportByRepId(rep_id);
        if(c.moveToFirst()){
            msg = c.getString(c.getColumnIndex(db.REPORT_REMARKS));
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewInventoryReport.this);
            builder.setMessage("" + msg);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            if(msg.trim().length() == 0)
                Toast.makeText(this, "No remarks", Toast.LENGTH_SHORT).show();
            else
                alert.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.remark_view_inventory, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }
}
