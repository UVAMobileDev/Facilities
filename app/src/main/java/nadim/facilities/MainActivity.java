    package nadim.facilities;

    import android.content.Intent;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.AdapterView;
    import android.widget.ArrayAdapter;
    import android.widget.ListView;

    import com.android.volley.Request;
    import com.android.volley.RequestQueue;
    import com.android.volley.Response;
    import com.android.volley.VolleyError;
    import com.android.volley.VolleyLog;
    import com.android.volley.toolbox.JsonArrayRequest;
    import com.android.volley.toolbox.StringRequest;
    import com.android.volley.toolbox.Volley;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.concurrent.ExecutionException;

    /**
     * Main Activity Class
     * Show all buildings with Sensor Info
     */
    public class MainActivity extends AppCompatActivity {

        //declare variables to be used later
        private ListView mainListView ;
        private ArrayAdapter<String> listAdapter ;
        ArrayList<String> values = new ArrayList<String>();
        RequestQueue queue;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            //set content view for respective xml
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //create the listview
            mainListView = (ListView) findViewById( android.R.id.list);
            mainListView.setFastScrollEnabled(true);
            queue = Volley.newRequestQueue(this);
            //set the list adaper
            listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, values);
            //create the api call, this will give all the sensors available
            String url = "http://138.197.11.189:3000/api/sensors";

            // Instantiate the RequestQueue.
            JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {

                            try {
                                // Parsing json array response
                                // loop through each json object
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject building = (JSONObject) response
                                            .get(i);
                                    String name = building.getString("Name");
                                    if(!(values.contains(name))){
                                        values.add(name);
                                    }
                                }
                                //sort the response so the names are listed alphabetically
                                Collections.sort(values);

                                // Set the ArrayAdapter as the ListView's adapter.
                                mainListView.setAdapter( listAdapter );

                                //onclick listener to transition to new activity when clicked
                                mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent intent = new Intent(MainActivity.this, BuildingSensorActivity.class);
                                        String data=(String)parent.getItemAtPosition(position);
                                        intent.putExtra("Name", data);
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
            //!Important, must add request to queue or API will not be called
            queue.add(req);
        }

    }
