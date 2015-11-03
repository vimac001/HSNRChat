package de.hs_niederrhein.chat.hsnrchat;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<String> facData = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        populateFACData();
        populateFACListView();
    }

    private void populateFACData() {
        facData.add("Chemie");
        facData.add("Design");
        facData.add("Elektrotechnik/Informatik");
    }

    private void populateFACListView() {
        ArrayAdapter<String> facAdapter = new FACListAdapter();
        ListView listView_main = (ListView)findViewById(R.id.listView_main);
        listView_main.setAdapter(facAdapter);
    }



    private class FACListAdapter extends ArrayAdapter<String>
    {

        public FACListAdapter() {
            super(MainActivity.this, R.layout.item_layout, facData);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null)
                itemView = getLayoutInflater().inflate(R.layout.item_layout, parent, false);

            String currentFAC = facData.get(position);

            TextView textLayout = (TextView)itemView.findViewById(R.id.item_text);
            textLayout.setText(currentFAC);

            return itemView;
        }


    }




}
