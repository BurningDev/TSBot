/*
 * TSBOT
 * Licensed under MIT-License
 */
package tsbot.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import tsbot.core.TSBot;

public class CmdTime implements TSCommand {

	@Override
	public void execute(Client sender, String[] arguments, TSBot bot) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH : mm");
		String time = sdf.format(new Date());
	
		bot.sendPrivateMessage(sender.getId(), time);
	}

	@Override
	public String getCommand() {
		return "time";
	}

}
