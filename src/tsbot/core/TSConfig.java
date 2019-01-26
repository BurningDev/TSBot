/*
 * TSBOT
 * Licensed under MIT-License
 */
package tsbot.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.pmw.tinylog.Logger;

public class TSConfig {
	private Properties properties;
	private File configFile;

	public TSConfig(File configFile) {
		this.configFile = configFile;
		loadConfiguration();
	}

	public void loadConfiguration() {
		this.properties = new Properties();
		try {
			this.properties.load(new FileReader(this.configFile));
		} catch (IOException e) {
			Logger.error(e, e.getMessage());
		}
	}

	public boolean getBooleanValue(String key) {
		String value = String.valueOf(this.properties.get(key));
		
		if(value.equalsIgnoreCase("true")) {
			return true;
		} else {
			return false;
		}
	}

	public String getStringValue(String key) {
		String value = String.valueOf(this.properties.get(key));
		return value;
	}

	public int getIntValue(String key) {
		int result = 0;
		String value = String.valueOf(this.properties.get(key));
		result = Integer.valueOf(value);
		
		return result;
	}
	
	public String getText(String key) {
		return getStringValue("t_" + key);
	}
}
