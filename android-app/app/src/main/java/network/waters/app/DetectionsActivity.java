package network.waters.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class DetectionsActivity extends AppCompatActivity {

    private TextView tStartTime, tEndTime, tParameters, tNoResults;
    private Button bSetStartTime, bSetEndTime, bSetParameters, bResetStartTime, bResetEndTime, bResetParameters;
    private int buttonCalled = -1; //0 = start time, 1 = end time
    private long startTime = -1l, endTime = -1l;
    private ArrayList seletedItems = new ArrayList();
    private TableLayout tableData;
    private String deviceName = null;

    private SharedPreferences prefs;
    private RequestQueue queue;
    private String gUsername, gPsw;

    private CharSequence[] opz;

    private GraphView graph;

    private SlideDateTimeListener listener = new SlideDateTimeListener() {
        @Override
        public void onDateTimeSet(Date date) {
            Log.i(Utils.TAG, "Set: " + date.getTime());

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
        setContentView(R.layout.activity_detections);

        Intent intent = getIntent();
        deviceName = intent.getExtras().getString("deviceName");

        setTitle(getString(R.string.detections_title) + " " +  deviceName);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        gUsername = prefs.getString("LOGIN_USR", "");
        gPsw = prefs.getString("LOGIN_PSW", "");

        queue = Volley.newRequestQueue(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        tableData = (TableLayout)findViewById(R.id.table_pars);

        tStartTime = (TextView)findViewById(R.id.tStartTime);
        tEndTime = (TextView)findViewById(R.id.tEndTime);
        tParameters = (TextView)findViewById(R.id.tParameters);
        tNoResults = (TextView)findViewById(R.id.tNoResults);

        bSetStartTime = (Button)findViewById(R.id.bStartTime);
        bSetEndTime = (Button)findViewById(R.id.bEndTime);
        bSetParameters = (Button)findViewById(R.id.bParameters);

        bResetStartTime = (Button)findViewById(R.id.bResetStartTime);
        bResetEndTime = (Button)findViewById(R.id.bResetEndtTime);
        bResetParameters = (Button)findViewById(R.id.bResetPars);

        graph = (GraphView) findViewById(R.id.graph);

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

        bSetParameters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showParametersDialog();
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

        bResetParameters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seletedItems = new ArrayList();
                tParameters.setText(getString(R.string.pars_unset));
            }
        });

        opz = new CharSequence[]{getString(R.string.par1), getString(R.string.par2), getString(R.string.par3)};
    }

    private void showParametersDialog(){
        AlertDialog dialog;

        //reset the pars list
        seletedItems = new ArrayList();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selez. parametri di ricerca ");
        builder.setMultiChoiceItems(opz, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected,
                                        boolean isChecked) {
                        if (isChecked) {
                            seletedItems.add(indexSelected);
                        } else if (seletedItems.contains(indexSelected)) {
                            seletedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String selPars = "";
                        for(int k = 0; k < seletedItems.size(); k++) {
                            switch ((int) seletedItems.get(k)){
                                case 0:
                                    selPars += " temp";
                                    break;
                                case 1:
                                    selPars += " torb";
                                    break;
                                case 2:
                                    selPars += " ph";
                                    break;
                            }
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing...
                    }
                });

        dialog = builder.create();
        dialog.show();
    }

    private void performSearch(){
        JSONObject reqPars = new JSONObject();

        try {
            reqPars.put("action", "1");
            reqPars.put("device", deviceName);

            if(startTime != -1l)
                reqPars.put("startTime", startTime);

            if(endTime != -1l)
                reqPars.put("endTime", endTime);

            if(!seletedItems.isEmpty()){
                JSONArray arrayOfPars = new JSONArray();
                String[] pars = new String[seletedItems.size()];

                for(int k = 0; k < seletedItems.size(); k++) {
                    switch ((int) seletedItems.get(k)){
                        case 0:
                            arrayOfPars.put("temp");
                            break;
                        case 1:
                            arrayOfPars.put("torb");
                            break;
                        case 2:
                            arrayOfPars.put("ph");
                            break;
                    }
                }

                reqPars.put("RIL", arrayOfPars);
            }

            reqPars.put("username",  gUsername);
            reqPars.put("psw", gPsw);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, Utils.URL, reqPars, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(Utils.TAG, response.toString());

                try {
                    if(response.get("result").equals("No result")){
                        Toast.makeText(DetectionsActivity.this, getString(R.string.no_results), Toast.LENGTH_LONG).show();
                        tNoResults.setVisibility(View.VISIBLE);
                    } else {
                        tNoResults.setVisibility(View.GONE);

                        //remove all the rows except for the 1st one...
                        tableData.removeAllViews();

                        //reset the graph too
                        graph.removeAllSeries();

                        TableRow mainRow = new TableRow(DetectionsActivity.this);

                        //manually re-add all the titles
                        TextView time = new TextView(DetectionsActivity.this);
                        time.setText(getString(R.string.device_date));
                        time.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                        time.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                        time.setGravity(Gravity.CENTER_HORIZONTAL);
                        time.setTypeface(Typeface.DEFAULT_BOLD);
                        time.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                        TextView par = new TextView(DetectionsActivity.this);
                        par.setText(getString(R.string.device_ParName));
                        par.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                        par.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                        par.setGravity(Gravity.CENTER_HORIZONTAL);
                        par.setTypeface(Typeface.DEFAULT_BOLD);
                        par.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                        TextView val = new TextView(DetectionsActivity.this);
                        val.setText(getString(R.string.device_ParValue));
                        val.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                        val.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                        val.setGravity(Gravity.CENTER_HORIZONTAL);
                        val.setTypeface(Typeface.DEFAULT_BOLD);
                        val.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                        mainRow.addView(time);
                        mainRow.addView(par);
                        mainRow.addView(val);

                        tableData.addView(mainRow);

                        LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>();
                        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>();
                        LineGraphSeries<DataPoint> series3 = new LineGraphSeries<>();

                        series1.setDrawDataPoints(true);
                        series1.setDataPointsRadius(10f);

                        series2.setDrawDataPoints(true);
                        series2.setDataPointsRadius(10f);

                        series3.setDrawDataPoints(true);
                        series3.setDataPointsRadius(10f);

                        series1.setColor(Color.RED);
                        series2.setColor(Color.BLUE);
                        series3.setColor(Color.GREEN);

                        // legend
                        series1.setTitle("temp");
                        series2.setTitle("ph");
                        series3.setTitle("torb");

                        graph.getLegendRenderer().setVisible(true);
                        graph.getLegendRenderer().setBackgroundColor(Color.LTGRAY);
                        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

                        int s1 = 0, s2 = 0, s3 = 0;

                        JSONArray PARSdata = response.getJSONArray("result");

                        //here we cross the list in the opposite way because of DESC
                        for (int j = PARSdata.length() - 1; j >= 0; j--) {
                            final JSONObject json = PARSdata.getJSONObject(j);

                            TableRow newRow = new TableRow(DetectionsActivity.this);

                            TextView timestamp = new TextView(DetectionsActivity.this);
                            timestamp.setText(DateFormat.format("yyyy/MM/dd HH:mm:ss", new Date(Long.parseLong(json.getString("Timestamp")) * 1000l)).toString());
                            timestamp.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                            timestamp.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                            timestamp.setGravity(Gravity.CENTER_HORIZONTAL);
                            timestamp.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                            TextView parro = new TextView(DetectionsActivity.this);
                            parro.setText(json.getString("ParName"));
                            parro.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                            parro.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                            parro.setGravity(Gravity.CENTER_HORIZONTAL);
                            parro.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                            TextView vallo = new TextView(DetectionsActivity.this);
                            vallo.setText(json.getString("ParValue"));
                            vallo.setBackground(getResources().getDrawable(R.drawable.cell_shape));
                            vallo.setPadding(getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding), getResources().getInteger(R.integer.table_text_padding));
                            vallo.setGravity(Gravity.CENTER_HORIZONTAL);
                            vallo.setTextSize(getResources().getDimension(R.dimen.table_textsize));

                            newRow.addView(timestamp);
                            newRow.addView(parro);
                            newRow.addView(vallo);

                            tableData.addView(newRow);

                            //graph series data
                            if(json.getString("ParName").equals("temp"))
                                series1.appendData(new DataPoint(s1++, Float.parseFloat(json.getString("ParValue"))), false, 50);
                            if(json.getString("ParName").equals("ph"))
                                series2.appendData(new DataPoint(s2++, Float.parseFloat(json.getString("ParValue"))), false, 50);
                            if(json.getString("ParName").equals("torb"))
                                series3.appendData(new DataPoint(s3++, Float.parseFloat(json.getString("ParValue"))), false, 50);
                        }

                        tableData.setVisibility(View.VISIBLE);

                        graph.addSeries(series1);
                        graph.addSeries(series2);
                        graph.addSeries(series3);

                        graph.getViewport().setMaxY(30);

                        graph.getViewport().setXAxisBoundsManual(true);
                        graph.getViewport().setMinX(0);

                        int max = 0;

                        if(s1 > max)
                            max = s1;
                        if(s2 > max)
                            max = s2;
                        if(s3  > max)
                            max = s3;

                        graph.getViewport().setMaxX(max-1);
                        graph.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(DetectionsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(Utils.TAG, error.toString());

                Toast.makeText(DetectionsActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(jsonReq);
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
}
