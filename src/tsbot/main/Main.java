/*
 * TSBOT
 * Licensed under MIT-License
 */
package tsbot.main;

import java.io.File;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.FileWriter;

import tsbot.core.TSBot;
import tsbot.core.TSConfig;

public class Main {
	public static void main(String[] args) {
		File configFile = new File("config.properties");
		Configurator.defaultConfig().formatPattern("{date:yyyy-MM-dd HH:mm:ss} [{level}]: {message}").activate();
		
		if (!configFile.exists() || !configFile.canRead()) {
			Logger.error("The configuration file doesent exists or cannot be read.");
		} else {
			Logger.info("Bot starting...");
			Logger.info("Bot by BurningDev. Licensed under MIT-License.");
			Configurator.defaultConfig().formatPattern("{date:yyyy-MM-dd HH:mm:ss} [{level}]: {message}").addWriter(new FileWriter("log.txt", false, true)).activate();
			
			TSConfig tsConfig = new TSConfig(configFile);

			TSBot bot = new TSBot(tsConfig);
			bot.start();
		}
	}
}
