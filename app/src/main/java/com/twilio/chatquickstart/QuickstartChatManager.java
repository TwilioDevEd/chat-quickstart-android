package com.twilio.chatquickstart;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.Channel;
import com.twilio.chat.ChannelListener;
import com.twilio.chat.ChatClient;
import com.twilio.chat.ErrorInfo;
import com.twilio.chat.Member;
import com.twilio.chat.Message;
import com.twilio.chat.StatusListener;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

interface QuickstartChatManagerListener {
    void reloadMessages();
    void receivedNewMessage();
    void messageSentCallback();
}

class QuickstartChatManager {

    // This is the unique name of the chat channel we are using
    private final static String DEFAULT_CHANNEL_NAME = "general";

    final private ArrayList<Message> messages = new ArrayList<>();

    private ChatClient chatClient;

    private Channel channel;

    private QuickstartChatManagerListener chatManagerListener;

    private class TokenResponse {
        String token;
    }

    void retrieveAccessTokenFromServer(final Context context, String identity) {

        // Set the chat token URL in your strings.xml file
        String chatTokenURL = context.getString(R.string.chat_token_url);
        final String tokenURL = chatTokenURL + "?identity=" + identity;

        new Thread(new Runnable() {
            @Override
            public void run() {
                retrieveToken(context, tokenURL);
            }
        }).start();
    }

    private void retrieveToken(final Context context, String tokenURL) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(tokenURL)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            Gson gson = new Gson();
            TokenResponse tokenResponse = gson.fromJson(responseBody,TokenResponse.class);
            String accessToken = tokenResponse.token;
            Log.d(MainActivity.TAG, "Retrieved access token from server: " + accessToken);

            ChatClient.Properties.Builder builder = new ChatClient.Properties.Builder();
            ChatClient.Properties props = builder.createProperties();
            ChatClient.create(context, accessToken, props, mChatClientCallback);

        }
        catch (IOException ex) {
            Log.e(MainActivity.TAG, ex.getLocalizedMessage(),ex);
                /*Toast.makeText(context,
                        R.string.error_retrieving_access_token, Toast.LENGTH_SHORT)
                        .show();*/
        }
    }

    void sendChatMessage(String messageBody) {
        if (channel != null) {
            Message.Options options = Message.options().withBody(messageBody);
            Log.d(MainActivity.TAG,"Message created");
            channel.getMessages().sendMessage(options, new CallbackListener<Message>() {
                @Override
                public void onSuccess(Message message) {
                    if (chatManagerListener != null) {
                        chatManagerListener.messageSentCallback();
                    }
                }
            });
        }
    }


    private void loadChannels() {
        chatClient.getChannels().getChannel(DEFAULT_CHANNEL_NAME, new CallbackListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) {
                if (channel != null) {
                    if (channel.getStatus() == Channel.ChannelStatus.JOINED
                            || channel.getStatus() == Channel.ChannelStatus.NOT_PARTICIPATING) {
                        Log.d(MainActivity.TAG, "Already Exists in Channel: " + DEFAULT_CHANNEL_NAME);
                        QuickstartChatManager.this.channel = channel;
                        QuickstartChatManager.this.channel.addListener(mDefaultChannelListener);
                    } else {
                        Log.d(MainActivity.TAG, "Joining Channel: " + DEFAULT_CHANNEL_NAME);
                        joinChannel(channel);
                    }
                } else {
                    Log.d(MainActivity.TAG, "Creating Channel: " + DEFAULT_CHANNEL_NAME);

                    chatClient.getChannels().createChannel(DEFAULT_CHANNEL_NAME,
                            Channel.ChannelType.PRIVATE, new CallbackListener<Channel>() {
                                @Override
                                public void onSuccess(Channel channel) {
                                    if (channel != null) {
                                        Log.d(MainActivity.TAG, "Joining Channel: " + DEFAULT_CHANNEL_NAME);
                                        joinChannel(channel);
                                    }
                                }

                                @Override
                                public void onError(ErrorInfo errorInfo) {
                                    Log.e(MainActivity.TAG, "Error creating channel: " + errorInfo.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(MainActivity.TAG, "Error retrieving channel: " + errorInfo.getMessage());
            }

        });
    }

    private void joinChannel(final Channel channel) {
        Log.d(MainActivity.TAG, "Joining Channel: " + channel.getUniqueName());
        channel.join(new StatusListener() {
            @Override
            public void onSuccess() {
                QuickstartChatManager.this.channel = channel;
                Log.d(MainActivity.TAG, "Joined default channel");
                QuickstartChatManager.this.channel.addListener(mDefaultChannelListener);
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(MainActivity.TAG, "Error joining channel: " + errorInfo.getMessage());
            }
        });
    }

    private final CallbackListener<ChatClient> mChatClientCallback =
            new CallbackListener<ChatClient>() {
                @Override
                public void onSuccess(ChatClient chatClient) {
                    QuickstartChatManager.this.chatClient = chatClient;
                    loadChannels();
                    Log.d(MainActivity.TAG, "Success creating Twilio Chat Client");
                }

                @Override
                public void onError(ErrorInfo errorInfo) {
                    Log.e(MainActivity.TAG, "Error creating Twilio Chat Client: " + errorInfo.getMessage());
                }
            };


    private final ChannelListener mDefaultChannelListener = new ChannelListener() {


        @Override
        public void onMessageAdded(final Message message) {
            Log.d(MainActivity.TAG, "Message added");
            messages.add(message);
            if (chatManagerListener != null) {
                chatManagerListener.receivedNewMessage();
            }
        }

        @Override
        public void onMessageUpdated(Message message, Message.UpdateReason updateReason) {
            Log.d(MainActivity.TAG, "Message updated: " + message.getMessageBody());
        }

        @Override
        public void onMessageDeleted(Message message) {
            Log.d(MainActivity.TAG, "Message deleted");
        }

        @Override
        public void onMemberAdded(Member member) {
            Log.d(MainActivity.TAG, "Member added: " + member.getIdentity());
        }

        @Override
        public void onMemberUpdated(Member member, Member.UpdateReason updateReason) {
            Log.d(MainActivity.TAG, "Member updated: " + member.getIdentity());
        }

        @Override
        public void onMemberDeleted(Member member) {
            Log.d(MainActivity.TAG, "Member deleted: " + member.getIdentity());
        }

        @Override
        public void onTypingStarted(Channel channel, Member member) {
            Log.d(MainActivity.TAG, "Started Typing: " + member.getIdentity());
        }

        @Override
        public void onTypingEnded(Channel channel, Member member) {
            Log.d(MainActivity.TAG, "Ended Typing: " + member.getIdentity());
        }

        @Override
        public void onSynchronizationChanged(Channel channel) {

        }
    };

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setChatManagerListener(QuickstartChatManagerListener listener)  {
        this.chatManagerListener = listener;
    }
}

