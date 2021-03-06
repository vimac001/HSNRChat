package de.hs_niederrhein.chat.hsnrchat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.hs_niederrhein.chat.hsnrchat.Database.DatabaseOpenHelper;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ClientErrorException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ClientNotAutheticatedException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ConnectionTimeoutException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.InvalidResponseStatusException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.InvalidSSIDException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.RoomNotFoundException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ServerErrorException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.UserNotFoundException;
import de.hs_niederrhein.chat.hsnrchat.Networking.User;
import de.hs_niederrhein.chat.hsnrchat.types.Faculty;
import de.hs_niederrhein.chat.hsnrchat.types.Finisher;
import de.hs_niederrhein.chat.hsnrchat.types.Message;

public class ChatActivity extends AppCompatActivity {
    private int facID;
    private int lastUpdate;
    private List<Message> messages = new ArrayList<>();
    private  ArrayAdapter<Message> messageAdapter;
    private DatabaseOpenHelper db = new DatabaseOpenHelper(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Finisher.register(this);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.lastUpdate = 0;
        //Thread starten um neue Nachrichten zu überwachen
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()){
                    try {
                        populateMessages();
                        Thread.sleep(1000,0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClientNotAutheticatedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        t.start();
        Intent intent = getIntent();
        if(intent.hasExtra("facID")){
            this.facID = intent.getIntExtra("facID",0);
        }
        ListView listView = (ListView) findViewById(R.id.chat_listview);
        listView.smoothScrollToPosition(messages.size());


        populateMessageListView();
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
                changeToLocationActivity();
                return true;
            case R.id.logout:
                Finisher.finishApp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void changeToMainActivity(){
        Intent changeToMainActivity = new Intent(this, MainActivity.class);
        startActivity(changeToMainActivity);
    }
    private void changeToLocationActivity(){
        Intent changeToLocationActivity = new Intent(this, LocationActivity.class);
        startActivity(changeToLocationActivity);
    }

    private long getUserID() throws IOException, ClientNotAutheticatedException {
        return ClientServerCommunicator.get(this).getUserId();
    }

    public void populateMessages() throws IOException, ClientNotAutheticatedException {
        boolean isRight;
        SQLiteDatabase read = db.getReadableDatabase();
        String where_clause = db.facNummer + "=" + this.facID + " AND " + db.timeStamp + " > " + this.lastUpdate   ;
        Cursor c = read.query(db.messages, new String[]{db.facNummer, db.message, db.userID, db.timeStamp}, where_clause,null,null,null, db.timeStamp);
        if(c.getCount() > 0){
            while(c.moveToNext()){
                //Vergleichen ob es der eigene User ist oder nicht
                if(getUserID() == c.getLong(c.getColumnIndex(db.userID)) ){
                    isRight = true;
                }
                else{
                    isRight = false;
                }

                messages.add(new Message(
                        (long)c.getInt(c.getColumnIndex(db.facNummer)),
                        (long)c.getLong(c.getColumnIndex(db.userID)),
                        c.getString(c.getColumnIndex(db.message)),
                        isRight));
                this.lastUpdate = c.getInt(c.getColumnIndex(db.timeStamp));
            }
        }



    }

    private void populateMessageListView() {
        this.messageAdapter = new ChatAdapter();
        ListView listView_chat = (ListView)findViewById(R.id.chat_listview);
        listView_chat.setAdapter(messageAdapter);
    }

    private void registerClick() {
        Button btSend = (Button)findViewById(R.id.chat_btsend);
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.chat_edit);
                if (editText.getText().toString() != "") {

                    //Nachricht an Server schicken
                    try {
                        sendMessageToServer(editText.getText().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClientErrorException e) {
                        e.printStackTrace();
                    } catch (InvalidSSIDException e) {
                        e.printStackTrace();
                    } catch (InvalidResponseStatusException e) {
                        e.printStackTrace();
                    } catch (ServerErrorException e) {
                        e.printStackTrace();
                    } catch (ConnectionTimeoutException e) {
                        e.printStackTrace();
                    } catch (RoomNotFoundException e) {
                        e.printStackTrace();
                    } catch (ClientNotAutheticatedException e) {
                        e.printStackTrace();
                    }


                    //Editfeld leeren
                    editText.setText("");
                    //Tastatur schließen
                    hideKeyboard(ChatActivity.this);

                    //Um immer nach unten zu scrollen
                    ListView listView = (ListView) findViewById(R.id.chat_listview);
                    listView.smoothScrollToPosition(messages.size());
                }


            }
        });
    }

    public void sendMessageToServer(String message) throws IOException, ClientErrorException, InvalidSSIDException, InvalidResponseStatusException, ServerErrorException, ConnectionTimeoutException, RoomNotFoundException, ClientNotAutheticatedException {
        ClientServerCommunicator.get(this).sendMessage((short) this.facID, message);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if(view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private class ChatAdapter extends ArrayAdapter<Message> {

        public ChatAdapter() {
            super(ChatActivity.this, R.layout.item_chat_right, messages);
        }

        @Override
        public void add(Message msg){
            super.add(msg);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            Message currentMessage = messages.get(position);

            if(currentMessage.getIsRight() == true)
                itemView = getLayoutInflater().inflate(R.layout.item_chat_right, parent, false);
            else
                itemView = getLayoutInflater().inflate(R.layout.item_chat_left,parent,false);

            User currentUser = null;
            try {
                currentUser = ClientServerCommunicator.get().resolveUser(currentMessage.getUserID());
            } catch (ServerErrorException e) {
                e.printStackTrace();
            } catch (InvalidSSIDException e) {
                e.printStackTrace();
            } catch (UserNotFoundException e) {
                e.printStackTrace();
            } catch (ClientNotAutheticatedException e) {
                e.printStackTrace();
            } catch (ClientErrorException e) {
                e.printStackTrace();
            } catch (ConnectionTimeoutException e) {
                e.printStackTrace();
            } catch (InvalidResponseStatusException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String textToShow = currentUser.getDisplayName() + ": " + currentMessage.getMessage();

            TextView textLayout = (TextView) itemView.findViewById(R.id.id_text);
            textLayout.setText(textToShow);

            return itemView;
        }
    }
}
