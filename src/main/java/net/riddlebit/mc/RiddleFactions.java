package net.riddlebit.mc;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import net.riddlebit.mc.commands.TestCommand;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.plugin.java.JavaPlugin;

public class RiddleFactions extends JavaPlugin {

    public Morphia morphia;
    public Datastore datastore;

    @Override
    public void onEnable() {

        // Database stuff
        morphia = new Morphia();
        morphia.map(PlayerData.class);
        datastore = morphia.createDatastore(new MongoClient(), "riddleFactions");
        datastore.ensureIndexes();

        // Register commands
        this.getCommand("test").setExecutor(new TestCommand());

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

    }

    @Override
    public void onDisable() {

    }

}
