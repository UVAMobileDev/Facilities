package nadim.facilities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This Activity will list all data attributes that a specified building has: electric, water, etc.
 */
public class BuildingSensorActivity extends AppCompatActivity {

    //declare variables
    private String newString;
    private static final String API_KEY = BuildConfig.API_KEY;
    private String jsonResponse;
    ArrayList<String> values = new ArrayList<String>();
    private ListView mainListView ;
    private ArrayAdapter<String> listAdapter ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_sensor);

        //get building name from last activity
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                newString = null;
            } else {
                newString = extras.getString("Name");
            }
        }
        else {
            newString = (String) savedInstanceState.getSerializable("Name");
        }
        //create new listview
        mainListView = (ListView) findViewById( android.R.id.list);
        mainListView.setFastScrollEnabled(true);
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, values);
        String hold = null;
        try {
            hold = URLEncoder.encode(newString,"UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //api url
        String url = "http://138.197.11.189:3000/api/"+ API_KEY +"/sensors/" + hold;
        RequestQueue queue = Volley.newRequestQueue(this);
        //create json array request
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Parsing json array response
                            // loop through each json object
                            jsonResponse = "";
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject building = (JSONObject) response
                                        .get(i);
                                String type = building.getString("Type");
                                if(!(values.contains(type))) {
                                    values.add(type);
                                }
                            }
                            //get all demand types and sort
                            Collections.sort(values);

                            //set the list view to adapter
                            mainListView.setAdapter( listAdapter );

                            //create onclick listener fro chart data
                            mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent intent = new Intent(BuildingSensorActivity.this, GraphListActivity.class);
                                    String data=(String)parent.getItemAtPosition(position);
                                    intent.putExtra("Type", data);
                                    intent.putExtra("Name", newString);
                                    startActivity(intent);
                                }
                            });

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
