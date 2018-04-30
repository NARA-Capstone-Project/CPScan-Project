package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class SignatureActivity extends AppCompatActivity {

    String from;
    Button mClear, mGetSign, mCancel;
    LinearLayout mContent;
    View view;
    signature mSignature;
    Bitmap bitmap;
    int rep_id;
    VolleyRequestSingleton volley;
    AlertDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        from = getIntent().getStringExtra("from"); //request(startactivityforresult), report, profile
        rep_id = getIntent().getIntExtra("rep_id", 0);

        volley = new VolleyRequestSingleton(this);

        mContent = (LinearLayout) findViewById(R.id.canvasLayout);
        mSignature = new signature(getApplicationContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        // Dynamically generating Layout through java code
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mClear = (Button) findViewById(R.id.clear);
        mGetSign = (Button) findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        mCancel = (Button) findViewById(R.id.cancel);
        view = mContent;
        mGetSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageToString = "";
                Log.v("log_tag", "Panel Saved");
                view.setDrawingCacheEnabled(true);
                imageToString = mSignature.save(view);
                saveSignature(imageToString);
            }
        });
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("log_tag", "Panel Cleared");
                mSignature.clear();
                mGetSign.setEnabled(false);
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (from.equalsIgnoreCase("report")) {
                    Intent intent = new Intent(SignatureActivity.this, ViewInventoryReport.class);
                    intent.putExtra("rep_id", rep_id);
                    startActivity(intent);
                    finish();
                } else if (from.equalsIgnoreCase("profile")) {
                    Intent intent = new Intent(SignatureActivity.this, EditProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else { // request peripherals
                    Intent intent = new Intent();
                    intent.putExtra("result", 0);
                    SignatureActivity.this.setResult(RESULT_CANCELED, intent);
                    SignatureActivity.this.finish();
                }
            }
        });
        progress = new SpotsDialog(this, "Saving...");
        progress.setCancelable(false);
    }

    private void saveSignature(final String image) {
        String user_id = SharedPrefManager.getInstance(this).getUserId();
        String query = "UPDATE accounts SET signature = ? WHERE user_id = ? ;";

        Map<String, String> param = new HashMap<>();
        param.put("query", query);
        param.put("image", image);
        param.put("user_id", user_id);

        volley.sendStringRequestPost(AppConfig.SAVE_SIGNATURE
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        Log.e("RESPONSE", response);
                        try {
                            progress.dismiss();
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                Log.e("IMAGE", obj.getString("image"));
                                if (obj.getString("image").equalsIgnoreCase("inserted")) {
                                    if (from.equalsIgnoreCase("request")) {
                                        Intent intent = new Intent();
                                        intent.putExtra("result", 1);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    } else if(from.equalsIgnoreCase("profile")){
                                        Intent intent = new Intent(SignatureActivity.this, EditProfileActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else { //report
                                        updateSignedStatus();
                                    }
                                } else {
                                    progress.dismiss();
                                    Toast.makeText(SignatureActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                progress.dismiss();
                                Toast.makeText(SignatureActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            progress.dismiss();
                            Toast.makeText(SignatureActivity.this, "An error occurred while saving", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SignatureActivity.this, "Can't connect to the server, pleaase try again later", Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                        error.printStackTrace();
                    }
                }, param);
    }

    private void updateSignedStatus() {
        String user_role = SharedPrefManager.getInstance(this).getUserRole();
        String query;

        if (user_role.equalsIgnoreCase("main technician"))
            query = "UPDATE assessment_reports SET htech_signed = 1 where rep_id = '" + rep_id + "'";
        else if (user_role.equalsIgnoreCase("custodian"))
            query = "UPDATE assessment_reports SET cust_signed = 1 where rep_id = '" + rep_id + "'";
        else
            query = "UPDATE assessment_reports SET admin_signed = 1 where rep_id = '" + rep_id + "'";

        Map<String, String> param = new HashMap<>();
        param.put("query", query);

        volley.sendStringRequestPost(AppConfig.UPDATE_QUERY
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        Log.e("RESPONSE", response);
                        try {
                            JSONObject obj = new JSONObject(response);

                            if (!obj.getBoolean("error")) {
                                Toast.makeText(SignatureActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                if (from.equalsIgnoreCase("report")) {
                                    Intent intent = new Intent(SignatureActivity.this, ViewInventoryReport.class);
                                    intent.putExtra("rep_id", rep_id);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(SignatureActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        }
                        progress.dismiss();
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SignatureActivity.this, "Can't connect to the server, pleaase try again later", Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                        error.printStackTrace();
                    }
                }, param);
    }

    private class signature extends View {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public String save(View v) {
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            //convert output to image jpg and to string
            v.draw(canvas);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            byte[] imgBytes = outputStream.toByteArray();

//                FileOutputStream mFileOutStream = new FileOutputStream(StoredPath);
//                v.draw(canvas);
//                // Convert the output file to Image such as .png
//                bitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
//                mFileOutStream.flush();
//                mFileOutStream.close();

            return Base64.encodeToString(imgBytes, Base64.DEFAULT);
//            passImageString(Base64.encodeToString(imgBytes, Base64.DEFAULT));
        }

        public void clear() {
            path.reset();
            invalidate();
            mGetSign.setEnabled(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
            Log.v("log_tag", string);
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (from.equalsIgnoreCase("report")) {
            Intent intent = new Intent(SignatureActivity.this, ViewInventoryReport.class);
            intent.putExtra("rep_id", rep_id);
            startActivity(intent);
            finish();
        } else if (from.equalsIgnoreCase("request")) {
            Intent intent = new Intent();
            intent.putExtra("result", 0);
            SignatureActivity.this.setResult(RESULT_CANCELED, intent);
            SignatureActivity.this.finish();
        } else {
            //profile
            Intent intent = new Intent(SignatureActivity.this, EditProfileActivity.class);
            startActivity(intent);
            finish();

        }
    }
}
