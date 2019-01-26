/*
 * TSBOT
 * Licensed under MIT-License
 */
package tsbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import tsbot.core.TSBot;

public interface TSCommand {
	public void execute(Client sender, String[] arguments, TSBot bot);
	
	public String getCommand();
}
