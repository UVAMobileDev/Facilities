package nadim.facilities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Activity to show Monthly data for Building Facilities
 */
public class MonthlyGraphActivity extends AppCompatActivity {

    //declare variables
    private String nameString;
    private String typeString;
    private String jsonResponse;
    ArrayList<Date> dateTime = new ArrayList<Date>();
    ArrayList<Double> values = new ArrayList<Double>();
    Map<String, Double> monthVal= new HashMap<String, Double>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weely_graph);

        //save variables for name and type passed from previous activities
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

        //format date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateobj = new Date();
        String date = dateFormat.format(dateobj);
        String holdName = null;
        String holdType = null;
        try {
            //url encode name and type
            holdName = URLEncoder.encode(nameString,"UTF-8").replace("+", "%20");
            holdType = URLEncoder.encode(typeString,"UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //create api call
        String url = "http://138.197.11.189:3000/api/sensors/" + holdName + "/" + holdType + "/monthly?date=" + date;

        RequestQueue queue = Volley.newRequestQueue(this);
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
                                //add value
                                values.add(value);
                                //create new date to format
                                Date date = null;
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                //add date at i to current date
                                try {
                                    date = sdf.parse(time);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                //add the date
                                dateTime.add(date);
                            }

                            //loop through dates, aggregate values for each day
                            for(int i = 0; i< dateTime.size(); i++){
                                    Format formatter = new SimpleDateFormat("dd/MM/yy");
                                    String dayMonthYear = formatter.format(dateTime.get(i));
                                    double hold = monthVal.containsKey(dayMonthYear) ? monthVal.get(dayMonthYear) : 0;
                                    hold += values.get(i);
                                    monthVal.put(dayMonthYear, hold);
                            }

                            //initialize line chart
                            LineChart chart = (LineChart) findViewById(R.id.chart);
                            List<Entry> entries = new ArrayList<>();

                            //initialize xaxis to hold dates
                            XAxis xAxis = chart.getXAxis();
                            xAxis.setDrawGridLines(true);
                            xAxis.setGranularity(1f); // only intervals of 1 day
                            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                            //create treemap
                            Map<String, Double> map = new TreeMap<String, Double>(monthVal);

                            //pass the array to xaxisFormatter
                            String[] xAxisValues = map.keySet().toArray(new String[map.size()]);
                            xAxis.setValueFormatter(new MyXAxisValueFormatter(xAxisValues));

                            //iterate through hashmap, save values
                            Iterator it = map.entrySet().iterator();
                            int i = 0;
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry)it.next();
                                Entry be = new Entry((float) i, (float) (double) pair.getValue());
                                entries.add(be);
                                i++;
                                it.remove(); // avoids a ConcurrentModificationException
                            }

                            //LineChart
                            LineDataSet set = new LineDataSet(entries, nameString +
                                    " " + typeString + " current month ");
                            LineData data = new LineData(set);
                            chart.setData(data);
                            chart.getDescription().setText("");
                            chart.setDrawGridBackground(false);
                            chart.getDescription().setTextSize(16f);
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
        //MAKE SURE TO ADD REQUEST TO QUEUE
        queue.add(req);
    }
}
