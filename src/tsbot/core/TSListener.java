/*
 * TSBOT
 * Licensed under MIT-License
 */
package tsbot.core;

import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;

public class TSListener extends TS3EventAdapter {

	private TSBot bot;
	
	public TSListener(TSBot bot) {
		this.bot = bot;
	}
	
	@Override
	public void onTextMessage(TextMessageEvent e) {
		this.bot.onTextMessage(e);
	}

	@Override
	public void onClientJoin(ClientJoinEvent e) {
		this.bot.onClientJoin(e);
	}

	@Override
	public void onClientMoved(ClientMovedEvent e) {
		this.bot.onClientMoved(e);
	}
}
