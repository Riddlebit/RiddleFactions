package net.riddlebit.mc.commands;

import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.Bukkit;
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
            case "invite":
                if (args.length > 1) {
                    String playerName = args[1];
                    return invitePlayer(playerName, player);
                }
                break;
            case "join":
                if (args.length > 1) {
                    String factionName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    return joinFaction(factionName, player);
                }
                break;
            case "leave":
                return leaveFaction(player);
            case "status":
                return status(player);
            case "claim":
                return claimChunk(player);
        }

        return false;
    }

    private boolean createFaction(String factionName, Player player) {
        if (factionName == null) return false;
        return plugin.factionController.createFaction(factionName, player);
    }

    private boolean joinFaction(String factionName, Player player) {
        if (factionName == null) return false;
        return plugin.factionController.joinFaction(factionName, player);
    }

    private boolean leaveFaction(Player player) {
        return plugin.factionController.leaveFaction(player);
    }

    private boolean invitePlayer(String playerName, Player player) {
        Player invitee = Bukkit.getPlayer(playerName);
        if (invitee == null) {
            player.sendMessage("Failed to find player " + playerName);
            return true;
        }
        return plugin.factionController.invite(player, invitee);
    }

    private boolean status(Player player) {
        FactionData factionData = plugin.factionController.getFactionForPlayer(player);
        if (factionData == null) {
            player.sendMessage("You're not in a faction");
            return true;
        }

        PlayerData playerData = plugin.factionController.getPlayer(player);

        String status = "";
        status = "Faction: " + factionData.name + "\n";
        status += "Reputation: " + (int) playerData.reputation + " / " + (int) factionData.getReputation();

        player.sendMessage(status);
        return true;
    }

    private boolean claimChunk(Player player) {
        return plugin.factionController.claimChunk(player);
    }

}
