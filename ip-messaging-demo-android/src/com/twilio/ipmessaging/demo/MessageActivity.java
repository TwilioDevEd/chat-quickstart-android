package com.twilio.ipmessaging.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.twilio.example.R;
import com.twilio.ipmessaging.Channel;
import com.twilio.ipmessaging.Channel.ChannelType;
import com.twilio.ipmessaging.Constants.StatusListener;
import com.twilio.ipmessaging.ChannelListener;
import com.twilio.ipmessaging.Channels;
import com.twilio.ipmessaging.Member;
import com.twilio.ipmessaging.Members;
import com.twilio.ipmessaging.Message;
import com.twilio.ipmessaging.Messages;
import com.twilio.ipmessaging.impl.Logger;
import com.twilio.ipmessaging.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import uk.co.ribot.easyadapter.EasyAdapter;

public class MessageActivity extends Activity implements ChannelListener{

	private static final Logger logger = Logger.getLogger(MessageActivity.class);
	private ListView messageListView;
	private EditText inputText;
	private EasyAdapter<Message> adapter;
	
	private List<Message> messages =  new ArrayList<Message>();
	private List<Member> members =  new ArrayList<Member>();
	private Channel channel;
	private static final String[] EDIT_OPTIONS = {"Change Friendly Name", "Change Topic", "List Members", "Invite Member", "Add Member", "Remove Member", "Leave", "Change ChannelType", "destroy"};
	
	private static final int NAME_CHANGE = 0;
	private static final int TOPIC_CHANGE = 1;
	private static final int LIST_MEMBERS = 2;
	private static final int INVITE_MEMBER = 3;
	private static final int ADD_MEMBER = 4;
	private static final int REMOVE_MEMBER = 5;
	private static final int LEAVE = 6;
	private static final int CHANNEL_TYPE = 7;
	private static final int CHANNEL_DESTROY = 8;
	
	private AlertDialog editTextDialog;
	private AlertDialog memberListDialog;
    private AlertDialog changeChannelTypeDialog;
    private Toast typingToast;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createUI();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if (intent != null) {
			Channel channel = intent.getParcelableExtra(Constants.EXTRA_CHANNEL);
			if(channel != null) {
				setupListView(channel);
			}
		}
	}
	
	private void createUI() {
		setContentView(R.layout.activity_message);
		if(getIntent() != null) {
			BasicIPMessagingClient basicClient = TwilioApplication.get().getBasicClient();
			String channelSid = getIntent().getStringExtra("C_SID");
			Channels channelsObject = basicClient.getIpMessagingClient().getChannels();
			if(channelsObject != null) {
				channel = channelsObject.getChannel(channelSid);
				if(channel != null) {
					channel.setListener(MessageActivity.this);
					this.setTitle("Name:"+channel.getFriendlyName() + " Type:" + ((channel.getType()==ChannelType.CHANNEL_TYPE_PUBLIC)? "Public":"Private"));
				}
			}
		}
	
		setupListView(channel);	
		messageListView = (ListView) findViewById(R.id.message_list_view);
		if(messageListView != null) {
			messageListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			messageListView.setStackFromBottom(true);
			adapter.registerDataSetObserver(new DataSetObserver() {
				@Override
				public void onChanged() {
					super.onChanged();
					messageListView.setSelection(adapter.getCount() - 1);
				}
			});
		}
		setupInput();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.message, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			showChannelSettingsDialog();
			break;
		}
		return super.onOptionsItemSelected(item);

	}
	
	private void showChannelSettingsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
		//final AlertDialog alertDialog = builder.show();
		builder.setTitle("Select an option").setItems(EDIT_OPTIONS,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == NAME_CHANGE) {
					showChangeNameDialog();
				} else if (which == TOPIC_CHANGE) {
					showChangeTopicDialog();
				} else if (which == LIST_MEMBERS) {
					Members membersObject = channel.getMembers();
					Member[] members = membersObject.getMembers();
					
					logger.d("member retrieved");
					StringBuffer name = new StringBuffer();
					for(int i= 0; i< members.length; i++) {
						name.append(members[i].getIdentity());
						if(i+1 <members.length) {
							name.append(" ,");
						}
					} 
					Toast toast= Toast.makeText(getApplicationContext(),name, Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
					LinearLayout toastLayout = (LinearLayout) toast.getView();
					TextView toastTV = (TextView) toastLayout.getChildAt(0);
					toastTV.setTextSize(30);
					toast.show(); 
				} else if (which == INVITE_MEMBER) {			
					showInviteMemberDialog();
				} else if(which == ADD_MEMBER) {
					showAddMemberDialog();
				} else if (which == LEAVE) {
					channel.leave(new StatusListener() {
            			
    					@Override
    					public void onError() {
    						logger.e("Error leaving channel");
    					}
    	
    					@Override
    					public void onSuccess() {
    						logger.e("Successful at leaving channel");
    						finish();
    					}
    	      		});	     	
					
				} else if (which == REMOVE_MEMBER) {
					showRemoveMemberDialog();
				} else if (which == CHANNEL_TYPE) {
					showChangeChannelType();
				}   else if (which == CHANNEL_DESTROY) {
					channel.destroy(new StatusListener() {
            			
    					@Override
    					public void onError() {
    						logger.e("Error destroying channel");
    					}
    	
    					@Override
    					public void onSuccess() {
    						logger.e("Successful at destroying channel");
    						finish();
    					}
    	      		});	     	
				}  
			}
		});
		
		builder.show();
	}
	
	private void showChangeNameDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
		// Get the layout inflater
		LayoutInflater inflater = getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(inflater.inflate(R.layout.dialog_edit_friendly_name, null))
				.setPositiveButton("Update", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						String friendlyName = ((EditText) editTextDialog.findViewById(R.id.update_friendly_name)).getText()
								.toString();
						logger.e(friendlyName);
						channel.setFriendlyName(friendlyName, new StatusListener() {
	            			
	    					@Override
	    					public void onError() {
	    						logger.e("Error changing name");
	    					}
	    	
	    					@Override
	    					public void onSuccess() {
	    						logger.e("successfully changed name");
	    					}
	    	      		});	     	
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		editTextDialog = builder.create();
		editTextDialog.show();
	}
	
	private void showChangeTopicDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
		// Get the layout inflater
		LayoutInflater inflater = getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(inflater.inflate(R.layout.dialog_edit_channel_topic, null))
				.setPositiveButton("Update", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						String topic = ((EditText) editTextDialog.findViewById(R.id.update_topic)).getText()
								.toString();
						logger.e(topic);
						Map<String,String> attrMap = new HashMap<String, String>();
						attrMap.put("Topic", topic);
						channel.setAttributes(attrMap, new StatusListener() {
	            			
	    					@Override
	    					public void onError() {
	    						logger.e("Error at channel setAttributes");
	    					}
	    	
	    					@Override
	    					public void onSuccess() {
	    						logger.e("Success at channel setAttributes");
	    					}
	    	      		});	     	
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		editTextDialog = builder.create();
		editTextDialog.show();
	}
	
	private void showInviteMemberDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
		// Get the layout inflater
		LayoutInflater inflater = getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(inflater.inflate(R.layout.dialog_invite_member, null))
				.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						String memberName = ((EditText) editTextDialog.findViewById(R.id.invite_member)).getText()
								.toString();
						logger.e(memberName);
						
						Members membersObject = channel.getMembers();
						membersObject.inviteByIdentity(memberName, new StatusListener() {
	            			
	    					@Override
	    					public void onError() {
	    						logger.e("Error inviteByIdentity");
	    					}
	    	
	    					@Override
	    					public void onSuccess() {
	    						logger.e("Successful at inviteByidentityl");
	    					}
	    	      		});	     	
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		editTextDialog = builder.create();
		editTextDialog.show();
	}
	
	private void showAddMemberDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
		// Get the layout inflater
		LayoutInflater inflater = getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(inflater.inflate(R.layout.dialog_add_member, null))
				.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						String memberName = ((EditText) editTextDialog.findViewById(R.id.add_member)).getText()
								.toString();
						logger.e(memberName);
						
						Members membersObject = channel.getMembers();
						membersObject.addByIdentity(memberName, new StatusListener() {
	            			
	    					@Override
	    					public void onError() {
	    						logger.e("Error addByIdentity");
	    					}
	    	
	    					@Override
	    					public void onSuccess() {
	    						logger.e("Successful at addByIdentity");
	    					}
	    	      		});	     	
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		editTextDialog = builder.create();
		editTextDialog.show();
	}
	
	private void showRemoveMemberDialog() {
		final Members membersObject = channel.getMembers();
		Member[] membersArray= membersObject.getMembers();
		if(membersArray.length > 0 ) {
			members = new ArrayList<Member>(Arrays.asList(membersArray));
		}
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(MessageActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.member_list, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("List");
        ListView lv = (ListView) convertView.findViewById(R.id.listView1);
		EasyAdapter<Member> adapterMember = new EasyAdapter<Member>(this, MemberViewHolder.class, members,
				new MemberViewHolder.OnMemberClickListener() {
					@Override
					public void onMemberClicked(Member member) {
						membersObject.removeMember(member, new StatusListener() {
	            			
	    					@Override
	    					public void onError() {
	    						logger.e("Error at removeMember operation");
	    					}
	    	
	    					@Override
	    					public void onSuccess() {
	    						logger.e("Successful at removeMember operation");
	    					}
	    	      		});	     	
						memberListDialog.dismiss();
					}
				});
		lv.setAdapter(adapterMember);
		memberListDialog = alertDialog.create();
		memberListDialog.show();
		memberListDialog.getWindow().setLayout(800, 600); 
	}
	
	private void showChangeChannelType() {

		// Strings to Show In Dialog with Radio Buttons
		final CharSequence[] items = { " Public ", " Private " };
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
		builder.setTitle("Select The Channel Type");
		builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0:
					// PUBLIC
					logger.e("Setting channel type to public");
					channel.setType(ChannelType.CHANNEL_TYPE_PUBLIC, new StatusListener() {
            			
    					@Override
    					public void onError() {
    						logger.e("Error public channel Type update");
    					}
    	
    					@Override
    					public void onSuccess() {
    						logger.e("Successfull at public channel Type update");
    					}
    	      		});	     	
					break;
				case 1:
					// PRIVATE
					logger.e("Setting channel type to private");
					channel.setType(ChannelType.CHANNEL_TYPE_PRIVATE, new StatusListener() {
            			
    					@Override
    					public void onError() {
    						logger.e("Failed at private channel Type update");
    					}
    	
    					@Override
    					public void onSuccess() {
    						logger.e("Successfull at private channel Type update");
    					}
    	      		});	     	
					break;
				}
				changeChannelTypeDialog.dismiss();
			}
		});
		changeChannelTypeDialog = builder.create();
		changeChannelTypeDialog.show();
	}
	

	private void setupInput() {
		// Setup our input methods. Enter key on the keyboard or pushing the
		// send
		// button
		EditText inputText = (EditText) findViewById(R.id.messageInput);
		inputText.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if(channel != null ) {
					channel.typing();
				}
			}
		   
		}); 
		inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
				if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					sendMessage();
				}
				return true;
			}
		});

		findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendMessage();
			}
		});
	}

	
	private class CustomMessageComparator implements Comparator<Message> {
		@Override
		public int compare(Message lhs, Message rhs) {
			return lhs.getTimeStamp().compareTo(rhs.getTimeStamp());		
		}
	}

	private void setupListView(Channel channel) {
		messageListView = (ListView) findViewById(R.id.message_list_view);
		Messages messagesObject = channel.getMessages();
		if(messagesObject != null) {
			Message[] messagesArray = messagesObject.getMessages();
			if(messagesArray.length > 0 ) {
				messages = new ArrayList<Message>(Arrays.asList(messagesArray));
				Collections.sort(messages, new CustomMessageComparator());
			}
			adapter = new EasyAdapter<Message>(this, MessageViewHolder.class, messages,
					new MessageViewHolder.OnMessageClickListener() {
						@Override
						public void onMessageClicked(final Channel channel) {
							
						}
					});
			messageListView.setAdapter(adapter);
			adapter.notifyDataSetChanged(); 
		}
	}


	private void sendMessage() {
		inputText = (EditText) findViewById(R.id.messageInput);
		String input = inputText.getText().toString();
		if (!input.equals("")) {
			
			Messages messagesObject = this.channel.getMessages();
			Message message = messagesObject.createMessage(input);
			messagesObject.sendMessage(message, new StatusListener() {
    			
				@Override
				public void onError() {
					logger.e("Error sending message.");
				}

				@Override
				public void onSuccess() {
					logger.e("Successful at sending message.");
				}
      		});	     	
			messages.add(message);
			adapter.notifyDataSetChanged();
			inputText.setText("");
		}
		inputText.requestFocus();
	}

	@Override
	public void onMessageAdd(Message message) {	
		setupListView(this.channel);
	}

	@Override
	public void onMessageChange(Message message) {
		if(message != null) {
			logger.d("Received onMessageChange "  + message.getSid());
		} else {
			logger.d("Received onMessageChange ");
		}
	}

	@Override
	public void onMessageDelete(Message message) {
		if(message != null) {
			logger.d("Received onMessageDelete "  + message.getSid());
		} else {
			logger.d("Received onMessageDelete.");
		}
	}

	@Override
	public void onMemberJoin(Member member) {
		if(member != null) {
			showToast(member.getIdentity() + " joined");
		}
	}

	@Override
	public void onMemberChange(Member member) {
		if(member != null) {
			showToast(member.getIdentity() + " changed");
		}
	}
	
	@Override
	public void onMemberDelete(Member member) {
		if(member != null) {
			showToast(member.getIdentity() + " deleted");
		}
	}
	
	@Override
	public void onAttributesChange(Map<String, String> updatedAttributes) {
	
		if(updatedAttributes != null) {
			logger.d("Received onAttributesChange event" + updatedAttributes.toString());
		} else {
			logger.d("Received onAttributesChange event");
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
			TextView typingIndc = (TextView) findViewById(R.id.typingIndicator);
			String text = member.getIdentity() + " started typing .....";
			typingIndc.setText(text);
			typingIndc.setTextColor(Color.RED);
			logger.d(text);
		}
	}
	
	@Override
	public void onTypingEnded(Member member) {
		if(member != null) {
			TextView typingIndc = (TextView) findViewById(R.id.typingIndicator);
			typingIndc.setText(null);
			logger.d(member.getIdentity() + " ended typing");
		}
	}
}
