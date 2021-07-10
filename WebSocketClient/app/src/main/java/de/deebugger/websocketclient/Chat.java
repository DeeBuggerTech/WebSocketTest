package de.deebugger.websocketclient;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.os.Build;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;


public abstract class Chat {

    public static final int OUTGOING_MESSAGE = -1;
    public static final int SYSTEM_MESSAGE = 0;
    public static final int INCOMING_MESSAGE = 1;


    private Context context;
    private ScrollView scrollView;
    private ConstraintLayout chatContainer;
    private int lastMessageId;


    float screenDensity;


    public Chat( ConstraintLayout container){
        this.context = container.getContext();

        screenDensity = context.getResources().getDisplayMetrics().density;// this will be used to calculate the font size of the messages

        View chatUI = generateChatUI(); //the chatUI contains the editText and the send button
        container.addView(chatUI);

        scrollView = generateScrollView(); //this is the main scroll view
        container.addView(scrollView);



        ConstraintSet set = new ConstraintSet(); //in the lines below, the chatUI is centered and constrained to the bottom of the viewport and the scroll view is centered and told to fill the space above the chatUI
        set.clone(container);

        set.connect(chatUI.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
        set.connect(chatUI.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.connect(chatUI.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);

        set.connect(scrollView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(scrollView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
        set.connect(scrollView.getId(), ConstraintSet.BOTTOM, chatUI.getId(), ConstraintSet.TOP);
        set.connect(scrollView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);

        set.applyTo(container);



        chatContainer = new ConstraintLayout(context);
        scrollView.addView(chatContainer);
        lastMessageId = chatContainer.getId(); //if it's the first message, its top will be constrained to the top of the parent

    }

    public void addMessage(String text, int messagetype){
        text.trim();
        if (!text.equals("")) {
            int currentId = ViewCompat.generateViewId();
            TextView tv = new TextView(context);
            tv.setId(currentId);
            tv.setText(text);

            if (messagetype == INCOMING_MESSAGE || messagetype == OUTGOING_MESSAGE) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44 / screenDensity);
                tv.setBackground(context.getDrawable(R.drawable.message));
            } else {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,32 / screenDensity);
                tv.setBackground(context.getDrawable(R.drawable.system_message));
            }


            tv.setMaxWidth((int)(context.getResources().getDisplayMetrics().widthPixels * 0.9)); //maximal message width -> 90% of screen width

            chatContainer.addView(tv);

            ConstraintSet set = new ConstraintSet();
            set.clone(chatContainer);

            if (messagetype == INCOMING_MESSAGE) {
                set.connect(currentId, ConstraintSet.TOP, lastMessageId, ConstraintSet.BOTTOM, 10);
                set.connect(currentId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            } else if (messagetype == OUTGOING_MESSAGE) {
                set.connect(currentId, ConstraintSet.TOP, lastMessageId, ConstraintSet.BOTTOM, 10);
                set.connect(currentId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            } else {
                set.connect(currentId, ConstraintSet.TOP, lastMessageId, ConstraintSet.BOTTOM, 10);
                set.connect(currentId, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                set.connect(currentId, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            }
            set.applyTo(chatContainer);

            //https://stackoverflow.com/questions/14801215/scrollview-not-scrolling-down-completely workaround for scrolling issue - without the runnable it didn't scroll all the way down
            scrollView.post(new Runnable() {
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });

            lastMessageId = currentId;


        }



    }


    private ConstraintLayout generateChatUI(){

        ConstraintLayout newChatInterface = new ConstraintLayout(context);
        newChatInterface.setId(ViewCompat.generateViewId());
        newChatInterface.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT));


        final EditText messageInput = new EditText(context);
        messageInput.setId(ViewCompat.generateViewId());
        messageInput.setInputType(InputType.TYPE_CLASS_TEXT);
        messageInput.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.colorAccent2)));
        messageInput.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_PARENT));
        newChatInterface.addView(messageInput);


        Button sendButton = new Button(context);
        sendButton.setId(ViewCompat.generateViewId());
        sendButton.setText(context.getString(R.string.send));
        if (Build.VERSION.SDK_INT >= 29) {
            sendButton.getBackground().setColorFilter(new BlendModeColorFilter(context.getColor(R.color.colorAccent2), BlendMode.MULTIPLY));
        }else {
            sendButton.getBackground().setColorFilter(context.getColor(R.color.colorAccent2), PorterDuff.Mode.MULTIPLY);
        }
        sendButton.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if(message != "") {
                    onSendMessage(message);
                }
                messageInput.setText("");
            }
        });
        newChatInterface.addView(sendButton);


        ConstraintSet set = new ConstraintSet();
        set.clone(newChatInterface);

        set.connect(sendButton.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(sendButton.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
        set.connect(sendButton.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        set.connect(messageInput.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID , ConstraintSet.TOP);
        set.connect(messageInput.getId(), ConstraintSet.RIGHT, sendButton.getId(), ConstraintSet.LEFT);
        set.connect(messageInput.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.connect(messageInput.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);

        set.applyTo(newChatInterface);

        return newChatInterface;
    }

    private ScrollView generateScrollView(){
        ScrollView newScrollView = new ScrollView(context);
        newScrollView.setId(ViewCompat.generateViewId());
        newScrollView.setBackgroundColor(context.getColor(R.color.colorPrimaryDark));
        newScrollView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT));
        newScrollView.setPadding(8,8,8,8);
        return newScrollView;
    }



    public abstract void onSendMessage(String text);



}
