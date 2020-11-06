package net.riddlebit.mc.commands;

import net.riddlebit.mc.RFChat;
import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.ChunkData;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
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
        switch (args[0].toLowerCase()) {
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
                if (args.length > 1) {
                    String factionName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    return leaveFaction(factionName, player);
                }
                break;
            case "status":
                return status(player);
            case "list":
                return list(player);
            case "inspect":
                return inspect(player);
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
            String[] validArgs = {"status", "list", "inspect", "create", "invite", "join", "leave", "claim", "clear"};
            for (String arg : validArgs) {
                if (arg.startsWith(args[0].toLowerCase())) {
                    returnList.add(arg);
                }
            }
        } else if (args.length > 1) {
            switch (args[0].toLowerCase()) {
                case "join":
                case "leave":
                    String factionName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    for (FactionData factionData : plugin.dataManager.factions.values()) {
                        if (factionData.name.toLowerCase().contains(factionName.toLowerCase())) {
                            returnList.add(factionData.name);
                        }
                    }
                    break;
                case "invite":
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getDisplayName().toLowerCase().contains(args[1].toLowerCase())) {
                            returnList.add(player.getDisplayName());
                        }
                    }
                    break;
            }
        }
        return returnList;
    }

    private boolean createFaction(String factionName, Player player) {
        if (factionName == null) return false;
        return plugin.factionController.createFaction(factionName.trim(), player);
    }

    private boolean joinFaction(String factionName, Player player) {
        if (factionName == null) return false;
        return plugin.factionController.joinFaction(factionName, player);
    }

    private boolean leaveFaction(String factionName, Player player) {
        return plugin.factionController.leaveFaction(factionName, player);
    }

    private boolean inviteToFaction(String inviteeName, Player inviter) {
        Player invitee = Bukkit.getPlayer(inviteeName);
        if (invitee == null) {
            RFChat.toPlayer(inviter, "Failed to find player: " + inviteeName);
            return true;
        }
        return plugin.factionController.inviteToFaction(inviter, invitee);
    }

    private boolean status(Player player) {
        FactionData factionData = plugin.factionController.getFactionForPlayer(player);
        if (factionData == null) {
            RFChat.toPlayer(player, "You're not in a faction...");
            return true;
        }

        PlayerData playerData = plugin.dataManager.getPlayerData(player);
        int onlinePlayersInFaction = factionData.getOnlinePlayersInFaction().size();
        float factionReputationRate = plugin.factionController.getReputationRateForFaction(factionData);
        float playerReputationRate = factionReputationRate / onlinePlayersInFaction;

        int ownedChunksCount = factionData.ownedChunks.size();
        boolean isSustainable = plugin.chunkController.isFactionSustainable(factionData);
        String factionMembers = Arrays.toString(factionData.getPlayersInFaction().toArray());

        String status = ChatColor.RED + "-> " + ChatColor.GOLD + "Faction Status:\n";
        status += ChatColor.WHITE + "----------------------------------------\n";
        status += ChatColor.GOLD + "Faction: " + ChatColor.WHITE + factionData.name + "\n";
        status += ChatColor.GOLD + "Reputation: ";
        status += ChatColor.WHITE + Integer.toString((int) playerData.reputation);
        status += ChatColor.GOLD + " / ";
        status += ChatColor.WHITE + Integer.toString((int) factionData.getReputation());
        status += ChatColor.GOLD + " (" + ChatColor.WHITE + (int) playerReputationRate;
        status += ChatColor.GOLD + " / " + ChatColor.WHITE + (int) factionReputationRate;
        status += ChatColor.GRAY + " per hour" + ChatColor.GOLD + ")\n";
        status += "Claimed chunks: " + (isSustainable ? ChatColor.GREEN : ChatColor.RED) + ownedChunksCount + "\n";
        status += ChatColor.GOLD + "Members: \n";
        status += ChatColor.GOLD + " - " + ChatColor.WHITE + factionMembers
                .replace("[[", "")
                .replace(",", ChatColor.GOLD + " (" + ChatColor.WHITE )
                .replace("]]", ChatColor.GOLD + " )" )+ "\n";
        status += ChatColor.WHITE + "----------------------------------------";
        player.sendMessage(status);
        return true;
    }

    private boolean list(Player player) {
        String message = ChatColor.RED + "-> " + ChatColor.GOLD + "Faction List:\n";
        message += ChatColor.WHITE + "----------------------------------------\n";
        for (FactionData factionData : plugin.dataManager.factions.values()) {
            boolean isSustainable = plugin.chunkController.isFactionSustainable(factionData);
            int playerCount = factionData.players.size();
            int onlinePlayers = factionData.getOnlinePlayersInFaction().size();
            message += ChatColor.WHITE + factionData.name + ChatColor.GOLD + " | ";
            message += isSustainable ? ChatColor.GREEN : ChatColor.RED;
            message += Integer.toString((int) factionData.getReputation()) + ChatColor.GOLD + " | " + ChatColor.WHITE;
            message += onlinePlayers + "/" + playerCount + ChatColor.GRAY + " players online\n";
        }
        message += ChatColor.WHITE + "----------------------------------------";
        player.sendMessage(message);
        return true;
    }

    private boolean inspect(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        ChunkData chunkData = new ChunkData(chunk.getX(), chunk.getZ());
        if (plugin.chunkController.isChunkWithinSpawnRadius(chunkData)) {
            RFChat.toPlayer(player, "This is a spawn chunk, it cant be claimed...");
        } else {
            float chunkCost = plugin.chunkController.getChunkReputationCost(chunkData);
            RFChat.toPlayer(player, "This chunk costs " + chunkCost + " reputation");
        }
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
            RFChat.toPlayer(player, "You're not an operator...");
        }
    }
}
