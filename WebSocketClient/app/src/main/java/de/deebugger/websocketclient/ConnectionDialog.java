package de.deebugger.websocketclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.widget.*;


public abstract class ConnectionDialog {

    static final int DIALOG_DISCONNECTED = -1;
    static final int DIALOG_START = 0;
    static final int DIALOG_CONNECTED = 1;


    Context context;
    SharedPreferences sharedPreferences;



    public ConnectionDialog(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("DEEBUGGER-WSC-DATA0", 0);
    }

    public boolean restartOldConnection(){
        String address = sharedPreferences.getString("address", "");
        int port = sharedPreferences.getInt("port", 0);
        int pingInterval = sharedPreferences.getInt("ping", 0);
        if (!address.equals("") && port != 0 && pingInterval >= 500) {
            if(!address.startsWith("ws://") && !address.startsWith("wss://")){
                address = "ws://"+address;
            }
            if(!address.endsWith(":")){
                address += ":";
            }
            onStartConnection(address+port, pingInterval);
            return true;
        }
        return false;
    }

    public void showConnectionDialog(int type) {
        AlertDialog.Builder settingsDialogBuilder = new AlertDialog.Builder(context);
        if (type == DIALOG_START) {
            settingsDialogBuilder.setTitle(R.string.dialog_title_setup);
        }else{
            settingsDialogBuilder.setTitle(R.string.dialog_title_settings);
        }

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView tv_address = new TextView(context);
        tv_address.setText(R.string.tv_address_text);
        linearLayout.addView(tv_address);

        final EditText addressInput = new EditText(context);
        addressInput.setHint("echo.websocket.org / 192.168...");
        linearLayout.addView(addressInput);

        TextView tv_port = new TextView(context);
        tv_port.setText(R.string.tv_port_text);
        linearLayout.addView(tv_port);

        final EditText portInput = new EditText(context);
        portInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        portInput.setHint("80 / 81...");
        linearLayout.addView(portInput);

        TextView tv_ping = new TextView(context);
        tv_ping.setText(R.string.tv_ping_text);
        linearLayout.addView(tv_ping);

        final Integer[] ping_intervals = new Integer[]{500, 1000, 2000};

        final Spinner pingSpinner = new Spinner(context);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, ping_intervals);
        pingSpinner.setAdapter(adapter);
        pingSpinner.setSelection(1);
        linearLayout.addView(pingSpinner);
        
        
        String address_old = sharedPreferences.getString("address", "");
        if (!address_old.equals("")) {
            addressInput.setText(address_old);
        }


        int port_old = sharedPreferences.getInt("port", 0);
        if (port_old != 0) {
            portInput.setText(String.valueOf(port_old));
        }

        int ping_old = sharedPreferences.getInt("ping", 0);

        for (int i = 0; i < ping_intervals.length; i++) {
            if (ping_intervals[i] == ping_old) {
                pingSpinner.setSelection(i);
                break;
            }
        }

        settingsDialogBuilder.setView(linearLayout);


         if (type == DIALOG_DISCONNECTED){

             settingsDialogBuilder.setPositiveButton(R.string.dialog_button_connect, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     if (validateUserInput(addressInput.getText().toString(), portInput.getText().toString(), (int) pingSpinner.getSelectedItem())) {
                         dialogInterface.dismiss();
                     } else {
                         Toast.makeText(context, context.getText(R.string.dialog_false_entry), Toast.LENGTH_LONG).show();
                     }
                 }
             });

             settingsDialogBuilder.setNegativeButton(R.string.dialog_button_resume, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     dialogInterface.dismiss();
                 }
             });

        }else if (type == DIALOG_CONNECTED){

             settingsDialogBuilder.setPositiveButton(R.string.dialog_button_connect, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 if (validateUserInput(addressInput.getText().toString(), portInput.getText().toString(), (int) pingSpinner.getSelectedItem())) {
                     dialogInterface.dismiss();
                 } else {
                     Toast.makeText(context, context.getText(R.string.dialog_false_entry), Toast.LENGTH_LONG).show();
                 }
             }
         });

             settingsDialogBuilder.setNeutralButton(R.string.dialog_button_disconnect, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                    onEndConnection();
                    dialogInterface.dismiss();
                 }
             });


             settingsDialogBuilder.setNegativeButton(R.string.dialog_button_resume, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     dialogInterface.dismiss();
                 }
             });

        }else{

             settingsDialogBuilder.setPositiveButton(R.string.dialog_button_connect, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     if (validateUserInput(addressInput.getText().toString(), portInput.getText().toString(), (int) pingSpinner.getSelectedItem())) {
                         dialogInterface.dismiss();
                     } else {
                         Toast.makeText(context, context.getText(R.string.dialog_false_entry), Toast.LENGTH_LONG).show();
                     }
                 }
             });


             settingsDialogBuilder.setNegativeButton(R.string.dialog_button_demo_mode, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     dialogInterface.dismiss();
                 }
             });

        }



        settingsDialogBuilder.show();
    }

    public boolean validateUserInput(String address,  String portString, int pingInterval){

        int port;
        try {
            port = Integer.parseInt(portString.trim());
        }catch (NumberFormatException e){
            port = 0;
        }

        address.trim();

        if (!address.equals("")         &&          port > 0 && port < 65536        &&       pingInterval >= 500 ) {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("address", address);
            editor.putInt("port", port);
            editor.putInt("ping", pingInterval);
            editor.apply();

            if(!address.startsWith("ws://") && !address.startsWith("wss://")){
                address = "ws://"+address;
            }
            if(!address.endsWith(":")){
                address += ":";
            }

            onStartConnection(address+port, pingInterval);
            return true;
        }
        return false;

    }

    public void connectionIsAccomplished(boolean success){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isAccomplished", success);
        editor.apply();
    }

    public boolean lastConnectionWasAccomplished(){
        return sharedPreferences.getBoolean("isAccomplished", false);
    }

    public abstract void onStartConnection(String url, int pingInterval);

    public abstract void onEndConnection();



}
