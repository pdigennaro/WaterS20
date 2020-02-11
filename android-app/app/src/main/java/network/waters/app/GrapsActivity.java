package network.waters.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GrapsActivity extends AppCompatActivity {

    private String deviceName = null;

    private SharedPreferences prefs;
    private RequestQueue queue;
    private String gUsername, gPsw;

    private GraphView graph;

    private int mode = 0; // 0 = one week, 1 = 3 weeks, 4 = one month, 5 = 3 months
    private long startTime = System.currentTimeMillis() / 1000 - 7257600; //three months default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graps);

        Intent intent = getIntent();
        deviceName = intent.getExtras().getString("deviceName");

        setTitle(getString(R.string.graphics_activity_title) + " " + deviceName);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        gUsername = prefs.getString("LOGIN_USR", "");
        gPsw = prefs.getString("LOGIN_PSW", "");

        queue = Volley.newRequestQueue(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        graph = (GraphView) findViewById(R.id.graph);

        showGraph();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == android.R.id.home)
            onBackPressed();
        else if (id == R.id.action_oneweek) {
            startTime = System.currentTimeMillis() / 1000 - 604800;
            showGraph();
            return true;
        } else if(id == R.id.action_3weeks){
            startTime = System.currentTimeMillis() / 1000 - 1814400;
            showGraph();
            return true;
        } else if(id == R.id.action_onemonth){
            startTime = System.currentTimeMillis() / 1000 - 2419200;
            showGraph();
            return true;
        } else if(id == R.id.action_3months){
            startTime = System.currentTimeMillis() / 1000 - 7257600;
            showGraph();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showGraph(){
        JSONObject reqPars = new JSONObject();

        try {
            reqPars.put("action", "1");
            reqPars.put("device", deviceName);

            if(startTime != -1l)
                reqPars.put("startTime", startTime);

            reqPars.put("username",  gUsername);
            reqPars.put("psw", gPsw);

            Log.i(Utils.TAG, reqPars.toString());

            JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.POST, Utils.URL, reqPars, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i(Utils.TAG, response.toString());

                    //reset the graph
                    graph.removeAllSeries();

                    LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>();
                    LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>();
                    LineGraphSeries<DataPoint> series3 = new LineGraphSeries<>();

                    series1.setDrawDataPoints(true);
                    series1.setThickness(10);
                    series1.setDataPointsRadius(12f);

                    series2.setDrawDataPoints(true);
                    series2.setThickness(10);
                    series2.setDataPointsRadius(12f);

                    series3.setDrawDataPoints(true);
                    series3.setThickness(10);
                    series3.setDataPointsRadius(12f);

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

                    JSONArray PARSdata = null;
                    try {
                        if(!response.getString("result").equals("No result")) {
                            PARSdata = response.getJSONArray("result");

                            //here we cross the list in the opposite way because of DESC
                            for (int j = PARSdata.length() - 1; j >= 0; j--) {
                                final JSONObject json = PARSdata.getJSONObject(j);

                                //graph series data
                                if (json.getString("ParName").equals("temp"))
                                    series1.appendData(new DataPoint(s1++, Float.parseFloat(json.getString("ParValue"))), false, 50);
                                if (json.getString("ParName").equals("ph"))
                                    series2.appendData(new DataPoint(s2++, Float.parseFloat(json.getString("ParValue"))), false, 50);
                                if (json.getString("ParName").equals("torb"))
                                    series3.appendData(new DataPoint(s3++, Float.parseFloat(json.getString("ParValue"))), false, 50);
                            }

                            graph.addSeries(series1);
                            graph.addSeries(series2);
                            graph.addSeries(series3);

                            graph.getViewport().setMaxY(30);

                            graph.getViewport().setXAxisBoundsManual(true);
                            graph.getViewport().setMinX(0);

                            int max = 0;

                            if (s1 > max)
                                max = s1;
                            if (s2 > max)
                                max = s2;
                            if (s3 > max)
                                max = s3;

                            graph.getViewport().setMaxX(max - 1);

                            graph.getViewport().setScrollable(true); // enables horizontal scrolling
                            graph.getViewport().setScrollableY(true); // enables vertical scrolling
                            graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
                            graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling
                        } else
                            Toast.makeText(GrapsActivity.this, getString(R.string.no_results), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(Utils.TAG, error.toString());
                    Toast.makeText(GrapsActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                }
            });

            queue.add(jsonReq);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(GrapsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
