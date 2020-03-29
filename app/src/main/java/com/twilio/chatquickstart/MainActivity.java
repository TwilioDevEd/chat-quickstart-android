package com.twilio.chatquickstart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.twilio.chat.Message;

public class MainActivity extends AppCompatActivity implements QuickstartChatManagerListener {

    public final static String TAG = "TwilioChat";

    // Update this identity for each individual user, for instance after they login
    private String mIdentity = "CHAT_USER";

    private MessagesAdapter mMessagesAdapter;

    private EditText mWriteMessageEditText;

    private QuickstartChatManager quickstartChatManager = new QuickstartChatManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quickstartChatManager.setChatManagerListener(this);

        RecyclerView recyclerView = findViewById(R.id.messagesRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // for a chat app, show latest at the bottom
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        mMessagesAdapter = new MessagesAdapter();
        recyclerView.setAdapter(mMessagesAdapter);

        mWriteMessageEditText = findViewById(R.id.writeMessageEditText);

        setTitle(mIdentity);


        Button sendChatMessageButton = findViewById(R.id.sendChatMessageButton);
        sendChatMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageBody = mWriteMessageEditText.getText().toString();
                quickstartChatManager.sendChatMessage(messageBody);
            }
        });

        quickstartChatManager.retrieveAccessTokenFromServer(this, mIdentity);
    }



    @Override
    public void reloadMessages() {

    }

    @Override
    public void receivedNewMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // need to modify user interface elements on the UI thread
                mMessagesAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void messageSentCallback() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // need to modify user interface elements on the UI thread
                mWriteMessageEditText.setText("");
            }
        });
    }

    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView mMessageTextView;

            ViewHolder(TextView textView) {
                super(textView);
                mMessageTextView = textView;
            }
        }

        MessagesAdapter() {

        }

        @NonNull
        @Override
        public MessagesAdapter
                .ViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
            TextView messageTextView = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_text_view, parent, false);
            return new ViewHolder(messageTextView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Message message = quickstartChatManager.getMessages().get(position);
            String messageText = String.format("%s: %s", message.getAuthor(), message.getMessageBody());
            holder.mMessageTextView.setText(messageText);

        }

        @Override
        public int getItemCount() {
            return quickstartChatManager.getMessages().size();
        }
    }


}
