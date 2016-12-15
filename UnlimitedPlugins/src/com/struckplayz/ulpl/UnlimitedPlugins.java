package com.struckplayz.ulpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonObject;
import com.struckplayz.ulpl.inventory.SpigetWebhook;

public class UnlimitedPlugins extends JavaPlugin implements Listener {

	private static UnlimitedPlugins instance;

	final File file = new File("plugins/UnlimitedPlugins/");
	final File loadbefore = new File("plugins/UnlimitedPlugins/loadbefore.yml");

	public void onEnable() {
		metrics();
		String u = checkUpdate();
		if (u != null) {
			if (!u.equals(this.getDescription().getVersion().toString())) {
				getLogger().info("Your version of UnlimitedPlugins seems to be outdated.");
				getLogger().info("Please download the latest version (v" + u + ") from https://www.spigotmc.org/resources/unlimitedplugins.32431/");
			} else {
				getLogger().info("Your version of UnlimitedPlugins is up-to-date!");
			}
		}
		
		instance = this;

		ConsoleCommandSender sender = Bukkit.getConsoleSender();
		FileConfiguration loadcf = new YamlConfiguration();
		if (!file.exists() || !loadbefore.exists()) {
			try {
				file.mkdirs();
				loadbefore.createNewFile();
				loadcf = new YamlConfiguration();
				loadcf.options().header("DO NOT DELETE THIS FILE!\n" + "This file is made for the plugin load order. \n"
						+ "If you need certain plugins to load before others, put their names \n"
						+ "on this list in the order you would like them to load.\n"
						+ "If you need help with this file, contact me at: https://www.spigotmc.org/conversations/add?to=Struck713");
				loadcf.set("load-order", new ArrayList<String>());
				loadcf.save(loadbefore);
			} catch (IOException e) {
				sender.sendMessage("[UnlimitedPlugins] Failed to generate files.");
			}
			sender.sendMessage("[UnlimitedPlugins] Generated files!");
		}
		try {
			loadcf.load(loadbefore);
		} catch (IOException | InvalidConfigurationException e) {
			sender.sendMessage("[UnlimitedPlugins] Failed to load loadbefore.yml");
			return;
		}
		ArrayList<Plugin> loaded = new ArrayList<Plugin>();
		sender.sendMessage("[UnlimitedPlugins] Starting load process.");
		sender.sendMessage("[UnlimitedPlugins] Consulting loadbefore.yml.");
		List<String> before = loadcf.getStringList("load-order");
		if (!before.isEmpty()) {
			for (String s : before) {
				File f = getPluginFile(s);
				if (f != null) {
					Plugin pl = loadPlugin(sender, f, loaded);
					if (pl != null) {
						loaded.add(pl);
					}
				} else {
					sender.sendMessage("[UnlimitedPlugins] Unknown plugin, " + s + ", in loadbefore.yml.");
				}
			}
		} else {
			sender.sendMessage("[UnlimitedPlugins] loadbefore.yml is empty!");
		}
		sender.sendMessage("[UnlimitedPlugins] Scanning files to see if anything is left to load.");
		for (File f : file.listFiles()) {
			String name = f.getName();
			if (name.endsWith(".jar")) {
				Plugin pl = loadPlugin(sender, f, loaded);
				if (pl != null && !loaded.contains(pl)) {
					loaded.add(pl);
				}
			}
		}
		if (loaded.size() != 0) {
			sender.sendMessage("[UnlimitedPlugins] Listing loaded plugins: ");
			sender.sendMessage("[UnlimitedPlugins] ----------------------------------");
			for (Plugin p : loaded) {
				sender.sendMessage("[UnlimitedPlugins] " + p.getName());
			}
			sender.sendMessage("[UnlimitedPlugins] ----------------------------------");
		} else {
			sender.sendMessage(
					"[UnlimitedPlugins] Found no plugins to load. Place all plugins you want to load in the \""
							+ file.getPath() + "\" directory.");
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
	}

	public void onDisable() {
		instance = null;
	}

	
	final String prefix = "§aUnlimitedPlugins> §7";
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final String help = prefix + "/ulpl <disable/enable/search/installer> <plugin>";
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
				if (args[0].equalsIgnoreCase("installer")) {
					try {
						ArrayList<JsonObject> objs = SpigetWebhook.getRecents();
						Inventory i = getInventory("Spigot Resources - Recents", objs);
						if (sender instanceof Player) {
							Player player = (Player)sender;
							player.openInventory(i);
						}
					} catch (IOException e1) {
						sender.sendMessage(prefix + "Failed to create inventory. :-(");
					}
					return true;
				}
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
				} else if (args[0].equalsIgnoreCase("enable")) {
					if (!isPlugin(name)) {
						if (isExisting(name)) {
							File f = getPluginFile(name);
							loadPlugin(sender, f, new ArrayList<Plugin>());
							sender.sendMessage(String.format(enabled, name));
						} else {
							sender.sendMessage(doesntExist);
						}
					} else {
						sender.sendMessage(isLoaded);
					}
				} else if (args[0].equalsIgnoreCase("search")) {
					try {
						ArrayList<JsonObject> objs = SpigetWebhook.search(args[1], 20);
						Inventory i = getInventory("Spigot Resources - Search: " + args[1], objs);
						if (sender instanceof Player) {
							Player player = (Player)sender;
							player.openInventory(i);
						}
					} catch (IOException e) {
						sender.sendMessage(prefix + "Failed to search for plugin. :-(");
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

	public Plugin loadPlugin(CommandSender sender, File f, ArrayList<Plugin> loaded) {
		Plugin pl = null;
		for (Plugin pll : loaded) {
			if (pll.getName().equals(f.getName().replaceAll(".jar", ""))) {
				return null;
			}
		}
		try {
			pl = Bukkit.getPluginManager().loadPlugin(f);
		} catch (UnknownDependencyException | InvalidPluginException | InvalidDescriptionException e) {
			if (e instanceof UnknownDependencyException) {
				sender.sendMessage("[UnlimitedPlugins] Tried to load the jar, " + f.getName()
						+ " but failed because the required dependencies wheren't found.");
			} else if (e instanceof InvalidPluginException) {
				sender.sendMessage("[UnlimitedPlugins] Tried to load the jar, " + f.getName()
						+ " but failed because the jar was invalid.");
			} else if (e instanceof InvalidDescriptionException) {
				sender.sendMessage("[UnlimitedPlugins] Tried to load the jar, " + f.getName()
						+ " but failed because the plugin.yml was invalid.");
			} else {
				sender.sendMessage("[UnlimitedPlugins] Tried to load the jar, " + f.getName()
						+ " but failed because an unknown error occurred.");
			}
			return null;
		}
		if (pl != null) {
			Bukkit.getPluginManager().enablePlugin(pl);
		}
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
	
	private Inventory getInventory(String nam, ArrayList<JsonObject> objs) {
		Inventory i = Bukkit.createInventory(null, 27, nam);

		for (JsonObject obj : objs) {
			String name = obj.get("name").getAsString();
			String tag = obj.get("tag").getAsString();
			int id = obj.get("id").getAsInt();
			ItemStack is = new ItemStack(Material.NAME_TAG);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName("§7" + name);
			im.setLore(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{

					add("§7Tag: §a" + tag);
					add("§7ID: §a" + id);
					add(" ");
					add("§aClick to download!");

				}
			});
			is.setItemMeta(im);
			i.addItem(is);
		}
		
		return i;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		Player player = (Player)event.getWhoClicked();
		if (event.getClickedInventory() == null) {
			return;
		}
		Inventory i = event.getClickedInventory();
		if (event.getCurrentItem() == null) {
			return;
		}
		ItemStack is = event.getCurrentItem();
		if (!is.hasItemMeta()) {
			return;
		}
		String lore = is.getItemMeta().getLore().get(1);
		if (i.getName().contains("Spigot Resources")) {
			if (is.getItemMeta().hasLore()) {
				String stripLore = ChatColor.stripColor(lore);
				int id = Integer.parseInt(stripLore.replaceAll("ID: ", "").toString());
				try {
					SpigetWebhook.downloadResource(id);
				} catch (IOException e) {
					player.sendMessage(prefix + "Failed to download resource.");
				}
				player.sendMessage(prefix + "Downloaded and loaded resource!");
				
				event.setCancelled(true);
			}
		}
	}
	
	public String checkUpdate() {
		try {
            HttpURLConnection con = (HttpURLConnection) new URL(
                    "http://www.spigotmc.org/api/general.php").openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.getOutputStream()
                    .write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=32431")
                            .getBytes("UTF-8"));
            String version = new BufferedReader(new InputStreamReader(
                    con.getInputStream())).readLine();
            if (version.length() <= 7) {
                return version;
            }
        } catch (Exception ex) {
            getLogger().info("Failed to check for a update on spigot.");
        }
		return null;
	}

	private void metrics() {
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			this.getLogger().info("Failed to send metrics to http://mcstats.org. :-(");
		}
	}

	public static UnlimitedPlugins getInstance() {
		return instance;
	}


}
