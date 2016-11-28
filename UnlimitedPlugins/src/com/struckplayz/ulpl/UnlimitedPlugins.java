package com.struckplayz.ulpl;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

public class UnlimitedPlugins extends JavaPlugin {

	final File file = new File("plugins/UnlimitedPlugins/");
	
	public void onEnable() {
		ConsoleCommandSender sender = Bukkit.getConsoleSender();
		if (!file.exists()) {
			file.mkdirs();
			sender.sendMessage("[UnlimitedPlugins] Generated files..");
		}
		ArrayList<Plugin> loaded = new ArrayList<Plugin>();
		for (File f : file.listFiles()) {
			Plugin pl = loadPlugin(sender, f);
			loaded.add(pl);
		}
		if (loaded.size() != 0) {
			sender.sendMessage("[UnlimitedPlugins] Listing loaded plugins: ");
			sender.sendMessage("[UnlimitedPlugins] §aGreen §7: Success");
			sender.sendMessage("[UnlimitedPlugins] §cRed §7: Failed");
			sender.sendMessage("[UnlimitedPlugins] ----------------------------------");
			for (Plugin p : loaded) {
				String color = p.isEnabled() ? "§a":"§c";
				sender.sendMessage("[UnlimitedPlugins] " + color + p.getName());
			}
			sender.sendMessage("[UnlimitedPlugins] ----------------------------------");
		} else {
			sender.sendMessage("[UnlimitedPlugins] Found no plugins to load. Place all plugins you want to load");
			sender.sendMessage("[UnlimitedPlugins] in the \"" + file.getPath() + "\" directory.");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final String prefix = "§aUnlimitedPlugins> §7";
		final String help = prefix + "/ulpl <disable/enable> <plugin>";
		final String isntLoaded = prefix + "That plugin doesn't exist.";
		final String isLoaded = prefix + "That plugin already exists.";
		final String cantDisableThis = prefix + "You can't disable this plugin.";
		final String disabled = prefix + "Disabled the plugin, %s";
		final String enabled = prefix + "Enabled the plugin, %s";
		final String doesntExist = prefix + "That plugin doesn't exist.";
		if (command.getName().equalsIgnoreCase("unlimitedplugins") || command.getName().equalsIgnoreCase("ulpl")) {
			if (args.length == 0) {
				sender.sendMessage(help);
			}
			if (args.length == 1) {
				sender.sendMessage(help);
			}
			if (args.length == 2) {
				String name = args[1];
				if (args[0].equalsIgnoreCase("disable")) {
					if (isPlugin(name)) {
						if (!name.equalsIgnoreCase(this.getDescription().getName())) {
							Plugin kill = getPlugin(name);
							Bukkit.getScheduler().cancelTasks(kill);
							Bukkit.getPluginManager().disablePlugin(kill);
							sender.sendMessage(String.format(disabled, name));
						} else {
							sender.sendMessage(cantDisableThis);
						}
					} else {
						sender.sendMessage(isntLoaded);
					}
				} else if (args[0].equalsIgnoreCase("enable")){
					if (!isPlugin(name)) {
						if (isExisting(name)) {
							File f = getPluginFile(name);
							loadPlugin(sender, f);
							sender.sendMessage(String.format(enabled, name));
						} else {
							sender.sendMessage(doesntExist);
						}
					} else {
						sender.sendMessage(isLoaded);
					}
				} else {
					sender.sendMessage(help);
				}
			}
			if (args.length >= 3) {
				sender.sendMessage(help);
			}
		}
		return true;
	}
	
	private Plugin loadPlugin(CommandSender sender, File f) {
		Plugin pl = null;
		try {
			pl = Bukkit.getPluginManager().loadPlugin(f);
		} catch (UnknownDependencyException | InvalidPluginException | InvalidDescriptionException e) {
			if (e instanceof UnknownDependencyException) {
				sender.sendMessage("[UnlimitedPlugins] Tried to load the jar, " + f.getName() + "but failed because");
				sender.sendMessage("[UnlimitedPlugins] the required dependencies wheren't found.");
			} else if (e instanceof InvalidPluginException) {
				sender.sendMessage("[UnlimitedPlugins] Tried to load the jar, " + f.getName() + "but failed because");
				sender.sendMessage("[UnlimitedPlugins] the jar was invalid.");
			} else if (e instanceof InvalidDescriptionException) {
				sender.sendMessage("[UnlimitedPlugins] Tried to load the jar, " + f.getName() + "but failed because");
				sender.sendMessage("[UnlimitedPlugins] the plugin.yml was invalid.");
			} else {
				sender.sendMessage("[UnlimitedPlugins] Tried to load the jar, " + f.getName() + "but failed because");
				sender.sendMessage("[UnlimitedPlugins] an unknown e occurred.");
			}
		}
		Bukkit.getPluginManager().enablePlugin(pl);
		return pl;
	}
	
	private boolean isPlugin(String name) {
		return Bukkit.getPluginManager().getPlugin(name) != null;
	}
	
	private Plugin getPlugin(String name) {
		return Bukkit.getPluginManager().getPlugin(name);
	}
	
	private boolean isExisting(String name) {
		for (File f : file.listFiles()) {
			if (f.getName().replaceAll(".jar", "").equals(name)) {
				return true;
			}
		}
		for (File f : new File("plugins/").listFiles()) {
			if (f.getName().replaceAll(".jar", "").equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	private File getPluginFile(String name) {
		for (File f : file.listFiles()) {
			if (f.getName().replaceAll(".jar", "").equals(name)) {
				return f;
			}
		}
		for (File f : new File("plugins/").listFiles()) {
			if (f.getName().replaceAll(".jar", "").equals(name)) {
				return f;
			}
		}
		return null;
	}
	
}
