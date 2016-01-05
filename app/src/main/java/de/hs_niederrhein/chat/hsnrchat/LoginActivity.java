package de.hs_niederrhein.chat.hsnrchat;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ClientErrorException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ConnectionTimeoutException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.InvalidResponseStatusException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.ServerErrorException;
import de.hs_niederrhein.chat.hsnrchat.Networking.Exception.UserNotFoundException;
import de.hs_niederrhein.chat.hsnrchat.types.ClientServerCommunciator;

public class LoginActivity extends AppCompatActivity {
    private String _username;
    private String _password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // ClientServerCommunciator.connect("0.0.0.0",1337); //Richtige IP ergänzen
        Button bt_login = (Button)findViewById(R.id.bt_login);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerClick();
            }
        });


    }

    private void registerClick() {
        EditText username = (EditText)findViewById(R.id.input_username);
        this._username = username.getText().toString();

        EditText password = (EditText)findViewById(R.id.input_password);
        this._password = password.getText().toString();

        if(this._username.isEmpty()){
            alert("Bitte Benutzernamen eingeben!");
        }else if(this._password.isEmpty()){
                alert("Bitte Passwort eingeben!");
        }else{
            /*try {
               ClientServerCommunciator.LoginUser(this._username,this._password);
            } catch (UserNotFoundException e) {
                e.printStackTrace();
            } catch (ConnectionTimeoutException e) {
                e.printStackTrace();
            } catch (InvalidResponseStatusException e) {
                e.printStackTrace();
            } catch (ServerErrorException e) {
                e.printStackTrace();
            } catch (ClientErrorException e) {
                e.printStackTrace();
            }*/
            changeToMainActivity();
        }


    }


    private void alert(String s) {
        new AlertDialog.Builder(this)
            .setMessage(s)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            })
            .show();
    }

    private void changeToMainActivity() {
        Intent changeToMainActivity = new Intent(this, MainActivity.class);
        startActivity(changeToMainActivity);
    }

}
