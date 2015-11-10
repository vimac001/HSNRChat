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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_niederrhein.chat.hsnrchat.db.DatabaseOpenHelper;

public class DetailActivity extends AppCompatActivity {
    private List<String> facNames = new ArrayList<String>();
    private DatabaseOpenHelper db = new DatabaseOpenHelper(this);
    private int facNummer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent.hasExtra("facNummer")) {
            this.facNummer = intent.getIntExtra("facNummer", 0);
            System.out.println("Extra: " + facNummer);
            populateDetailData(facNummer);
            populateDetailListView();
        }

        registerClick();

    }

    private void registerClick() {
        ListView detailList = (ListView)findViewById(R.id.listView_detail);
        detailList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startSemesterActivity(position);
            }
        });
    }

    private void startSemesterActivity(int id) {
        //hier muss weiter implementiert werden

    }

    private void populateDetailListView() {
        ArrayAdapter<String> facAdapter = new FACListAdapter();
        ListView listView_main = (ListView) findViewById(R.id.listView_detail);
        listView_main.setAdapter(facAdapter);
    }

    private void populateDetailData(int facNummer) {
        SQLiteDatabase read = db.getReadableDatabase();
        String whereClause = "facNummer = " + facNummer;
        Cursor c = read.query("facData", new String[]{"facName"}, whereClause, null, null, null, null);
        while (c.moveToNext()) {
            System.out.println("DB: " + c.getString(c.getColumnIndex("facName")));
            facNames.add(c.getString(c.getColumnIndex("facName")));
        }
    }


    private class FACListAdapter extends ArrayAdapter<String> {

        public FACListAdapter() {
            super(DetailActivity.this, R.layout.item_layout, facNames);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View itemView = convertView;
            if (itemView == null)
                itemView = getLayoutInflater().inflate(R.layout.item_layout, parent, false);

            String currentFAC = facNames.get(position);

            TextView textLayout = (TextView) itemView.findViewById(R.id.item_text);
            textLayout.setText(currentFAC);

            return itemView;
        }
    }
}



