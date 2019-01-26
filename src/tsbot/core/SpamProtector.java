/*
 * TSBOT
 * Licensed under MIT-License
 */
package tsbot.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpamProtector {
	private Map<Integer, String> messages;
	private List<Character> caps = Arrays.asList(new Character[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'});
	
	public SpamProtector() {
		this.messages = new HashMap<>();
	}
	
	public String checkMessage(int clientId, String message, boolean checkSpam, boolean checkCaps) {
		if(checkSpam && isMessageSpam(clientId, message)) {
			return "SPAM";
		}
		
		if(checkCaps && isMessageCaps(message)) {
			return "CAPS";
		}
		
		return null;
	}

	private boolean isMessageSpam(int clientId, String message) {
		if (this.messages.containsKey(clientId)) {
			String previousMessage = this.messages.get(clientId);
			if (previousMessage.equalsIgnoreCase(message)) {
				return true;
			}
		}
		this.messages.put(clientId, message);
		return false;
	}
	
	private boolean isMessageCaps(String message) {
		char[] chars = message.toCharArray();
		int amount = 0;
		
		for(char c : chars) {
			if(this.caps.contains(c)) {
				amount++;
			} else {
				amount = 0;
			}
			
			if(amount > 4) {
				return true;
			}
		}
		
		return false;
	}
}
