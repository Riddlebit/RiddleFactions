package net.riddlebit.mc;

import net.riddlebit.mc.commands.RFCommand;
import net.riddlebit.mc.controller.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class RiddleFactions extends JavaPlugin {

    public FileConfiguration config;
    public DataManager dataManager;
    public PlayerController playerController;
    public FactionController factionController;
    public TreasureController treasureController;
    public ChunkController chunkController;

    @Override
    public void onEnable() {

        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();

        dataManager = new DataManager(this);

        // Register commands
        this.getCommand("rf").setExecutor(new RFCommand(this));
        this.getCommand("rf").setTabCompleter(new RFCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new RFEventListener(this), this);

        // Controllers
        playerController = new PlayerController(this);
        factionController = new FactionController(this);
        treasureController = new TreasureController(this);
        chunkController = new ChunkController(this);

        // Schedule reputation updating
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> factionController.updateReputation(), 20, 20);

        // Schedule database saving
        int saveFrequency = config.getInt("db-save-frequency");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> dataManager.save(), saveFrequency, saveFrequency);
    }

    @Override
    public void onDisable() {
        dataManager.save();
    }

}
