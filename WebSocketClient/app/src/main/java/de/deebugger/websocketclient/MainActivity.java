package de.deebugger.websocketclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Chat chat;
    ConnectionDialog connectionDialog;
    WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //this is the starting point

        //display the main view element
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //connect to the ui elements from xml ui
        TextView appName = findViewById(R.id.appNameLabel);
        ImageView settings = findViewById(R.id.settingsImage);




        //create chat layout inside of main view
        chat = new Chat((ConstraintLayout) findViewById(R.id.constraintLayout)) {
            @Override
            public void onSendMessage(String text) { //when the user presses the send button...
                if (webSocket != null) { //webSocket might be null when not connected!
                    webSocket.send(text);
                }
                chat.addMessage(text, Chat.OUTGOING_MESSAGE);
            }
        };
        chat.addMessage(getString(R.string.message_hello).replace("%version", BuildConfig.VERSION_NAME),Chat.SYSTEM_MESSAGE);



        //setup of the connection dialog popup
        connectionDialog = new ConnectionDialog(this) {

            @Override
            public void onStartConnection(String url , int pingInterval) { //when the user presses the connect button...
                if(webSocket != null) {//if the user hits the connect button while a connection is alive
                    onEndConnection();
               }

                OkHttpClient client = new OkHttpClient.Builder().pingInterval(pingInterval, TimeUnit.MILLISECONDS).build();
                Request request = new Request.Builder().url(url).build();
                WebSocketListener wsl = new WebSocketListener() {
                    @Override
                    public void onOpen(@NotNull final WebSocket socket, @NotNull Response response) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                chat.addMessage(getString(R.string.message_connected), Chat.SYSTEM_MESSAGE);
                                connectionDialog.connectionIsAccomplished(true);
                            }
                        });

                    }

                    @Override
                    public void onMessage(@NotNull WebSocket socket, @NotNull final String text) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                chat.addMessage(text, Chat.INCOMING_MESSAGE);
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NotNull final WebSocket socket, @NotNull final Throwable t, final Response response) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if(webSocket != null){ //if app user disconnects, a message will be added to the chat and onFailure is prevented from adding another (because onFailure doesn't always trigger when you disconnect)
                                chat.addMessage(getString(R.string.message_disconnected).replace("%reason", Objects.requireNonNull(t.getMessage())), Chat.SYSTEM_MESSAGE);
                                connectionDialog.connectionIsAccomplished(false);
                                showConnectionDialog(R.string.dialog_button_demo_mode);
                                webSocket = null;
                                }
                            }
                        });
                    }
                };
                chat.addMessage(getString(R.string.message_connecting).replace("%clientAddr", url ), Chat.SYSTEM_MESSAGE);
                webSocket = client.newWebSocket(request, wsl);
            }

            @Override
            public void onEndConnection() { //when the user hits the disconnect button (if connected)...
                    webSocket.close(1000,"This connection was terminated by the user");
                    webSocket = null;
                    chat.addMessage(getString(R.string.message_disconnected).replace("%reason", getString(R.string.reason_self_disconnect) ), Chat.SYSTEM_MESSAGE);
            }
        };





        if(connectionDialog.lastConnectionWasAccomplished()){ //if the app accomplished a successful connection the last time running...
            if(!connectionDialog.restartOldConnection()){ //it will try to restart the connection with the saved address...
              connectionDialog.showConnectionDialog(ConnectionDialog.DIALOG_START); //if this doesn't work, it will open up the connection dialog popup
            }
        }else{
            connectionDialog.showConnectionDialog(ConnectionDialog.DIALOG_START); //if there was no successful connection it will open up the connection dialog popup
        }





        //on click listener for the app name label
        appName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url)))); //this will open the github page in the default browser
            }
        });


        settings.setOnClickListener(new View.OnClickListener() { //when you click the settings icon
            @Override
            public void onClick(View v) {
                if(webSocket == null){ //if there is no connection...
                    connectionDialog.showConnectionDialog(ConnectionDialog.DIALOG_DISCONNECTED); //it will open the connection dialog WITHOUT the disconnect button
                }else{
                    connectionDialog.showConnectionDialog(ConnectionDialog.DIALOG_CONNECTED); //it will open the connection dialog WITH the disconnect button
                }
            }
        });



    }

}
