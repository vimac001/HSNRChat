package de.hs_niederrhein.chat.hsnrchat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import de.hs_niederrhein.chat.hsnrchat.types.Finisher;

public class MainActivity extends AppCompatActivity {
    private DatabaseOpenHelper db = new DatabaseOpenHelper(this);
    private List<Faculty> facData = new ArrayList<>();
    private static List<Activity> _activities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Finisher.register(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db.insertFaculties();

        populateFACData();
        populateFACListView();
        registerClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.fac:
                //
                return true;
            case R.id.location:
                changeToLocationActivity();
                return true;
            case R.id.logout:
                Finisher.finishApp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void registerClick() {
        ListView facList = (ListView) findViewById(R.id.listView_main);
        facList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startChatActivity(position);

            }
        });
    }

    private void changeToLocationActivity(){
        Intent changeToLocationActivity = new Intent(this,LocationActivity.class);
        startActivity(changeToLocationActivity);
    }







    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Finisher.finishApp();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        db.deleteContentOfMessageCache();
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
