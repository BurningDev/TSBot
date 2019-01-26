/*
 * TSBOT
 * Licensed under MIT-License
 */
package tsbot.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;

import tsbot.commands.CmdRegister;
import tsbot.commands.CmdReport;
import tsbot.commands.CmdTime;
import tsbot.commands.TSCommand;

public class TSBot {
	private TS3Api api;

	private TSConfig config;
	private SpamProtector spamProtector;

	private List<TSCommand> commands;

	private Map<Integer, Integer> warnings;
	private Map<Integer, Integer> registration;

	private Thread broadCaster = new Thread(new Runnable() {

		@Override
		public void run() {
			while (broadCaster.isAlive()) {
				int random = (int) (Math.random() * 3) + 1;

				switch (random) {
				case 1:
					api.broadcast(config.getText("broadcast1"));
					break;
				case 2:
					api.broadcast(config.getText("broadcast2"));
					break;
				default:
					api.broadcast(config.getText("broadcast3"));
					break;
				}

				try {
					Thread.sleep(900 * 1000);
				} catch (InterruptedException e) {
					Logger.error(e, e.getMessage());
				}
			}
		}
	});

	public TSBot(TSConfig config) {
		this.config = config;
	}

	public void start() {
		final TS3Config config = new TS3Config();
		config.setHost(this.config.getStringValue("hostname"));
		config.setEnableCommunicationsLogging(this.config.getBooleanValue("debug"));

		final TS3Query query = new TS3Query(config);
		query.connect();

		this.api = query.getApi();
		this.api.login(this.config.getStringValue("nickname"), this.config.getStringValue("password"));
		this.api.selectVirtualServerById(1);
		this.api.setNickname(this.config.getStringValue("bot"));

		this.api.registerAllEvents();
		this.api.addTS3Listeners(new TSListener(this));

		this.commands = new ArrayList<>();
		if (this.config.getBooleanValue("cmd_register")) {
			this.commands.add(new CmdRegister());
		}
		if (this.config.getBooleanValue("cmd_report")) {
			this.commands.add(new CmdReport());
		}
		if (this.config.getBooleanValue("cmd_time")) {
			this.commands.add(new CmdTime());
		}

		this.spamProtector = new SpamProtector();
		this.warnings = new HashMap<>();

		this.registration = new HashMap<>();

		this.broadCaster.start();
	}

	public void onClientJoin(ClientJoinEvent e) {
		if (this.config.getBooleanValue("welcome_message")) {
			this.api.pokeClient(e.getClientId(), config.getText("welcome"));
		}
	}

	public void onClientMoved(ClientMovedEvent e) {
		final ChannelInfo channelInfo = this.api.getChannelInfo(e.getTargetChannelId());
		final ClientInfo clientInfo = this.api.getClientInfo(e.getClientId());

		if (this.config.getBooleanValue("support")
				&& channelInfo.getName().equals(this.config.getStringValue("support_channel"))) {
			final List<Client> clients = this.api.getClients();
			final List<ServerGroup> groups = this.api.getServerGroups();

			ServerGroup supportGroup = null;
			int supporterAmount = 0;

			for (ServerGroup group : groups) {
				if (group.getName().equals(this.config.getStringValue("support_group"))) {
					supportGroup = group;
				}
			}

			if (supportGroup != null) {
				for (Client client : clients) {
					if (client.isInServerGroup(supportGroup)) {
						this.api.sendPrivateMessage(client.getId(), clientInfo.getNickname() + " is waiting for help.");
						supporterAmount++;
					}
				}
			} else {
				Logger.warn("The support-group doesent exists.");
				this.api.sendPrivateMessage(e.getClientId(),
						"There is a problem. The supporter group doesent exists. Please contact an administrator.");
			}

			this.api.sendPrivateMessage(e.getClientId(), "Hello " + clientInfo.getNickname()
					+ ", there were all available support members (" + supporterAmount + ") notified.");

		}
	}

	public void onTextMessage(TextMessageEvent e) {
		if (e.getInvokerId() == 0) {
			return;
		}

		String message = e.getMessage();
		ClientInfo clientInfo = this.api.getClientInfo(e.getInvokerId());
		Client client = this.api.getClientByUId(clientInfo.getUniqueIdentifier());

		if (message == null || message.isEmpty()) {
			return;
		}

		String[] arguments = message.split(" ");
		boolean commandExecuted = false;

		if (message.startsWith("#")) {
			for (TSCommand command : this.commands) {
				if (message.startsWith("#" + command.getCommand())) {
					command.execute(client, arguments, this);
					commandExecuted = true;
				}
			}
		}

		if (!commandExecuted) {
			String spamResult = this.spamProtector.checkMessage(client.getId(), message,
					this.config.getBooleanValue("check_spam"), this.config.getBooleanValue("check_caps"));
			if (spamResult != null) {
				if (this.warnings.containsKey(client.getId())) {
					this.warnings.put(client.getId(), this.warnings.get(client.getId()) + 1);
				} else {
					this.warnings.put(client.getId(), 1);
				}

				if (this.warnings.get(client.getId()) >= 3) {
					this.warnings.put(client.getId(), 0);
					this.api.kickClientFromServer("Reason: " + spamResult, client.getId());
				} else {
					this.api.sendPrivateMessage(client.getId(), spamResult + " is not tolerated. You get a warning.");
				}
			}
		}
	}

	public List<Client> getClientsByGroup(String groupName) {
		final List<Client> clients = this.api.getClients();
		final List<Client> resultList = new ArrayList<>();

		ServerGroup specificGroup = getServerGroupByName(groupName);

		if (specificGroup != null) {
			for (Client client : clients) {
				if (client.isInServerGroup(specificGroup)) {
					resultList.add(client);
				}
			}
		}

		return resultList;
	}

	public Client getClientById(int clientId) {
		final List<Client> clients = this.api.getClients();

		for (Client client : clients) {
			if (client.getId() == clientId) {
				return client;
			}
		}

		return null;
	}

	public ServerGroup getServerGroupByName(String groupName) {
		final List<ServerGroup> groups = this.api.getServerGroups();

		for (ServerGroup group : groups) {
			if (group.getName().equals(groupName)) {
				return group;
			}
		}

		return null;
	}

	public void sendPrivateMessage(int clientId, String message) {
		this.api.sendPrivateMessage(clientId, message);
	}

	public void register(int clientId, String[] arguments) {
		Client client = getClientById(clientId);
		if (!client.isInServerGroup(getServerGroupByName("Gast"))) {
			this.api.sendPrivateMessage(clientId, "You are already registered.");
			return;
		}

		if (this.registration.containsKey(clientId)) {
			if (arguments.length < 2) {
				this.api.sendPrivateMessage(clientId, "Usage: #register <answer>");
				return;
			}

			int rightAnswer = this.registration.get(clientId);
			int userAnswer = 0;

			try {
				userAnswer = Integer.valueOf(arguments[1]);
			} catch (NumberFormatException e) {
				this.api.sendPrivateMessage(clientId, "The input must be a number.");
				return;
			}

			if (rightAnswer == userAnswer) {
				this.api.sendPrivateMessage(clientId, "Successful! You are registered.");

				this.api.removeClientFromServerGroup(getServerGroupByName(this.config.getStringValue("guest_group")),
						client);
				this.api.addClientToServerGroup(
						getServerGroupByName(this.config.getStringValue("registered_group")).getId(),
						client.getDatabaseId());

				this.registration.remove(clientId);
			} else {
				this.api.sendPrivateMessage(clientId,
						"Error! Your answer was wrong. The new task is " + generateRegistrationTask(clientId));
			}
		} else {
			this.api.sendPrivateMessage(clientId, "Solve the task, to become registered: "
					+ generateRegistrationTask(clientId) + ". Write then #register <answer>.");
		}
	}

	private String generateRegistrationTask(int clientId) {
		int number1 = (int) (Math.random() * 10) + 1;
		int number2 = (int) (Math.random() * 10) + 1;

		int result = number1 + number2;

		this.registration.put(clientId, result);

		return (number1 + "+" + number2);
	}
}
