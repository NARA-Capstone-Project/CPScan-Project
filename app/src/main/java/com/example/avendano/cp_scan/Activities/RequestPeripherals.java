package com.example.avendano.cp_scan.Activities;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avendano.cp_scan.R;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPickerListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestPeripherals extends AppCompatActivity {
    Toolbar toolbar;
    ListView listView;
    FloatingActionButton add;
    String[] peripheralsList = new String[]{"Mouse", "Keyboard", "Power Supply", "Power Cord", "Memory", "Video Card"
            , "Motherboard", "VGA Cable", "UTP Cable", "Router Hub"};
    ArrayList<String> choices = new ArrayList<>();
    ArrayList<Integer> quantity = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_peripherals);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Request Peripherals");

        listView = (ListView) findViewById(R.id.peripherals);

        add = (FloatingActionButton) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(peripheralsList.length > 0)
                    showPeripherals();
                else
                    Toast.makeText(RequestPeripherals.this, "You have already selected all peripherals", Toast.LENGTH_SHORT).show();
            }
        });
        showPeripherals();
    }

    private void showPeripherals() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Peripherals...")
                .setMultiChoiceItems(peripheralsList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            choices.add(peripheralsList[which]);
                            quantity.add(1);
                        } else if (choices.contains(peripheralsList[which])) {
                            int idx = choices.indexOf(peripheralsList[which]);
                            choices.remove(peripheralsList[which]);
                            quantity.remove(idx);
                        }
                    }
                })
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (choices.isEmpty())
                            RequestPeripherals.this.finish();
                    }
                })
                .setCancelable(false);
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btn = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (choices.isEmpty()) {
                            Toast.makeText(RequestPeripherals.this, "No selected peripherals", Toast.LENGTH_SHORT).show();
                        } else {
                            for(int i = 0; i < choices.size(); i++){
                                String toRemove = choices.get(i);
                                List<String> list = new ArrayList<String>(Arrays.asList(peripheralsList));
                                list.remove(toRemove);
                                peripheralsList = list.toArray(new String[list.size()]);
                                dialog.dismiss();
                            }
                            PeripheralsAdapter adapter = new PeripheralsAdapter();
                            listView.setAdapter(adapter);
                        }
                    }
                });
            }
        });
        alert.show();
    }

    private class PeripheralsAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return choices.size();
        }

        @Override
        public Object getItem(int position) {
            return choices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final int pos = position;
            convertView = getLayoutInflater().inflate(R.layout.request_peripherals_items, null);
            final TextView peripheral = (TextView) convertView.findViewById(R.id.peripheral);
            peripheral.setText(choices.get(position));
            ImageView delete = (ImageView) convertView.findViewById(R.id.remove);
            ScrollableNumberPicker qty = (ScrollableNumberPicker) convertView.findViewById(R.id.qty);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String toRemove = peripheral.getText().toString();
                    List<String> list = new ArrayList<String>(Arrays.asList(peripheralsList));
                    list.add(toRemove);
                    peripheralsList = list.toArray(new String[list.size()]);
                    int  idx = choices.indexOf(toRemove);
                    quantity.remove(idx);
                    choices.remove(toRemove);
                    PeripheralsAdapter.this.notifyDataSetChanged();
                }
            });

            qty.setListener(new ScrollableNumberPickerListener() {
                @Override
                public void onNumberPicked(int value) {
                    quantity.set(pos, value);
                }
            });
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.assess_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:

                break;
            case R.id.cancel:
                this.finish();
                break;
        }

        return true;
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
