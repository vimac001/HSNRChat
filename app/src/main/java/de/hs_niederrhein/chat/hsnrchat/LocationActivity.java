package de.hs_niederrhein.chat.hsnrchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import de.hs_niederrhein.chat.hsnrchat.Database.DatabaseOpenHelper;

public class LocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.register(this);
        setContentView(R.layout.activity_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                changeToMainActivity();
                return true;
            case R.id.location:

                return true;
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void changeToMainActivity(){
        Intent changeToMainActivity = new Intent(this, MainActivity.class);
        startActivity(changeToMainActivity);
    }

    private void registerClick(){
        Button krS = (Button)findViewById(R.id.btKRS);
        krS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChatActivity(11);
            }
        });

        Button krW = (Button)findViewById(R.id.btKRW);
        krW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChatActivity(12);
            }
        });

        Button mg = (Button)findViewById(R.id.btMG);
        mg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChatActivity(13);
            }
        });
    }

    private void startChatActivity(int position) {
        Intent changeToChatView = new Intent(this, ChatActivity.class);
        changeToChatView.putExtra("facID", position);
        startActivity(changeToChatView);
    }

    public void logout(){
        DatabaseOpenHelper db = new DatabaseOpenHelper(this);
        db.deleteContentOfMessageCache();
        MainActivity.finishAll();
    }

}
