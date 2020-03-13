package net.riddlebit.mc.commands;

import net.riddlebit.mc.RiddleFactions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class RFCommand implements CommandExecutor {

    private RiddleFactions plugin;

    public RFCommand(RiddleFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (args.length < 1) return false;
        switch (args[0]) {
            case "create":
                if (args.length > 1) {
                    String factionName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    return createFaction(factionName, player);
                }
                break;
        }
        return false;
    }

    private boolean createFaction(String factionName, Player player) {
        if (factionName == null) return false;
        return plugin.factionController.createFaction(factionName, player);
    }

}
