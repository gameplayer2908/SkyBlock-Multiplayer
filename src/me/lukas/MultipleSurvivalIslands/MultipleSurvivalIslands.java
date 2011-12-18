package me.lukas.MultipleSurvivalIslands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MultipleSurvivalIslands extends JavaPlugin {
	PluginDescriptionFile pluginFile = null;
	Logger log = null;
	PluginManager manager;

	PlayerBreackBlockListener breakBlock;
	PlayerPlaceBlockListener placeBlock;
	PlayerUseBucketListener useBucket;

	protected FileConfiguration config;
	private static World worldIslands = null;
	private static String WORLD_NAME = "worldIslands";
	
	public static FileConfiguration sconf;
	public static File sconfFile;

	@Override
	public void onDisable() {
		log.info(pluginFile.getName() + " Version " + pluginFile.getVersion() + " disabled!");
	}


	@Override
	public void onEnable() {
		pluginFile = this.getDescription();
		log = this.getServer().getLogger();
		breakBlock = new PlayerBreackBlockListener();
		placeBlock = new PlayerPlaceBlockListener();
		useBucket = new PlayerUseBucketListener();
		manager = this.getServer().getPluginManager();

		manager.registerEvent(Type.BLOCK_BREAK, breakBlock , Priority.Normal, this);
		manager.registerEvent(Type.BLOCK_PLACE, placeBlock, Priority.Normal, this);
		manager.registerEvent(Type.PLAYER_BUCKET_EMPTY, useBucket , Priority.Normal, this);
		//manager.registerEvent(arg0, arg1, arg2, arg3)

		log.info(pluginFile.getName() + " Version " + pluginFile.getVersion() + " enabled!");

		config = this.getConfig();
		File confFile = new File(this.getDataFolder(), "config.yml");
		MultipleSurvivalIslands.sconf = config;
		MultipleSurvivalIslands.sconfFile = confFile;

		if(!confFile.exists()){

			ArrayList<ItemStack> alitemsChest = new ArrayList<ItemStack>();
			alitemsChest.add(new ItemStack(Material.ICE, 2));
			alitemsChest.add(new ItemStack(Material.SAPLING,5));
			alitemsChest.add(new ItemStack(Material.MELON,3));
			alitemsChest.add(new ItemStack(Material.CACTUS,1));
			alitemsChest.add(new ItemStack(Material.LAVA_BUCKET,1));

			ItemStack[] itemsChest = new ItemStack[alitemsChest.size()]; 

			for(int i = 0;i < itemsChest.length;i++){
				itemsChest[i] = alitemsChest.get(i);
			}

			String items = "";
			for(ItemStack i: alitemsChest){
				items += i.getType().getId() + ":" + i.getAmount() + ";";
			}

			CreateNewIsland.IslandDistance = 50;
			config.set("islandDistance", 50);
			config.set("chest.items", items);
			config.set("mods", "");

			Data.itemsChest = itemsChest;

			try {
				config.save(confFile);
			} catch (IOException e) {
			}
		}else{
			try {
				config.load(confFile);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try{
				CreateNewIsland.IslandDistance = config.getInt("islandDistance");
			}catch(Exception ex){
				CreateNewIsland.IslandDistance = 50;
			}

			String[] dataItems = config.get("chest.items").toString().split(";");
			ArrayList<ItemStack> alitemsChest = new ArrayList<ItemStack>(); 

			for(String s: dataItems){
				if(s.trim() != ""){
					String[] dataValues = s.split(":");
					try{
						alitemsChest.add(new ItemStack(Integer.parseInt(dataValues[0]), Integer.parseInt(dataValues[1])));
					}catch(Exception ex) {} 
				}
			}

			ItemStack[] itemsChest = new ItemStack[alitemsChest.size()]; 

			for(int i = 0;i < itemsChest.length;i++) {
				itemsChest[i] = alitemsChest.get(i);
			}

			Data.itemsChest = itemsChest;	
			
			// Lade die Mods
			Data.mods = config.getString("mods").split(" ");
		}

		MultipleSurvivalIslands.getWorldIslands();
	}

	/*public boolean anonymousCheck(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cannot execute that command, I don't know who you are!");
            return true;
        } else {
            return false;
        }
    }*/

	public static World getWorldIslands() {
		if (worldIslands == null) {
			//worldIslands = Bukkit.getServer().createWorld(WORLD_NAME, World.Environment.NORMAL, new WorldIslandsdChunkGenerator());
			worldIslands = WorldCreator.name(MultipleSurvivalIslands.WORLD_NAME).environment(Environment.NORMAL).generator(new WorldIslandsdChunkGenerator()).createWorld();
			MultipleSurvivalIslands.CreateSpawnTower();
			worldIslands.setSpawnLocation(0, MultipleSurvivalIslands.getWorldIslands().getHighestBlockYAt(0, 0), 0);
		}

		return worldIslands;
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new WorldIslandsdChunkGenerator();
	}

	private boolean checkIfPlayerInventoryEmpty(Player p){
		for(ItemStack i: p.getInventory().getContents()){
			if(i != null){
				return false;
			}
		}		
		return true;
	}

	public static Chest chest;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		Player player = (Player)sender;

		if(cmd.getName().equalsIgnoreCase("skyblock")){
			if(args.length == 0){
				int PlayerNr = this.findPlayer(player.getName()); // Suche Spieler
				if (PlayerNr == -1){ // Falls der Spieler nicht in der Liste ist, f�ge ihn hinzu
					Data.players.add(new PlayerInfo(player));
				}
				player.teleport(MultipleSurvivalIslands.getWorldIslands().getSpawnLocation()); // Teleportiere Spieler zum Spawntower in der Mitte
				player.sendMessage(ChatColor.GREEN + "Willkommen auf der Welt SkyBlock f�r Multiplayer! Es gibt momentan " + CreateNewIsland.ISLANDNR + " Inseln. Gib '/skyblock start' ein um eine eigene Insel zu bekommen.");
				return true;
			}

			if(args[0].equalsIgnoreCase("start")){
				if(player.getWorld() != MultipleSurvivalIslands.getWorldIslands())
					return true;

				boolean ismepty = this.checkIfPlayerInventoryEmpty(player);	
				if(!ismepty){
					player.sendMessage(ChatColor.RED + "Es ist nicht erlaubt mit Inhalt im Inventar mitzuspielen!");
					return true;
				}

				int PlayerNr = this.findPlayer(player.getName()); // Suche Spieler
				if (PlayerNr == -1){ // Falls der Spieler nicht in der Liste ist, f�ge ihn hinzu
					Data.players.add(new PlayerInfo(player));

					PlayerNr = this.findPlayer(player.getName());
				}
				if(Data.players.get(PlayerNr).getHasIsland()){ // Hat bereits eine Insel
					player.sendMessage(ChatColor.RED + "Du hattest bereits eine Insel und hast Mist gebaut - heul den Eventmanager voll, vielleicht gibt er dir eine neue Insel.");
					return true;
				} else {
					new CreateNewIsland(player);
					Data.players.get(PlayerNr).setHasIslandToTrue();
					player.sendMessage(ChatColor.GREEN + "Fall nicht runter, und mach kein Obsidian :-)");
				}										
			}

			if(args[0].equalsIgnoreCase("leave")){
				int PlayerNr = this.findPlayer(player.getName());
				if (PlayerNr == -1){
					player.setHealth(0);
				}
				Location l = Data.players.get(PlayerNr).getOldPlayerLocation();
				player.teleport(l);
				player.sendMessage(ChatColor.GREEN + "Du hast die Welt Skyblock verlassen und (vielleicht) zur�ck auf sicherem Boden. Achte auf den Creeper hinter dir.");
				return true;
			}

			if(args[0].equalsIgnoreCase("newisland")){
				
				if (!this.isMod(player)){
					player.sendMessage(ChatColor.RED + "Du bist nicht autorisiert!");
					return true;
				}
				
				if(args.length == 1){
					player.sendMessage("Du musst einen Spielernamen angeben!");
					return true;
				}

				int playerNr = this.findPlayer(args[1]);
				if (playerNr == -1){
					player.sendMessage("Es gibt keinen Spieler mit diesem Namen.");
					return true;
				}
				Player tp = Data.players.get(playerNr).getPlayer();
				new  CreateNewIsland(tp);
				player.sendMessage("Der Spieler "+tp.getName() + " wurde auf eine neue Insel teleportiert.");
				return true;
			}
			
			if(args[0].equalsIgnoreCase("addmod")){
				if (!this.isMod(player)){
					player.sendMessage(ChatColor.RED + "Du bist nicht autorisiert!");
					return true;
				}
				
				if(args.length == 1){
					player.sendMessage("Du musst einen Spielernamen angeben!");
					return true;
				}
				
				for(String s:Data.mods){
					if(s.equalsIgnoreCase(args[1])){
						player.sendMessage(ChatColor.GREEN + "Der Spieler ist schon in der Liste der Mods!");
						return true;
					}
				}
				
				Data.addMod(args[1]);
				player.sendMessage(args[1] + " wurde zu der Liste der Mods hinzugef�gt.");
				return true;
			}
		}

		/*if(cmd.getName().equalsIgnoreCase("create")){
			player.sendMessage("Erstelle ein paar Inseln.");
			if(args.length == 0) {
				new CreateNewIsland().CreateIt(1, player);
			}else{				
				new CreateNewIsland().CreateIt(Integer.parseInt(args[0]), player);
			}
			return true;
		}*/
		return false;
	}

	public int findPlayer(String playername){
		for (int i=0;i<Data.players.size();i++){
			if(Data.players.get(i).getPlayerName().equalsIgnoreCase(playername)){
				return i;
			}	
		}
		return -1;
	}
	
	public boolean isMod(Player p){
		for (String s: Data.mods){
			if (s.equalsIgnoreCase(p.getName())){
				return true;
			}
		}
		return false;
	}

	private static void makeBlock(int x, int y, int z, Material m){
		MultipleSurvivalIslands.getWorldIslands().getBlockAt(x,y,z).setType(m);
	}

	private static void quader(int x, int y, int z, Material m){
		if (y < 0) return;
		MultipleSurvivalIslands.makeBlock(x, y, z, m);
		MultipleSurvivalIslands.makeBlock(x, y, z+1, m);
		MultipleSurvivalIslands.makeBlock(x+1, y, z, m);
		MultipleSurvivalIslands.makeBlock(x+1, y, z+1, m);
	}



	private static void CreateSpawnTower(){	
		int yStart = 2;
		int yEnde = 90;
		int[][] lavatreppe = {{2,0},{2,0},{0,2},{0,2},{-2,0},{-2,0},{0,-2},{0,-2}};
		int i = 0;
		int x = -2;
		int z = -2;
		for (int y=yStart;y < yEnde-2;y++){
			// Mache Obsidian Turm
			MultipleSurvivalIslands.quader(0,y,0,Material.OBSIDIAN);
			// Mache Innenwand
			for(int xw=-2;xw<4;xw++){
				MultipleSurvivalIslands.makeBlock(xw, y, -2, Material.GLASS);
				MultipleSurvivalIslands.makeBlock(xw, y, 3, Material.GLASS);
			}
			for(int zw=-2;zw<4;zw++){
				MultipleSurvivalIslands.makeBlock(-2, y, zw, Material.GLASS);
				MultipleSurvivalIslands.makeBlock(3, y, zw, Material.GLASS);
			}
			// Mache Lavatreppe
			MultipleSurvivalIslands.quader(x,y,z,Material.getMaterial(43));
			x += lavatreppe[i][0];
			z += lavatreppe[i][1];
			i++;
			if (i == lavatreppe.length)
				i=0;

		}
		// Wassertreppe
		i = 0;
		x = -2;
		z = -2;
		for (int y=yStart;y <= yEnde;y++){
			MultipleSurvivalIslands.quader(x,y-3,z,Material.GLASS);
			x += lavatreppe[i][0];
			z += lavatreppe[i][1];
			i++;
			if (i == lavatreppe.length)
				i=0;
		}

		// Setze die Treppe, Ganzesteine
		i = 0;
		x = -2;
		z = -4;
		int[][] stairsWhole = {{4,0},{2,2},{0,4},{-2,2},{-4,0},{-2,-2},{0,-4},{2,-2}};
		for (int y=yStart+1;y < yEnde-1;y++){
			MultipleSurvivalIslands.quader(x,y,z,Material.getMaterial(43));
			x += stairsWhole[i][0];
			z += stairsWhole[i][1];
			i++;
			if (i == stairsWhole.length)
				i=0;
		}
		// Setze die Treppe, Halbesteine
		i = 0;
		x = -4;
		z = -4;
		int[][] stairsHalf = {{4,0},{4,0},{0,4},{0,4},{-4,0},{-4,0},{0,-4},{0,-4}};
		for (int y=yStart+1;y < yEnde-1;y++){
			MultipleSurvivalIslands.quader(x,y,z,Material.getMaterial(44));
			x += stairsHalf[i][0];
			z += stairsHalf[i][1];
			i++;
			if (i == stairsHalf.length)
				i=0;
		}
		
		// Setze Lava
		MultipleSurvivalIslands.makeBlock(2, yEnde-3, 2, Material.LAVA);
		// Setze Wasser
		MultipleSurvivalIslands.makeBlock(-1, yEnde-3, 0, Material.WATER);
		
		// Mache Dach
		for (x=-2;x<4;x++){
			for (z=-2;z<4;z++){
				MultipleSurvivalIslands.makeBlock(x, yEnde-2, z, Material.getMaterial(43));
			}
		}

		// Zaun
		for (x=-2;x<4;x++){
			MultipleSurvivalIslands.makeBlock(x, yEnde-1, -2, Material.FENCE);
		}
		for (z=-2;z<4;z++){
			MultipleSurvivalIslands.makeBlock(-2, yEnde-1, z, Material.FENCE);
			MultipleSurvivalIslands.makeBlock(3, yEnde-1, z, Material.FENCE);
		}
		
		// Fackeln
		MultipleSurvivalIslands.makeBlock(-2, yEnde, -2, Material.TORCH);
		MultipleSurvivalIslands.makeBlock(-2, yEnde, 3, Material.TORCH);
		MultipleSurvivalIslands.makeBlock(3, yEnde, -2, Material.TORCH);
		MultipleSurvivalIslands.makeBlock(3, yEnde, 3, Material.TORCH);
		
		// Mache den Towerboden
		// Mache Dach
		for (x=-2;x<4;x++){
			for (z=-2;z<4;z++){
				for (int y=0;y<yStart;y++){
					MultipleSurvivalIslands.makeBlock(x, y, z, Material.AIR);
				}
				MultipleSurvivalIslands.makeBlock(x, yStart, z, Material.getMaterial(43));
				
			}
		}
	}
	
	
}
