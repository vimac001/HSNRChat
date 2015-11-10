package de.hs_niederrhein.chat.hsnrchat;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hs_niederrhein.chat.hsnrchat.db.DatabaseOpenHelper;
import de.hs_niederrhein.chat.hsnrchat.net.Talker;

public class MainActivity extends AppCompatActivity {
    private Thread tTalker;
    private Talker talker;
    
    private DatabaseOpenHelper db = new DatabaseOpenHelper(this);
    private List<String> facData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        populateFACData();
        populateFACListView();
        registerClick();
        db.insertTestData();

        openSocketConnection();


    }

    private void registerClick() {
        ListView facList = (ListView)findViewById(R.id.listView_main);
        facList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startDetailActivity(position);

            }
        });
    }

    private void startDetailActivity(int position) {
        System.out.println("Position:" + position);
        Intent changeToDetailView = new Intent(this, DetailActivity.class);
        changeToDetailView.putExtra("facNummer", ++position);
        startActivity(changeToDetailView);
    }

    private void openSocketConnection() {
        try {
            tTalker = new Thread(new Runnable() {
                @Override
                public void run() {
                    talker = new Talker();
                }
            });

            tTalker.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void populateFACData() {
        SQLiteDatabase read = db.getReadableDatabase();
        Cursor c = read.query("faculties", new String[]{"facName"},null,null,null,null,null);
        while(c.moveToNext()){
            facData.add(c.getString(c.getColumnIndex("facName")));
        }

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
