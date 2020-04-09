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
    private String identity = "CHAT_USER";

    private MessagesAdapter messagesAdapter;

    private EditText writeMessageEditText;

    private final QuickstartChatManager quickstartChatManager = new QuickstartChatManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quickstartChatManager.setChatManagerListener(this);

        RecyclerView recyclerView = findViewById(R.id.messagesRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // for a chat app, show latest messages at the bottom
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        messagesAdapter = new MessagesAdapter();
        recyclerView.setAdapter(messagesAdapter);

        writeMessageEditText = findViewById(R.id.writeMessageEditText);

        setTitle(identity);

        Button sendChatMessageButton = findViewById(R.id.sendChatMessageButton);
        sendChatMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageBody = writeMessageEditText.getText().toString();
                quickstartChatManager.sendChatMessage(messageBody);
            }
        });

        quickstartChatManager.retrieveAccessTokenFromServer(this, identity);
    }

    @Override
    public void receivedNewMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // need to modify user interface elements on the UI thread
                messagesAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void messageSentCallback() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // need to modify user interface elements on the UI thread
                writeMessageEditText.setText("");
            }
        });
    }

    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {

            final TextView messageTextView;

            ViewHolder(TextView textView) {
                super(textView);
                messageTextView = textView;
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
            holder.messageTextView.setText(messageText);
        }

        @Override
        public int getItemCount() {
            return quickstartChatManager.getMessages().size();
        }
    }
}
