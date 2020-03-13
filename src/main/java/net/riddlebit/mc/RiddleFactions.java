package net.riddlebit.mc;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import net.riddlebit.mc.commands.RFCommand;
import net.riddlebit.mc.commands.TestCommand;
import net.riddlebit.mc.controller.FactionController;
import net.riddlebit.mc.controller.PlayerController;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RiddleFactions extends JavaPlugin {

    public Morphia morphia;
    public Datastore datastore;

    public PlayerController playerController;
    public FactionController factionController;

    @Override
    public void onEnable() {

        // Database stuff
        morphia = new Morphia();
        morphia.map(PlayerData.class);
        morphia.map(FactionData.class);
        datastore = morphia.createDatastore(new MongoClient(), "riddleFactions");
        datastore.ensureIndexes();

        // Register commands
        this.getCommand("test").setExecutor(new TestCommand());
        this.getCommand("rf").setExecutor(new RFCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Controllers
        playerController = new PlayerController(this);
        factionController = new FactionController(this);

        // Schedule reputation updating
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> factionController.updateReputation(), 20, 20);

        // Schedule database saving
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> playerController.savePlayers(), 12000, 12000);
    }

    @Override
    public void onDisable() {
        playerController.savePlayers();
    }

}
