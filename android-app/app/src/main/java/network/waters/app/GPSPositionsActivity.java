package network.waters.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class GPSPositionsActivity extends AppCompatActivity {

    private TextView tStartTime, tEndTime, tNoResults;
    private Button bSetStartTime, bSetEndTime, bResetStartTime, bResetEndTime;
    private int buttonCalled = -1; //0 = start time, 1 = end time
    private long startTime = -1l, endTime = -1l;
    private TableLayout tableGPS;
    private String deviceName = null;

    private SharedPreferences prefs;
    private RequestQueue queue;
    private String gUsername, gPsw;

    private SupportMapFragment mapFragment;
    private GoogleMap map;

    private SlideDateTimeListener listener = new SlideDateTimeListener() {
        @Override
        public void onDateTimeSet(Date date) {
            if(buttonCalled == 0) {
                // our API rest do not use millisecs!
                startTime = date.getTime() / 1000;

                tStartTime.setText(DateFormat.format("yyyy/MM/dd HH:mm:ss", date).toString());
            } else {
                // our API rest do not use millisecs!
                endTime = date.getTime() / 1000;

                tEndTime.setText(DateFormat.format("yyyy/MM/dd HH:mm:ss", date).toString());
            }

            buttonCalled = -1;
        }

        @Override
        public void onDateTimeCancel() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpspositions);

        Intent intent = getIntent();
        deviceName = intent.getExtras().getString("deviceName");

        setTitle(getString(R.string.gps_activity_title) + " " + deviceName);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        gUsername = prefs.getString("LOGIN_USR", "");
        gPsw = prefs.getString("LOGIN_PSW", "");

        queue = Volley.newRequestQueue(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        tableGPS = (TableLayout)findViewById(R.id.table_gps);

        tStartTime = (TextView)findViewById(R.id.tStartTime);
        tEndTime = (TextView)findViewById(R.id.tEndTime);
        tNoResults = (TextView)findViewById(R.id.tNoResults);

        bSetStartTime = (Button)findViewById(R.id.bStartTime);
        bSetEndTime = (Button)findViewById(R.id.bEndTime);

        bResetStartTime = (Button)findViewById(R.id.bResetStartTime);
        bResetEndTime = (Button)findViewById(R.id.bResetEndtTime);

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        bSetStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonCalled = 0;

                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setInitialDate(new Date())
                        .build()
                        .show();
            }
        });

        bSetEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonCalled = 1;

                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setInitialDate(new Date())
                        .build()
                        .show();
            }
        });

        bResetStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTime = -1l;
                tStartTime.setText(getString(R.string.start_time_unset));
            }
        });

        bResetEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTime = -1l;
                tEndTime.setText(getString(R.string.end_time_unset));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detections, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                performSearch();
                break;
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }
        return true;
    }

    private void performSearch(){
        JSONObject reqGPS = new JSONObject();

        try {
            reqGPS.put("action", "2");
            reqGPS.put("device", deviceName);

            if (startTime != -1l)
                reqGPS.put("startTime", startTime);

            if (endTime != -1l)
                reqGPS.put("endTime", endTime);

            reqGPS.put("username",  gUsername);
            reqGPS.put("psw", gPsw);

            JsonObjectRequest GPSreq = new JsonObjectRequest(Request.Method.POST, Utils.URL, reqGPS, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.get("result").equals("No result")) {
                            Toast.makeText(GPSPositionsActivity.this, "No results!", Toast.LENGTH_LONG).show();

                            tNoResults.setVisibility(View.VISIBLE);
                        } else {
                            tNoResults.setVisibility(View.GONE);

                            final JSONArray GPSdata = response.getJSONArray("result");

                            tableGPS.setVisibility(View.VISIBLE);
                            tableGPS.removeAllViews();

                            TableRow mainRow = new TableRow(GPSPositionsActivity.this);

                            //manually re-add all the titles
                            TextView time = new TextView(GPSPositionsActivity.this);
                            time.setText(getString(R.string.device_date));
                            time.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                            time.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                            time.setGravity(Gravity.CENTER_HORIZONTAL);
                            time.setTypeface(Typeface.DEFAULT_BOLD);
                            time.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                            TextView par = new TextView(GPSPositionsActivity.this);
                            par.setText(getString(R.string.device_latitude));
                            par.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                            par.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                            par.setGravity(Gravity.CENTER_HORIZONTAL);
                            par.setTypeface(Typeface.DEFAULT_BOLD);
                            par.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                            TextView val = new TextView(GPSPositionsActivity.this);
                            val.setText(getString(R.string.device_longitude));
                            val.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                            val.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                            val.setGravity(Gravity.CENTER_HORIZONTAL);
                            val.setTypeface(Typeface.DEFAULT_BOLD);
                            val.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                            mainRow.addView(time);
                            mainRow.addView(par);
                            mainRow.addView(val);

                            tableGPS.addView(mainRow);

                            for (int j = 0; j < GPSdata.length(); j++) {
                                final JSONObject json = GPSdata.getJSONObject(j);

                                if (j == 0) {
                                    if (mapFragment != null) {
                                        mapFragment.getMapAsync(new OnMapReadyCallback() {
                                            @Override
                                            public void onMapReady(GoogleMap map) {
                                                loadMap(map);

                                                LatLng latLng = null;
                                                try {
                                                    latLng = new LatLng(Double.parseDouble(json.getString("Latitude")), Double.parseDouble(json.getString("Longitude")));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                map.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.latest_position)));

                                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 8);
                                                map.animateCamera(cameraUpdate);

                                                for(int k = 1; k < GPSdata.length(); k++){
                                                    try {
                                                        final JSONObject json = GPSdata.getJSONObject(k);

                                                        LatLng GPSpos = new LatLng(Double.parseDouble(json.getString("Latitude")), Double.parseDouble(json.getString("Longitude")));
                                                        map.addMarker(new MarkerOptions().position(GPSpos).title(getString(R.string.position_no) + (k+1)));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        });
                                    } else
                                        Toast.makeText(GPSPositionsActivity.this, getString(R.string.map_activity_error), Toast.LENGTH_SHORT).show();
                                }

                                TableRow newRow = new TableRow(GPSPositionsActivity.this);

                                TextView timestamp = new TextView(GPSPositionsActivity.this);
                                timestamp.setText(DateFormat.format("yyyy/MM/dd HH:mm:ss", new Date(Long.parseLong(json.getString("Timestamp")) * 1000l)).toString());
                                timestamp.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                                timestamp.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                                timestamp.setGravity(Gravity.CENTER_HORIZONTAL);
                                timestamp.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                                TextView lati = new TextView(GPSPositionsActivity.this);
                                lati.setText(json.getString("Latitude"));
                                lati.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                                lati.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                                lati.setGravity(Gravity.CENTER_HORIZONTAL);
                                lati.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                                TextView longi = new TextView(GPSPositionsActivity.this);
                                longi.setText(json.getString("Longitude"));
                                longi.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                                longi.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                                longi.setGravity(Gravity.CENTER_HORIZONTAL);
                                longi.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                                newRow.addView(timestamp);
                                newRow.addView(lati);
                                newRow.addView(longi);

                                tableGPS.addView(newRow);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(GPSPositionsActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                }
            });

            queue.add(GPSreq);
        } catch (JSONException e) {
            e.printStackTrace();

            Toast.makeText(GPSPositionsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map == null)
            Toast.makeText(this, getString(R.string.map_activity_error), Toast.LENGTH_SHORT).show();
    }
}
