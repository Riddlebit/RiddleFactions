package net.riddlebit.mc.commands;

import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RFCommand implements TabExecutor {

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
                    return inviteToFaction(playerName, player);
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
            case "list":
                return list(player);
            case "claim":
                return claimChunk(player);
            case "clear":
                return clearChunk(player);
            case "give":
                if (args.length > 1) {
                    try {
                        float reputation = Float.parseFloat(args[1]);
                        giveReputation(player, reputation);
                    } catch (Exception e) {
                        plugin.getLogger().severe(e.getMessage());
                    }
                    return true;
                }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> returnList = new ArrayList<>();
        if (args.length == 1) {
            returnList.add("status");
            returnList.add("list");
            returnList.add("create");
            returnList.add("invite");
            returnList.add("join");
            returnList.add("leave");
            returnList.add("claim");
            returnList.add("clear");
        } else if (args.length > 1) {
            switch (args[0].toLowerCase()) {
                case "join":
                    for (FactionData factionData : plugin.dataManager.factions.values()) {
                        returnList.add(factionData.name);
                    }
                    break;
                case "invite":
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        returnList.add(player.getDisplayName());
                    }
                    break;

            }
        }
        return returnList;
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

    private boolean inviteToFaction(String inviteeName, Player inviter) {
        Player invitee = Bukkit.getPlayer(inviteeName);
        if (invitee == null) {
            inviter.sendMessage("Failed to find player " + inviteeName);
            return true;
        }
        return plugin.factionController.inviteToFaction(inviter, invitee);
    }

    private boolean status(Player player) {
        FactionData factionData = plugin.factionController.getFactionForPlayer(player);
        if (factionData == null) {
            player.sendMessage("You're not in a faction");
            return true;
        }

        PlayerData playerData = plugin.dataManager.getPlayerData(player);
        int onlinePlayersInFaction = plugin.factionController.getOnlinePlayersInFaction(factionData).size();
        float factionReputationRate = plugin.factionController.getReputationRateForFaction(factionData);
        float playerReputationRate = factionReputationRate / onlinePlayersInFaction;

        String status = "";
        status = "Faction: " + factionData.name + "\n";
        status += "Reputation: " + (int) playerData.reputation + " / " + (int) factionData.getReputation();
        status += " (" + (int) playerReputationRate + " / " + (int) factionReputationRate + " per hour)";

        player.sendMessage(status);
        return true;
    }

    private boolean list(Player player) {
        String message = ChatColor.BOLD + "---- Faction List ----" + "\n" + ChatColor.RESET;
        for (FactionData factionData : plugin.dataManager.factions.values()) {
            int chunkCount = factionData.ownedChunks.size();
            boolean canAfford = factionData.canAffordChunkCount(chunkCount);
            message += canAfford ? ChatColor.GREEN : ChatColor.RED;
            message += factionData.name + " - " + (int) factionData.getReputation() + "\n";
        }
        message += ChatColor.RESET;
        message += ChatColor.BOLD + "--------------------";
        player.sendMessage(message);
        return true;
    }

    private boolean claimChunk(Player player) {
        return plugin.factionController.claimChunk(player);
    }

    private boolean clearChunk(Player player) {
        return plugin.factionController.clearChunk(player);
    }

    private void giveReputation(Player player, float reputation) {
        if (player.isOp()) {
            PlayerData playerData = plugin.dataManager.getPlayerData(player);
            playerData.reputation += reputation;
        } else {
            player.sendMessage("You're not an operator...");
        }
    }
}
