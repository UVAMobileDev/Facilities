package nadim.facilities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Activity to show current data for building demand
 */
public class SensorInfoActivity extends AppCompatActivity {

    //declare variables
    private static final String API_KEY = BuildConfig.API_KEY;
    private String nameString;
    private String typeString;
    private String jsonResponse;
    ArrayList<String> dates = new ArrayList<String>();
    ArrayList<Double> values = new ArrayList<Double>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_info);

        //get name and type from previous activities
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                nameString = null;
                typeString = null;
            } else {
                nameString = extras.getString("Name");
                typeString = extras.getString("Type");
            }
        }
        else {
            nameString = (String) savedInstanceState.getSerializable("Name");
            typeString = (String) savedInstanceState.getSerializable("Type");
        }

        //format date for returned datetime
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateobj = new Date();
        String date = dateFormat.format(dateobj);
        String holdName = null;
        String holdType = null;
        try {
            holdName = URLEncoder.encode(nameString,"UTF-8").replace("+", "%20");
            holdType = URLEncoder.encode(typeString,"UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //create api call
        String url = "http://138.197.11.189:3000/api/"+ API_KEY +"/sensors/" + holdName + "/" + holdType + "/daily?date=" + date;

        RequestQueue queue = Volley.newRequestQueue(this);
        //get array request info
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            // Parsing json array response
                            // loop through each json object
                            jsonResponse = "";
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject demand = (JSONObject) response
                                        .get(i);
                                String time = demand.getString("Time");
                                double value = demand.getDouble("Value");

                                dates.add(time);
                                values.add(value);
                            }
                            //create new arraylist for times
                            ArrayList<Integer> times = new ArrayList<Integer>();
                            for(int i = 0; i < dates.size(); i++){
                                Date date = null;
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                try {
                                    date = sdf.parse(dates.get(i));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                //turn the dates into hour format for comparison
                                SimpleDateFormat df = new SimpleDateFormat("HH");
                                String shortTimeStr = df.format(date);
                                int hour = Integer.parseInt(shortTimeStr);
                                times.add(hour);
                            }
                            //create bar chart
                            BarChart chart = (BarChart) findViewById(R.id.chart);
                            List<BarEntry> entries = new ArrayList<>();
                            ArrayList<Integer> holdTimes = new ArrayList<Integer>();
                            //do not start bar chart until 0 value confirmed, to avoid values from day before
                            for(int i =0; i < times.size(); i++){
                                if(holdTimes.contains(0) || times.get(i) == 0) {
                                    BarEntry be = new BarEntry((float) times.get(i), (float) (double) values.get(i));
                                    entries.add(be);
                                    holdTimes.add(times.get(i));
                                }
                            }
                            //create date for bar chart description
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            Date dateobj = new Date();
                            String date = dateFormat.format(dateobj);
                            BarDataSet set = new BarDataSet(entries, nameString + " " + typeString + " on " + date);

                            //set the data accordingly
                            BarData data = new BarData(set);
                            data.setBarWidth(0.9f); // set custom bar width
                            chart.setData(data);

                            //leave description empty
                            chart.getDescription().setText("");
                            chart.setDrawGridBackground(false);
                            chart.getDescription().setTextSize(16f);
                            chart.setFitBars(true); // make the x-axis fit exactly all bars
                            chart.invalidate(); // refresh


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        //MAKE SURE TO ADD REQUEST
        queue.add(req);
    }

}
