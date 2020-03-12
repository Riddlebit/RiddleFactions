package net.riddlebit.mc;

import net.riddlebit.mc.commands.TestCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class RiddleFactions extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register commands
        this.getCommand("test").setExecutor(new TestCommand());
    }

    @Override
    public void onDisable() {

    }
}
