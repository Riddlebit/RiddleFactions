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

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("fcreate")) {
			sender.sendMessage("Faction \"[fname]\" created!");
			return true;
		}
		if (command.getName().equalsIgnoreCase("finvite")) {
			sender.sendMessage("Invited [name]");
			return true;
		}

		if (command.getName().equalsIgnoreCase("fjoin")) {
			sender.sendMessage("Joined \"[fname]\"");
			return true;
		}

		if (command.getName().equalsIgnoreCase("fclear")) {
			if(true){
				sender.sendMessage("Cleared chunk.");
			}else{
				sender.sendMessage("Failed to clear chunk!");
			}
			return true;
		}

		if (command.getName().equalsIgnoreCase("fclaim")) {
			if(true){
				sender.sendMessage("Claimed chunk.");
			}else{
				sender.sendMessage("Failed to claim chunk, not enough reputation!");
				sender.sendMessage("Failed to claim chunk, occupied or border!");
			}return true;
		}

		if (command.getName().equalsIgnoreCase("fstatus")) {
			sender.sendMessage("Your reputation: 0, faction reputation: 0, current chunk owner: nobody/faction/border");
			return true;
		}

		return false;
	}
}
