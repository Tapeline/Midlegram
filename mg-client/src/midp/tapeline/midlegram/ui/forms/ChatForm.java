package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import midp.tapeline.midlegram.Midlegram;
import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.StringUtils;
import midp.tapeline.midlegram.client.data.AttachedMedia;
import midp.tapeline.midlegram.client.data.Chat;
import midp.tapeline.midlegram.client.data.Media;
import midp.tapeline.midlegram.client.data.Message;
import midp.tapeline.midlegram.ui.FileBrowser;
import midp.tapeline.midlegram.ui.FileBrowser.FileSelectListener;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;
import midp.tapeline.midlegram.ui.components.ChatItem;
import midp.tapeline.midlegram.ui.components.LoadingItem;
import midp.tapeline.midlegram.ui.components.MessageItem;
import midp.tapeline.midlegram.ui.components.QueuedMediaItem;

public class ChatForm extends UIForm {

	Vector messages;
	LoadingItem loading;
	Chat chat;
	Command prev = new Command("Prev", Command.SCREEN, 1);
	Command next = new Command("Next", Command.SCREEN, 1);
	Command reload = new Command("Reload", Command.SCREEN, 1);
	Command send = new Command("Send", Command.SCREEN, 1);
	Command noReply = new Command("Do not reply", Command.SCREEN, 1);
	Command attachVoice = new Command("Attach voice", Command.SCREEN, 1);
	Command attachPhoto = new Command("Attach photo", Command.SCREEN, 1);
	StringItem prevButton = new StringItem("", "Earlier", Item.BUTTON);
	StringItem nextButton = new StringItem("", "Later", Item.BUTTON);
	TextField msgInput = new TextField("New message", "", 1000, TextField.ANY);
	Vector anchors = new Vector();
	int currentAnchor = 0;
	Message replyTo = null;
	Vector mediaToSend = new Vector();
	int lastMessageItem = 0;
	ChatMessagePoller poller;
	
	public ChatForm(Chat chat) {
		super(chat.title);
		this.chat = chat;
		prevButton.setLayout(Item.LAYOUT_EXPAND);
		nextButton.setLayout(Item.LAYOUT_EXPAND);
		prevButton.setDefaultCommand(prev);
		nextButton.setDefaultCommand(next);
		prevButton.setItemCommandListener(this);
		nextButton.setItemCommandListener(this);
		msgInput.setLayout(Item.LAYOUT_EXPAND);
		addCommand(send);
		addCommand(noReply);
		addCommand(reload);
		addCommand(attachVoice);
		addCommand(attachPhoto);
		addBackButton();
		anchors.addElement(new Long(0));
		poller = new ChatMessagePoller(this);
	}
	
	private void reloadMessages() {
		deleteAll();
		setLoading(true);
		try {
			Long fromId = (Long) anchors.elementAt(currentAnchor);
			messages = Services.tg.getMessages(new Long(chat.id), fromId.longValue());
			append(prevButton);
			lastMessageItem = 2;
			for (int i = messages.size() - 1; i >= 0; --i) 
				insertNewMessage((Message) messages.elementAt(i));
			if (currentAnchor != 0) append(nextButton);
			append(msgInput); 
			for (int i = 0; i < mediaToSend.size(); ++i) 
				append(new QueuedMediaItem((AttachedMedia) mediaToSend.elementAt(i), this));
		} catch (IOException exc) {
			UI.alertFatal(exc);
		} finally {
			setLoading(false);
			lastMessageItem--;  // nasty hack -- loading item is also an item :/
		}
	}
	
	public void setReplyTo(Message msg) {
		replyTo = msg;
		if (replyTo == null) {
			msgInput.setLabel("New message");
		} else {
			msgInput.setLabel("Reply to \"" + StringUtils.trunc(msg.text, 16) + "\"");
		}
	}
	
	public void onStart() {
		reloadMessages();
		poller.start();
	}
	
	public void onEnd() {
		poller.stop();
	}

	protected void onCommand(Command cmd) {
		if (cmd == prev) {
			anchors.addElement(new Long(((Message) messages.elementAt(messages.size() - 1)).id));
			currentAnchor++;
			new Thread(new Runnable() {
				public void run() {
					poller.stop();
					reloadMessages();
					poller.start();
				}
			}).start(); 
		} else if (cmd == next) {
			anchors.removeElementAt(anchors.size() - 1);
			currentAnchor--;
			new Thread(new Runnable() {
				public void run() {
					poller.stop();
					reloadMessages();
					poller.start();
				}
			}).start(); 
		} else if (cmd == reload) {
			new Thread(new Runnable() {
				public void run() {
					poller.stop();
					reloadMessages();
					poller.start();
				}
			}).start(); 
		} else if (cmd == send) {
			setLoading(true);
			new Thread(new Runnable() {
				public void run() {
					poller.stop();
					if (msgInput.getString().length() != 0) {
						try {
							Services.tg.sendTextMessage(chat.id, replyTo == null? 0 : replyTo.id, msgInput.getString());
							/*insert(size() - 1, new MessageItem(
									new Message(
											0L, (byte) 0, 
											(int) (Calendar.getInstance().getTime().getTime() / 1000), 
											0, msgInput.getString(), "Me", "?", new Vector(), 
											replyTo == null? null : new Long(replyTo.id)
									), ChatForm.this)
							);*/
							msgInput.setString("");
						} catch (IOException e) {
							UI.alertFatal(e);
						}
					}
					for (int i = 0; i < mediaToSend.size(); ++i) {
						AttachedMedia media = (AttachedMedia) mediaToSend.elementAt(i);
						try {
							if (media.file != null)
								Services.tg.sendFileMessage(chat.id, replyTo == null? 0 : replyTo.id, media.type, media.file);
							else
								Services.tg.sendFileMessage(chat.id, replyTo == null? 0 : replyTo.id, media.type, media.data);
							/*insert(size() - 1, new MessageItem(
									new Message(
											0L, (byte) 0, 
											(int) (Calendar.getInstance().getTime().getTime() / 1000), 
											0, "(sent media " + media.type + ")" , "Me", "?", new Vector(), 
											replyTo == null? null : new Long(replyTo.id)
									), ChatForm.this)
							);*/
						} catch (IOException e) {
							UI.alertFatal(e);
						}
					}
					mediaToSend.removeAllElements();
					ChatForm.this.setLoading(false);
					ChatForm.this.reloadMessages();
					poller.start();
				}
			}).start();
		} else if (cmd == noReply) {
			setReplyTo(null);
			Display.getDisplay(Midlegram.instance).setCurrentItem(msgInput);
		} else if (cmd == attachVoice) {
			UI.startForm(new RecordVoiceForm(this));
		} else if (cmd == attachPhoto) {
			 FileBrowser browser = new FileBrowser(Display.getDisplay(Midlegram.instance), new FileSelectListener() {
		        public void onFileSelected(final String url, String filename) {
		            new Thread(new Runnable() {
		                public void run() {
		                    addMediaToSend(new AttachedMedia("photo", url));
		                }
		            }).start();
		            Display.getDisplay(Midlegram.instance).setCurrent(ChatForm.this);
		        }

		        public void onBrowserCancelled() {
		        	Display.getDisplay(Midlegram.instance).setCurrent(ChatForm.this);
		        }
		    });
		    Display.getDisplay(Midlegram.instance).setCurrent(browser);
		    browser.init();
		}
	}
	
	public void goToMsg(final long msgId) {
		if (anchors.size() == 1) {
			anchors.addElement(new Long(msgId));
			currentAnchor = 1;
		} else for (int i = 1; i < anchors.size(); ++i)
			if (((Long) anchors.elementAt(i)).longValue() < msgId) {
				System.out.print("Going from " + anchors.elementAt(currentAnchor) + " to " + msgId + "=");
				anchors.insertElementAt(new Long(msgId), i);
				currentAnchor = i;
				System.out.println("" + anchors.elementAt(currentAnchor));
			}
		new Thread(new Runnable() {
			public void run() {
				reloadMessages();
				for (int i = 0; i < messages.size(); ++i) {
					if (((Message) messages.elementAt(i)).id == msgId) {
						((MessageItem) get(i + 1)).setLabel(((MessageItem) get(i + 1)).getLabel() + " (preceding)");
						Display.getDisplay(Midlegram.instance).setCurrentItem(get(i + 1));
						break;
					}
				}
			}
		}).start(); 
	}
	
	public void addMediaToSend(AttachedMedia media) {
		mediaToSend.addElement(media);
		append(new QueuedMediaItem(media, this));
	}
	
	public void removeMediaToSend(AttachedMedia media) {
		mediaToSend.removeElement(media);
		for (int i = 0; i < size(); ++i) {
			if (get(i) instanceof QueuedMediaItem && ((QueuedMediaItem) get(i)).media == media) {
				delete(i);
				break;
			}
		}
	}
	
	void insertNewMessage(Message message) {
		insert(lastMessageItem, new MessageItem(message, this));
		lastMessageItem++;
	}
	
	static class ChatMessagePoller implements Runnable {
		
		ChatForm form;
		Thread th;
		boolean running = false;
		
		ChatMessagePoller(ChatForm form) {
			this.form = form;
		}
		
		void start() {
			th = new Thread(this);
			running = true;
			th.start();
		}
		
		void stop() {
			if (!running) return;
			running = false;
			try {
				Services.tg.interruptPolling();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			while (running) {
	            try {
	            	Vector updates = Services.tg.pollUpdates(form.chat.id);
	            	for (int i = 0; i < updates.size(); i++)
	            		form.insertNewMessage((Message) updates.elementAt(i));
	            } catch (Exception e) {
	                e.printStackTrace();
	            	// wait before retry
	                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
	            }
	        }
		}
		
	}

}
