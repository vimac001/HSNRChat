package de.hs_niederrhein.chat.hsnrchat;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_niederrhein.chat.hsnrchat.Database.DatabaseOpenHelper;
import de.hs_niederrhein.chat.hsnrchat.Type.Faculty;

public class DetailActivity extends AppCompatActivity {
    private List<Faculty> faculties = new ArrayList<Faculty>();
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

    private void startSemesterActivity(int position) {
        Faculty clickedFAC = faculties.get(position);
        Intent changeToChatActivity = new Intent(this,ChatActivity.class);
        changeToChatActivity.putExtra("facID", clickedFAC.getFacID());
        startActivity(changeToChatActivity);


    }

    private void populateDetailListView() {
        ArrayAdapter<Faculty> facAdapter = new FACListAdapter();
        ListView listView_main = (ListView) findViewById(R.id.listView_detail);
        listView_main.setAdapter(facAdapter);
    }

    private void populateDetailData(int facNummer) {
        SQLiteDatabase read = db.getReadableDatabase();
        String whereClause = "facNummer = " + facNummer;
        Cursor c = read.query("facData", new String[]{"facID","facName"}, whereClause, null, null, null, null);
        while (c.moveToNext()) {
            System.out.println("DB: " + c.getString(c.getColumnIndex("facName")));
            faculties.add(new Faculty(c.getInt(c.getColumnIndex("facID")),c.getString(c.getColumnIndex("facName"))));
        }
        changeIfThereIsOnlyOneEntry();
    }

    private void changeIfThereIsOnlyOneEntry() {
        if(faculties.size() == 1){
            Intent changeToChatActivity = new Intent(this,ChatActivity.class);
            changeToChatActivity.putExtra("facID", faculties.get(0).getFacID());
            startActivity(changeToChatActivity);
        }
    }


    private class FACListAdapter extends ArrayAdapter<Faculty> {

        public FACListAdapter() {
            super(DetailActivity.this, R.layout.listing_layout, faculties);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View itemView = convertView;
            if (itemView == null)
                itemView = getLayoutInflater().inflate(R.layout.listing_layout, parent, false);

            Faculty currentFAC = faculties.get(position);

            TextView textLayout = (TextView) itemView.findViewById(R.id.idText);
            textLayout.setText(currentFAC.getFacName());

            return itemView;
        }
    }
}



