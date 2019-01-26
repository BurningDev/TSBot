/*
 * TSBOT
 * Licensed under MIT-License
 */
package tsbot.commands;

import java.util.List;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import tsbot.core.TSBot;

public class CmdReport implements TSCommand {

	@Override
	public void execute(Client sender, String[] arguments, TSBot bot) {
		if(arguments.length >= 2) {
			String targetUser = arguments[1];
			List<Client> clients = bot.getClientsByGroup("Support");
			for(Client client : clients) {
				bot.sendPrivateMessage(client.getId(), sender.getNickname() + " has reported " + targetUser + ".");
			}
			
			bot.sendPrivateMessage(sender.getId(), "You have reported the user " + targetUser + ".");
		} else {
			bot.sendPrivateMessage(sender.getId(), "Usage: #report <user>");
		}
	}

	@Override
	public String getCommand() {
		return "report";
	}

}
