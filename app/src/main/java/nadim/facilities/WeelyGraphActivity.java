package nadim.facilities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Utils;

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
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import static android.R.attr.entries;

/**
 * Activity to create Weekly Chart data for demand
 */
public class WeelyGraphActivity extends AppCompatActivity {

    //declare variables
    private String nameString;
    private String typeString;
    private String jsonResponse;
    ArrayList<Date> dateTime = new ArrayList<Date>();
    ArrayList<Double> values = new ArrayList<Double>();
    Map<Date, Double> dayVal= new HashMap<Date, Double>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weely_graph);

        //save variables for name and type from previous activities
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

        //set date format
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateobj = new Date();
        String date = dateFormat.format(dateobj);
        String holdName = null;
        String holdType = null;
        try {
            //urlencode name and type
            holdName = URLEncoder.encode(nameString,"UTF-8").replace("+", "%20");
            holdType = URLEncoder.encode(typeString,"UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //create api call
        String url = "http://138.197.11.189:3000/api/sensors/" + holdName + "/" + holdType + "/monthly?date=" + date;

        RequestQueue queue = Volley.newRequestQueue(this);
        //make json array request
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            // Parsing json array response
                            // loop through each json object
                            jsonResponse = "";
                            for (int i = 0; i < response.length(); i++) {
                                //get response
                                JSONObject demand = (JSONObject) response.get(i);
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
                            //create date for 6 days ago
                            Calendar cal = new GregorianCalendar();
                            cal.add(Calendar.DAY_OF_MONTH, -6);
                            Date sevenDaysAgo = cal.getTime();

                            //loop through all dates
                            for(int i = 0; i< dateTime.size(); i++){
                                //make sure date is before 7 days
                                if(dateTime.get(i).after(sevenDaysAgo)){
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
                                    Date dayMonthYear = dateTime.get(i);
                                    //format the date
                                    String hold2 = formatter.format(dayMonthYear);
                                    try {
                                        dayMonthYear = formatter.parse(hold2);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    //aggregate values for days and store them in hashmap
                                    double hold = dayVal.containsKey(dayMonthYear) ? dayVal.get(dayMonthYear) : 0;
                                    hold += values.get(i);
                                    dayVal.put(dayMonthYear, hold);
                                }
                            }

                            //initialize line graph
                           LineChart chart = (LineChart) findViewById(R.id.chart);
                            List<Entry> entries = new ArrayList<>();

                            //initialize xAxis to set dates
                            XAxis xAxis = chart.getXAxis();
                            xAxis.setDrawGridLines(true);
                            xAxis.setGranularity(1f); // only intervals of 1 day
                            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                            //create a treemap with all values from hashmap
                            Map<Date, Double> map = new TreeMap<Date, Double>();
                            map.putAll(dayVal);
                            //create new format for dates
                            ArrayList<Date> dateStrings = new ArrayList<Date>();
                            Format formatter = new SimpleDateFormat("dd/MM/yy");
                            dateStrings.addAll(map.keySet());
                            ArrayList<String> stringDates = new ArrayList<String>();
                            //add the dates to an arraylist as a string
                            for(int i = 0; i < dateStrings.size(); i++) {
                                stringDates.add(formatter.format(dateStrings.get(i)));
                            }
                            //sort the dates
                            Collections.sort(stringDates);

                            //add the dates to an array, then pass the array to xaxisformatter
                            String[] xAxisValues = stringDates.toArray(new String[stringDates.size()]);
                            xAxis.setValueFormatter(new MyXAxisValueFormatter(xAxisValues));

                            //iterate through hashmap
                            Iterator it = map.entrySet().iterator();
                            int i = 0;
                            while (it.hasNext()) {
                                //create new entry for each variable
                                Map.Entry pair = (Map.Entry)it.next();
                                Entry be = new Entry((float) i, (float) (double) pair.getValue());
                                entries.add(be);
                                i++;
                                it.remove(); // avoids a ConcurrentModificationException
                            }

                            //LineChart
                            LineDataSet set = new LineDataSet(entries, nameString +
                                    " " + typeString + " current week ");
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
