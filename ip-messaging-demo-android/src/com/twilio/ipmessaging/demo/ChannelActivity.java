package com.twilio.ipmessaging.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.twilio.example.R;
import com.twilio.ipmessaging.Channel;
import com.twilio.ipmessaging.Channel.ChannelType;
import com.twilio.ipmessaging.ChannelListener;
import com.twilio.ipmessaging.Channels;
import com.twilio.ipmessaging.Member;
import com.twilio.ipmessaging.Message;
import com.twilio.ipmessaging.Messages;
import com.twilio.ipmessaging.Constants.CreateChannelListener;
import com.twilio.ipmessaging.Constants;
import com.twilio.ipmessaging.Constants.StatusListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import uk.co.ribot.easyadapter.EasyAdapter;

@SuppressLint("InflateParams")
public class ChannelActivity extends Activity implements ChannelListener {

	private static final String[] CHANNEL_OPTIONS = { "Join" };
	private static final Logger logger = Logger.getLogger(ChannelActivity.class);
	private static final int JOIN = 0;
	//private static final int DESTROY = 1;

	private ListView listView;
	private BasicIPMessagingClient basicClient;
	private List<Channel> channels = new ArrayList<Channel>();
	private EasyAdapter<Channel> adapter;
	private AlertDialog createChannelDialog;
	private Channels channelsObject;
	private Channel[] channelArray;
	
	private static final Handler handler = new Handler();
	private AlertDialog incoingChannelInvite;
	private ProgressDialog progressDialog;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel);
		basicClient = TwilioApplication.get().getBasicClient();
		setupListView();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.channel, menu);
		final Channel channelSelected = channelsObject.getChannel("CH55b0c05bc64a40b4951045546f3f1e40");
		Intent i = new Intent(ChannelActivity.this, MessageActivity.class);
		i.putExtra(Constants.EXTRA_CHANNEL, (Parcelable) channelSelected);
		i.putExtra("C_SID", channelSelected.getSid());
		startActivity(i);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_create_public:
			showCreateChannelDialog(ChannelType.CHANNEL_TYPE_PUBLIC);
			break;
		case R.id.action_create_private:
			showCreateChannelDialog(ChannelType.CHANNEL_TYPE_PRIVATE);
			break;
		case R.id.action_logout:
			if(this.basicClient != null & this.basicClient.getIpMessagingClient() != null) {
				this.basicClient.getIpMessagingClient().shutdown();
				finish();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();		
		handleIncomingIntent(getIntent());
		getChannels(null);
	}

	private boolean handleIncomingIntent(Intent intent) {
		if(intent != null) {
			Channel channel = intent.getParcelableExtra(Constants.EXTRA_CHANNEL);
			String action = intent.getStringExtra(Constants.EXTRA_ACTION);
			intent.removeExtra(Constants.EXTRA_CHANNEL);
		    intent.removeExtra(Constants.EXTRA_ACTION);
			if(action != null) {
				if(action.compareTo(Constants.EXTRA_ACTION_INVITE) == 0 ) {
					this.showIncomingInvite(channel);
				}
			}
		}
		return false;
	}

	private void showCreateChannelDialog(final ChannelType type) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ChannelActivity.this);
		// Get the layout inflater
		LayoutInflater inflater = getLayoutInflater();
		String title = "Enter " + type.toString() + " name";

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(inflater.inflate(R.layout.dialog_add_channel, null)).setTitle(title)
				.setPositiveButton("Create", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						
						String channelName = ((EditText) createChannelDialog.findViewById(R.id.channel_name)).getText()
								.toString();
						logger.e(channelName);
						Channels channelsLocal= basicClient.getIpMessagingClient().getChannels();
						channelsLocal.createChannel(channelName,type, new CreateChannelListener()
				        {
				            @Override
				            public void onCreated(final Channel newChannel)
				            {
				            	logger.e("Successfully created a channel");
				            	if(newChannel != null) {
				            		final String sid = newChannel.getSid();
				            		ChannelType type = newChannel.getType();
				            		newChannel.setListener(ChannelActivity.this);
				            		logger.e("channel Type is : " + type.toString());
				            		newChannel.join(new StatusListener() {
				            			
				    					@Override
				    					public void onError() {
				    						logger.e("failed to join channel");
				    					}
				    	
				    					@Override
				    					public void onSuccess() {
				    						runOnUiThread(new Runnable() {
						            	        @Override
						            	        public void run() {
						            	        	adapter.notifyDataSetChanged();
						            	        }
						            	    });
				    						logger.d("Successfully joined channel");
				    					}
				    	      			
				    	      		});	     	
				            		
				            		runOnUiThread(new Runnable() {
				            	        @Override
				            	        public void run() {
				            	        	getChannels(sid);
				            	        }
				            	    });
								} 
				            }

				            @Override
				            public void onError()
				            {
				               
				            }
				        }); 

					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		createChannelDialog = builder.create();
		createChannelDialog.show();
	}

	private void setupListView() {
		listView = (ListView) findViewById(R.id.channel_list);
		adapter = new EasyAdapter<Channel>(this, ChannelViewHolder.class, channels,
				new ChannelViewHolder.OnChannelClickListener() {
					@Override
					public void onChannelClicked(final Channel channel) {
						if (channel.getStatus() == Channel.ChannelStatus.JOINED) {
							final Channel channelSelected = channelsObject.getChannel(channel.getSid());
							if(channelSelected!= null) {
								new Handler().postDelayed(new Runnable() {
						            @Override
						            public void run() {
										logger.e("FUCK" + channelSelected.getSid());
						            	Intent i = new Intent(ChannelActivity.this, MessageActivity.class);
										i.putExtra(Constants.EXTRA_CHANNEL, (Parcelable) channelSelected);
										i.putExtra("C_SID", channelSelected.getSid());
										startActivity(i);
						            }
						        }, 0);
								
							}
							return;
						} 
						AlertDialog.Builder builder = new AlertDialog.Builder(ChannelActivity.this);
						builder.setTitle("Select an option").setItems(CHANNEL_OPTIONS,
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								if (which == JOIN) {
									dialog.cancel();
									channel.join(new StatusListener() {
				            			
				    					@Override
				    					public void onError() {
				    						logger.e("failed to join channel");
				    					}
				    	
				    					@Override
				    					public void onSuccess() {
				    						runOnUiThread(new Runnable() {
						            	        @Override
						            	        public void run() {
						            	        	adapter.notifyDataSetChanged();
						            	        }
						            	    });
				    						logger.e("Successfully joined channel");
				    					}
				    	      			
				    	      		});	     	
								} 
							}
						});
						builder.show();
					}
				});
		listView.setAdapter(adapter);
		getChannels(null);
	}
	
	private void getChannels(String channelId) {
		if (this.channels != null) {
			if(basicClient != null && basicClient.getIpMessagingClient() != null) {
			channelsObject= basicClient.getIpMessagingClient().getChannels();
				if(channelsObject != null) {
					channelsObject.loadChannelsWithListener(new StatusListener() {
						@Override
						public void onError() {
							logger.e("Failed to loadChannelsWithListener");
						}
		
						@Override
						public void onSuccess() {
							logger.e("Successfully loadChannelsWithListener.");
							if(channels != null) {
								channels.clear();
							}
							if (channelsObject != null) {
								channelArray = channelsObject.getChannels();
								setupListenersForChannel(channelArray);
								ChannelActivity.this.channels.addAll(new ArrayList<Channel>(Arrays.asList(channelArray)));
								Collections.sort(ChannelActivity.this.channels, new CustomChannelComparator());
								adapter.notifyDataSetChanged();
							}
						}
		      		});	     	
				}
			}
		}
	}

	@Override
	public void onMessageAdd(Message message) {
		if(message != null) {
			logger.d("Received onMessageAdd event");
		}
	}

	@Override
	public void onAttributesChange(Map<String, String> updatedAttributes) {
		if(updatedAttributes != null) {
			logger.d("updatedAttributes event received");
		}
	}

	public void onMessageChange(Message message) {
		if(message != null) {
			logger.d("Received onMessageChange event");
		}		
	}
	
	private void showIncomingInvite(final Channel channel)
    {
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (incoingChannelInvite == null) {
                    incoingChannelInvite = new AlertDialog.Builder(ChannelActivity.this)
                        .setTitle(R.string.incoming_call)
                        .setMessage(R.string.incoming_call_message)
                        .setPositiveButton(R.string.join, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                            	channel.join(new StatusListener() {
			            			
			    					@Override
			    					public void onError() {
			    						logger.d("Failed to join channel");
			    					}
			    					
			    					@Override
			    					public void onSuccess() {
			    						runOnUiThread(new Runnable() {
					            	        @Override
					            	        public void run() {
					            	        	adapter.notifyDataSetChanged();
					            	        }
					            	    });
			    						logger.d("Successfully joined channel");
			    					}
			    	      		});	     	
                                incoingChannelInvite = null;
                            }
                        })
                        .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                               channel.declineInvitation(new StatusListener() {
			            			
			    					@Override
			    					public void onError() {
			    						logger.d("Failed to decline channel invite");
			    					}
			    	
			    					@Override
			    					public void onSuccess() {
			    						logger.e("Successfully to declined channel invite");
			    					}
			    	      			
			    	      		});	     	
                               incoingChannelInvite = null;
                            }
                        })
                        .create();
                    incoingChannelInvite.show();
                }
            }
        });
    }
	
	private class CustomChannelComparator implements Comparator<Channel> {
		@Override
		public int compare(Channel lhs, Channel rhs) {
			return lhs.getFriendlyName().compareTo(rhs.getFriendlyName());		
		}
	}

	@Override
	public void onMessageDelete(Message message) {
		logger.e("Member deleted");
	}

	@Override
	public void onMemberJoin(Member member) {
		if(member != null) {
			logger.d(member.getIdentity() + " joined");
		}
	}

	@Override
	public void onMemberChange(Member member) {
		if(member != null) {
			logger.d(member.getIdentity() + " changed");
		}
	}
	
	@Override
	public void onMemberDelete(Member member) {
		if(member != null) {
			logger.d(member.getIdentity() + " deleted");
		}
		
	}
	
	private void setupListenersForChannel(Channel[] channelArray){
		if(channelArray != null) {
			for(int i=0; i<channelArray.length; i++) {
				channelArray[i].setListener(ChannelActivity.this);
			}
		}
	}
	
	private void showToast(String text) {
		Toast toast= Toast.makeText(getApplicationContext(),text, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
		LinearLayout toastLayout = (LinearLayout) toast.getView();
		TextView toastTV = (TextView) toastLayout.getChildAt(0);
		toastTV.setTextSize(30);
		toast.show(); 
	}
	
	@Override
	public void onTypingStarted(Member member){
		if(member != null) {
			logger.d(member.getIdentity() + " started typing");
		}
	}
	
	@Override
	public void onTypingEnded(Member member) {
		if(member != null) {
			logger.d(member.getIdentity() + " ended typing");
		}
	}
}
