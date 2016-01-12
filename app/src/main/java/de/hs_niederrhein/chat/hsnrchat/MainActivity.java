package de.hs_niederrhein.chat.hsnrchat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hs_niederrhein.chat.hsnrchat.Database.DatabaseOpenHelper;
//import de.hs_niederrhein.chat.hsnrchat.Networking.Talker;
import de.hs_niederrhein.chat.hsnrchat.types.Faculty;

public class MainActivity extends AppCompatActivity {
    private DatabaseOpenHelper db = new DatabaseOpenHelper(this);
    private List<Faculty> facData = new ArrayList<>();
    private static List<Activity> _activities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db.insertFaculties();

        populateFACData();
        populateFACListView();
        registerClick();
    }

    private void registerClick() {
        ListView facList = (ListView)findViewById(R.id.listView_main);
        facList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startChatActivity(position);

            }
        });
    }

    public static void register(Activity activity) {
        if(_activities == null) {
            _activities = new ArrayList<Activity>();
        }
        _activities.add(activity);
    }

    public static void finishAll() {
        for (Activity activity : _activities) {
            activity.finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        db.deleteContentOfMessageCache();
        this.finishAll();
        this.finish();

    }
    private void startChatActivity(int position) {
        System.out.println("Position:" + position);
        Intent changeToChatView = new Intent(this, ChatActivity.class);
        changeToChatView.putExtra("facID", ++position);
        startActivity(changeToChatView);
    }

    private void populateFACData() {
        SQLiteDatabase read = db.getReadableDatabase();
        Cursor c = read.query("faculties", new String[]{"facNummer","facName","facIcon"},null,null,null,null,null);
        while(c.moveToNext()){
            facData.add(new Faculty(c.getInt(c.getColumnIndex("facNummer")),c.getString(c.getColumnIndex("facName")),c.getInt(c.getColumnIndex("facIcon"))));
        }

    }

    private void populateFACListView() {
        ArrayAdapter<Faculty> facAdapter = new FACListAdapter();
        ListView listView_main = (ListView)findViewById(R.id.listView_main);
        listView_main.setAdapter(facAdapter);
    }



    private class FACListAdapter extends ArrayAdapter<Faculty>
    {

        public FACListAdapter() {
            super(MainActivity.this, R.layout.fac_layout, facData);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null)
                itemView = getLayoutInflater().inflate(R.layout.fac_layout, parent, false);

            Faculty currentFAC = facData.get(position);

            TextView textLayout = (TextView)itemView.findViewById(R.id.item_text);
            textLayout.setText(currentFAC.getFacName());

            ImageView imageView = (ImageView)itemView.findViewById(R.id.item_icon);
            imageView.setImageResource(currentFAC.getFacIcon());

            return itemView;
        }


    }




}
