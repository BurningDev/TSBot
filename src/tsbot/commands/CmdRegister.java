/*
 * TSBOT
 * Licensed under MIT-License
 */
package tsbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import tsbot.core.TSBot;

public class CmdRegister implements TSCommand {

	@Override
	public void execute(Client sender, String[] arguments, TSBot bot) {
		bot.register(sender.getId(), arguments);
	}
	
	public String getCommand() {
		return "register";
	}
}
