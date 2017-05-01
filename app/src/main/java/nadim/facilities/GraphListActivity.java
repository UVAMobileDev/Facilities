package nadim.facilities;

import android.content.Intent;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Activity to show listview of current, weekly, and monthly data
 */
public class GraphListActivity extends AppCompatActivity {

    //declare variables
    private String nameString;
    private String typeString;
    private ListView mainListView ;
    private ArrayAdapter<String> listAdapter ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_list);

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
            typeString = (String) savedInstanceState.getSerializable("Name");
        }

        //initialize listview
        mainListView = (ListView) findViewById( android.R.id.list);
        String [] values = new String[]{"Current " + typeString,
                                        "Weekly interpolated data for " + typeString,
                                        "Monthly interpolated data for " + typeString,
                                        };
        listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, values);
        mainListView.setAdapter( listAdapter );

        //create onclick listener for selected option
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //switch case on position
                switch(position){
                    //if 0 go to current data activity
                    case 0:
                        Intent intent = new Intent(GraphListActivity.this, SensorInfoActivity.class);
                        intent.putExtra("Type", typeString);
                        intent.putExtra("Name", nameString);
                        startActivity(intent);
                        break;
                    //if 1 go to weekly data activity
                    case 1:
                        Intent intent2 = new Intent(GraphListActivity.this, WeeklyGraphActivity.class);
                        intent2.putExtra("Type", typeString);
                        intent2.putExtra("Name", nameString);
                        startActivity(intent2);
                        break;
                    //if 2 go to monthly data activity
                    case 2:
                        Intent intent3 = new Intent(GraphListActivity.this, MonthlyGraphActivity.class);
                        intent3.putExtra("Type", typeString);
                        intent3.putExtra("Name", nameString);
                        startActivity(intent3);
                        break;
                }

            }
        });
    }




}
