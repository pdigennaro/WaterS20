package network.waters.app;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
	
	private static final int OPENLOGIN = 1111;
    private SharedPreferences prefs;

    private RequestQueue queue;
    private String gUsername, gPsw;

    private RelativeLayout rDeviceInfos;
    private LinearLayout lMainLayout;
    private FloatingActionButton fab;

    private SupportMapFragment mapFragment;
    private GoogleMap map;

    private TableLayout tableGPS, tableData;

    private String deviceName = null;

    private TextView tDevName, tLastActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        queue = Volley.newRequestQueue(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        gUsername = prefs.getString("LOGIN_USR", "");
        gPsw = prefs.getString("LOGIN_PSW", "");

        rDeviceInfos = (RelativeLayout) findViewById(R.id.rDeviceInfos);
        lMainLayout = (LinearLayout)findViewById(R.id.lMainLayout);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDevicesList();
            }
        });

        tDevName = (TextView)findViewById(R.id.tDevName);
        tLastActivity = (TextView)findViewById(R.id.tLastActivity);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(prefs.getBoolean("LOGIN_REQUIRED", true)){
            Intent openLogin = new Intent(this, LoginActivity.class);
            startActivityForResult(openLogin, OPENLOGIN);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == OPENLOGIN) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.d(Utils.TAG, "Login OK!");

                SharedPreferences.Editor editPref = prefs.edit();
                editPref.putBoolean("LOGIN_REQUIRED", false);
                editPref.commit();

                //update credentials when login done
                gUsername = prefs.getString("LOGIN_USR", "");
                gPsw = prefs.getString("LOGIN_PSW", "");
            } else
                finish();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update) {
            if(deviceName != null)
                showDeviceInfos();
            else
                Toast.makeText(this, getString(R.string.select_device_first), Toast.LENGTH_LONG).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dev_select) {
            showDevicesList();
        } else if (id == R.id.nav_rils) {
            if(deviceName != null)
                showDetections();
            else
                Toast.makeText(this, getString(R.string.select_device_first), Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_gpspositions) {
            if(deviceName != null)
                showGPSPositions();
            else
                Toast.makeText(this, getString(R.string.select_device_first), Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_graphs) {
            if(deviceName != null)
                showGraphs();
            else
                Toast.makeText(this, getString(R.string.select_device_first), Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_abstract) {
            openAbstract();
        } else if (id == R.id.nav_credits) {
            showCredits();
        } else if (id == R.id.nav_exit){
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDetections(){
        Intent openDetections = new Intent(MainActivity.this, DetectionsActivity.class);
        openDetections.putExtra("deviceName", deviceName);
        startActivity(openDetections);
    }

    private void showGPSPositions(){
        Intent openGPSPositions = new Intent(MainActivity.this, GPSPositionsActivity.class);
        openGPSPositions.putExtra("deviceName", deviceName);
        startActivity(openGPSPositions);
    }

    private void showGraphs(){
        Intent openGraphs = new Intent(MainActivity.this, GrapsActivity.class);
        openGraphs.putExtra("deviceName", deviceName);
        startActivity(openGraphs);
    }

    private void showCredits(){
        AlertDialog.Builder bCredits = new AlertDialog.Builder(this);
        bCredits.setTitle(getString(R.string.menu_credits));
        bCredits.setMessage(Html.fromHtml(String.format(getString(R.string.credits_text), getString(R.string.app_ver))));
        bCredits.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = bCredits.show();

        // Make the textview clickable. Must be called after show()
        ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void openAbstract(){
        Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(Utils.ABSTRACT_URL));
        startActivity(intent);
    }

    private void showDevicesList(){
        final JSONObject json = new JSONObject();
        try {
            json.put("action", "3");
            json.put("username",  gUsername);
            json.put("psw", gPsw);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, Utils.URL, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(Utils.TAG, "Response: " + response.toString());

                try {
                    final JSONArray devices = response.getJSONArray("result");
                    Log.d(Utils.TAG, "Devices: " + devices.toString());

                    final AlertDialog.Builder devicesBuilder = new AlertDialog.Builder(MainActivity.this);
                    devicesBuilder.setTitle(getString(R.string.select_device_text));

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_checked);

                    for(int j = 0; j < devices.length(); j++)
                        arrayAdapter.add(devices.getJSONObject(j).getString("ID"));

                    devicesBuilder.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    devicesBuilder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            deviceName = arrayAdapter.getItem(which);
                            tDevName.setText(deviceName);

                            lMainLayout.setVisibility(View.INVISIBLE);
                            fab.setVisibility(View.INVISIBLE);

                            rDeviceInfos.setVisibility(View.VISIBLE);

                            showDeviceInfos();
                        }
                    });
                    devicesBuilder.show();
                } catch (JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(Utils.TAG, "Response: " + error.toString());
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(jsObjRequest);
    }

    private void showDeviceInfos(){
        setTitle(getString(R.string.device_latest_events_title) + " " + deviceName);

        tableGPS = (TableLayout)findViewById(R.id.table_gps);
        tableData = (TableLayout)findViewById(R.id.table_pars);

        JSONObject reqGPS = new JSONObject();
        JSONObject reqPars = new JSONObject();

        try {
            reqGPS.put("action", "2");
            reqGPS.put("device", deviceName);
            reqGPS.put("username",  gUsername);
            reqGPS.put("psw", gPsw);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            reqPars.put("action", "1");
            reqPars.put("device", deviceName);
            reqPars.put("username",  gUsername);
            reqPars.put("psw", gPsw);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonGPS = new JsonObjectRequest(Request.Method.POST, Utils.URL, reqGPS, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(Utils.TAG, "response: " + response.toString());

                try {
                    if(response.get("result").equals("No result")){
                        Toast.makeText(MainActivity.this, "No results!", Toast.LENGTH_LONG).show();
                    } else {
                        JSONArray GPSdata = response.getJSONArray("result");

                        tableGPS.removeAllViews();

                        TableRow mainRow = new TableRow(MainActivity.this);
                        //manually re-add all the titles
                        TextView time = new TextView(MainActivity.this);
                        time.setText(getString(R.string.device_date));
                        time.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                        time.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                        time.setGravity(Gravity.CENTER_HORIZONTAL);
                        time.setTypeface(Typeface.DEFAULT_BOLD);
                        time.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                        TextView par = new TextView(MainActivity.this);
                        par.setText(getString(R.string.device_latitude));
                        par.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                        par.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                        par.setGravity(Gravity.CENTER_HORIZONTAL);
                        par.setTypeface(Typeface.DEFAULT_BOLD);
                        par.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                        TextView val = new TextView(MainActivity.this);
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

                                            map.addMarker(new MarkerOptions().position(latLng).title("Latest position"));

                                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 8);
                                            map.animateCamera(cameraUpdate);
                                        }
                                    });
                                } else
                                    Toast.makeText(MainActivity.this, getString(R.string.map_activity_error), Toast.LENGTH_SHORT).show();
                            }

                            TableRow newRow = new TableRow(MainActivity.this);

                            TextView timestamp = new TextView(MainActivity.this);
                            timestamp.setText(DateFormat.format("yyyy/MM/dd HH:mm:ss", new Date(Long.parseLong(json.getString("Timestamp")) * 1000l)).toString());
                            timestamp.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                            timestamp.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                            timestamp.setGravity(Gravity.CENTER_HORIZONTAL);
                            timestamp.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                            // in this context, first element is also the latest one!
                            if(j == 0)
                                tLastActivity.setText(DateFormat.format("yyyy/MM/dd HH:mm:ss", new Date(Long.parseLong(json.getString("Timestamp")) * 1000l)).toString());

                            TextView lati = new TextView(MainActivity.this);
                            lati.setText(json.getString("Latitude"));
                            lati.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                            lati.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                            lati.setGravity(Gravity.CENTER_HORIZONTAL);
                            lati.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                            TextView longi = new TextView(MainActivity.this);
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
                Log.e(Utils.TAG, error.toString());

                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        JsonObjectRequest jsonPars = new JsonObjectRequest(Request.Method.POST, Utils.URL, reqPars, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(Utils.TAG, "response: " + response.toString());

                try {
                    if(response.get("result").equals("No result")){
                        Toast.makeText(MainActivity.this, "No results!", Toast.LENGTH_LONG).show();
                    } else {
                        JSONArray PARSdata = response.getJSONArray("result");

                        tableData.removeAllViews();

                        TableRow mainRow = new TableRow(MainActivity.this);

                        //manually re-add all the titles
                        TextView time = new TextView(MainActivity.this);
                        time.setText(getString(R.string.device_date));
                        time.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                        time.setPadding(15, 15, 15, 15);
                        time.setGravity(Gravity.CENTER_HORIZONTAL);
                        time.setTypeface(Typeface.DEFAULT_BOLD);
                        time.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                        TextView par = new TextView(MainActivity.this);
                        par.setText(getString(R.string.device_ParName));
                        par.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                        par.setPadding(15, 15, 15, 15);
                        par.setGravity(Gravity.CENTER_HORIZONTAL);
                        par.setTypeface(Typeface.DEFAULT_BOLD);
                        par.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                        TextView val = new TextView(MainActivity.this);
                        val.setText(getString(R.string.device_ParValue));
                        val.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                        val.setPadding(15, 15, 15, 15);
                        val.setGravity(Gravity.CENTER_HORIZONTAL);
                        val.setTypeface(Typeface.DEFAULT_BOLD);
                        val.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                        mainRow.addView(time);
                        mainRow.addView(par);
                        mainRow.addView(val);

                        tableData.addView(mainRow);

                        for (int j = 0; j < PARSdata.length(); j++) {
                            final JSONObject json = PARSdata.getJSONObject(j);

                            TableRow newRow = new TableRow(MainActivity.this);

                            TextView timestamp = new TextView(MainActivity.this);
                            timestamp.setText(DateFormat.format("yyyy/MM/dd HH:mm:ss", new Date(Long.parseLong(json.getString("Timestamp")) * 1000l)).toString());
                            timestamp.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                            timestamp.setPadding(15, 15, 15, 15);
                            timestamp.setGravity(Gravity.CENTER_HORIZONTAL);
                            timestamp.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                            TextView parro = new TextView(MainActivity.this);
                            parro.setText(json.getString("ParName"));
                            parro.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                            parro.setPadding(15, 15, 15, 15);
                            parro.setGravity(Gravity.CENTER_HORIZONTAL);
                            parro.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                            TextView vallo = new TextView(MainActivity.this);
                            vallo.setText(json.getString("ParValue"));
                            vallo.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                            vallo.setPadding(15, 15, 15, 15);
                            vallo.setGravity(Gravity.CENTER_HORIZONTAL);
                            vallo.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                            newRow.addView(timestamp);
                            newRow.addView(parro);
                            newRow.addView(vallo);

                            tableData.addView(newRow);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Utils.TAG, error.toString());

                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(jsonGPS);
        queue.add(jsonPars);

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map == null)
            Toast.makeText(this, getString(R.string.map_activity_error), Toast.LENGTH_SHORT).show();
    }
}
