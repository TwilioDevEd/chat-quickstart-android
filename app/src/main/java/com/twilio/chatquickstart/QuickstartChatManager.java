package com.twilio.chatquickstart;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.Channel;
import com.twilio.chat.ChannelListener;
import com.twilio.chat.ChatClient;
import com.twilio.chat.ErrorInfo;
import com.twilio.chat.Member;
import com.twilio.chat.Message;
import com.twilio.chat.StatusListener;

import java.util.ArrayList;

interface QuickstartChatManagerListener {
    void reloadMessages();
    void receivedNewMessage();
    void messageSentCallback();
}

class QuickstartChatManager {

    private final static String TAG = "TwilioChat";

    private final static String DEFAULT_CHANNEL_NAME = "general";

    final private ArrayList<Message> mMessages = new ArrayList<>();

    private ChatClient mChatClient;

    private Channel mChannel;

    private QuickstartChatManagerListener chatManagerListener;

    void retrieveAccessTokenFromServer(final Context context, String identity) {

        // Set the chat token URL in your strings.xml file
        String chatTokenURL = context.getString(R.string.chat_token_url);
        String tokenURL = chatTokenURL + "?identity=" + identity;

        Ion.with(context)
                .load(tokenURL)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e == null) {
                            String accessToken = result.get("token").getAsString();

                            Log.d(TAG, "Retrieved access token from server: " + accessToken);


                            ChatClient.Properties.Builder builder = new ChatClient.Properties.Builder();
                            ChatClient.Properties props = builder.createProperties();
                            ChatClient.create(context, accessToken, props, mChatClientCallback);

                        } else {
                            Log.e(MainActivity.TAG, e.getMessage(), e);
                            Toast.makeText(context,
                                    R.string.error_retrieving_access_token, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    void sendChatMessage(String messageBody) {
        if (mChannel != null) {
            Message.Options options = Message.options().withBody(messageBody);
            Log.d(TAG,"Message created");
            mChannel.getMessages().sendMessage(options, new CallbackListener<Message>() {
                @Override
                public void onSuccess(Message message) {
                    if (chatManagerListener != null) {
                        chatManagerListener.messageSentCallback();
                    }
                }
            });
        }
    };


    private void loadChannels() {
        mChatClient.getChannels().getChannel(DEFAULT_CHANNEL_NAME, new CallbackListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) {
                if (channel != null) {
                    if (channel.getStatus() == Channel.ChannelStatus.JOINED
                            || channel.getStatus() == Channel.ChannelStatus.NOT_PARTICIPATING) {
                        Log.d(TAG, "Already Exists in Channel: " + DEFAULT_CHANNEL_NAME);
                        mChannel = channel;
                        mChannel.addListener(mDefaultChannelListener);
                    } else {
                        Log.d(TAG, "Joining Channel: " + DEFAULT_CHANNEL_NAME);
                        joinChannel(channel);
                    }
                } else {
                    Log.d(TAG, "Creating Channel: " + DEFAULT_CHANNEL_NAME);

                    mChatClient.getChannels().createChannel(DEFAULT_CHANNEL_NAME,
                            Channel.ChannelType.PRIVATE, new CallbackListener<Channel>() {
                                @Override
                                public void onSuccess(Channel channel) {
                                    if (channel != null) {
                                        Log.d(TAG, "Joining Channel: " + DEFAULT_CHANNEL_NAME);
                                        joinChannel(channel);
                                    }
                                }

                                @Override
                                public void onError(ErrorInfo errorInfo) {
                                    Log.e(TAG, "Error creating channel: " + errorInfo.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG, "Error retrieving channel: " + errorInfo.getMessage());
            }

        });
    }

    private void joinChannel(final Channel channel) {
        Log.d(TAG, "Joining Channel: " + channel.getUniqueName());
        channel.join(new StatusListener() {
            @Override
            public void onSuccess() {
                mChannel = channel;
                Log.d(TAG, "Joined default channel");
                mChannel.addListener(mDefaultChannelListener);
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG, "Error joining channel: " + errorInfo.getMessage());
            }
        });
    }

    private CallbackListener<ChatClient> mChatClientCallback =
            new CallbackListener<ChatClient>() {
                @Override
                public void onSuccess(ChatClient chatClient) {
                    mChatClient = chatClient;
                    loadChannels();
                    Log.d(TAG, "Success creating Twilio Chat Client");
                }

                @Override
                public void onError(ErrorInfo errorInfo) {
                    Log.e(TAG, "Error creating Twilio Chat Client: " + errorInfo.getMessage());
                }
            };


    private ChannelListener mDefaultChannelListener = new ChannelListener() {


        @Override
        public void onMessageAdded(final Message message) {
            Log.d(MainActivity.TAG, "Message added");
            mMessages.add(message);
            if (chatManagerListener != null) {
                chatManagerListener.receivedNewMessage();
            }
        }

        @Override
        public void onMessageUpdated(Message message, Message.UpdateReason updateReason) {
            Log.d(TAG, "Message updated: " + message.getMessageBody());
        }

        @Override
        public void onMessageDeleted(Message message) {
            Log.d(TAG, "Message deleted");
        }

        @Override
        public void onMemberAdded(Member member) {
            Log.d(TAG, "Member added: " + member.getIdentity());
        }

        @Override
        public void onMemberUpdated(Member member, Member.UpdateReason updateReason) {
            Log.d(TAG, "Member updated: " + member.getIdentity());
        }

        @Override
        public void onMemberDeleted(Member member) {
            Log.d(TAG, "Member deleted: " + member.getIdentity());
        }

        @Override
        public void onTypingStarted(Channel channel, Member member) {
            Log.d(TAG, "Started Typing: " + member.getIdentity());
        }

        @Override
        public void onTypingEnded(Channel channel, Member member) {
            Log.d(TAG, "Ended Typing: " + member.getIdentity());
        }

        @Override
        public void onSynchronizationChanged(Channel channel) {

        }
    };

    public ArrayList<Message> getMessages() {
        return mMessages;
    }

    public void setChatManagerListener(QuickstartChatManagerListener listener)  {
        this.chatManagerListener = listener;
    }
}

