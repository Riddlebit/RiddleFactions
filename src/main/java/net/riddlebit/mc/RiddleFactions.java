package net.riddlebit.mc;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import net.riddlebit.mc.commands.RFCommand;
import net.riddlebit.mc.controller.DataManager;
import net.riddlebit.mc.controller.FactionController;
import net.riddlebit.mc.controller.PlayerController;
import net.riddlebit.mc.controller.TreasureController;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RiddleFactions extends JavaPlugin {

    public Morphia morphia;
    public Datastore datastore;

    public DataManager dataManager;
    public PlayerController playerController;
    public FactionController factionController;
    public TreasureController treasureController;

    @Override
    public void onEnable() {

        // Database stuff
        morphia = new Morphia();
        morphia.map(PlayerData.class);
        morphia.map(FactionData.class);
        datastore = morphia.createDatastore(new MongoClient(), "riddleFactions");
        datastore.ensureIndexes();

        dataManager = new DataManager(this);

        // Register commands
        this.getCommand("rf").setExecutor(new RFCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new RFEventListener(this), this);

        // Controllers
        playerController = new PlayerController(this);
        factionController = new FactionController(this);
        treasureController = new TreasureController(this);

        // Schedule reputation updating
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> factionController.updateReputation(), 20, 20);

        // Schedule database saving
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> dataManager.save(), 12000, 12000);
    }

    @Override
    public void onDisable() {
        dataManager.save();
    }

}
