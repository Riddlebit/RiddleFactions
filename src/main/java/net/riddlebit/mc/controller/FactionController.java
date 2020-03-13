package net.riddlebit.mc.controller;

import dev.morphia.query.Query;
import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.Invite;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionController {

    private RiddleFactions plugin;

    private HashMap<String, FactionData> factions;

    private Set<Invite> invites;

    public FactionController(RiddleFactions plugin) {
        this.plugin = plugin;
        factions = new HashMap<>();
        invites = new HashSet<>();

        // Load factions from database
        Query query = plugin.datastore.createQuery(FactionData.class);
        List<FactionData> loadedFactions = query.find().toList();
        if (loadedFactions.size() > 0) {
            for (FactionData factionData : loadedFactions) {
                factions.put(factionData.name, factionData);
            }
        }

    }

    public boolean createFaction(String factionName, Player player) {

        if (isPlayerInFaction(player)) {
            player.sendMessage("You're already in a faction...");
            return true;
        }

        if (factions.containsKey(factionName)) {
            player.sendMessage("A faction with that name already exists");
            return true;
        }

        // Create the new faction
        PlayerData playerData = plugin.playerController.getPlayer(player);
        FactionData factionData = new FactionData(playerData);
        factionData.name = factionName;
        factions.put(factionName, factionData);
        plugin.datastore.save(factionData);

        player.sendMessage(factionName + " was created!");
        return true;
    }

    public boolean invite(Player inviter, Player invitee) {

        if (!isPlayerInFaction(inviter)) {
            inviter.sendMessage("You must be in a faction...");
            return true;
        }

        FactionData factionData = getFactionForPlayer(inviter);
        PlayerData inviterData = plugin.playerController.getPlayer(inviter);
        PlayerData inviteeData = plugin.playerController.getPlayer(invitee);

        if (factionData == null || inviterData == null || inviteeData == null) {
            return false;
        }

        if (inviterData.equals(inviteeData)) {
            inviter.sendMessage("You cannot invite yourself...");
            return true;
        }

        Invite invite = new Invite(factionData, inviterData, inviteeData);
        invites.add(invite);

        inviter.sendMessage(invitee.getDisplayName() + " was invited to your faction");
        invitee.sendMessage("You have been invited to " + factionData.name);
        return true;
    }

    public boolean joinFaction(String factionName, Player player) {
        PlayerData playerData = plugin.playerController.getPlayer(player);

        Invite invite = null;
        for (Invite i : invites) {
            if (i.factionData.name.equals(factionName) && i.invitee.equals(playerData)) {
                invite = i;
            }
        }

        if (invite == null) {
            player.sendMessage("No invite found :(");
            return true;
        }

        invites.remove(invite);

        // Remove player from current faction
        if (isPlayerInFaction(player)) {
            FactionData currentFactionData = getFactionForPlayer(player);
            currentFactionData.players.remove(playerData);
            if (currentFactionData.players.size() > 0) {
                plugin.datastore.save(currentFactionData);
            } else {
                plugin.datastore.delete(currentFactionData);
            }
        }

        // Reset player reputation
        playerData.reputation = 0;
        plugin.datastore.save(playerData);

        // Add player to new faction
        FactionData factionData = invite.factionData;
        factionData.players.add(playerData);
        plugin.datastore.save(factionData);

        player.sendMessage("You have joined " + factionData.name);
        for (Player factionPlayer : getPlayersInFaction(factionName)) {
            if (factionPlayer != player) {
                factionPlayer.sendMessage(player.getDisplayName() + " has joined your faction!");
            }
        }
        return true;
    }

    public boolean leaveFaction(Player player) {
        if (!isPlayerInFaction(player)) {
            player.sendMessage("You're not in a faction...");
            return true;
        }

        FactionData factionData = getFactionForPlayer(player);
        PlayerData playerData = plugin.playerController.getPlayer(player);

        // Remove player from faction
        factionData.players.remove(playerData);
        if (factionData.players.size() > 0) {
            plugin.datastore.save(factionData);

            // Broadcast to other faction members
            for (Player factionPlayer : getPlayersInFaction(factionData)) {
                factionPlayer.sendMessage(player.getDisplayName() + " left your faction!");
            }

        } else {
            // Delete faction if there are no players left
            plugin.datastore.delete(factionData);
        }

        // Reset player reputation
        playerData.reputation = 0;
        plugin.datastore.save(playerData);

        player.sendMessage("You left " + factionData.name);
        return true;
    }

    public boolean isPlayerInFaction(Player player) {
        return getFactionForPlayer(player) != null;
    }

    public FactionData getFaction(String factionName) {
        return factions.get(factionName);
    }

    public FactionData getFactionForPlayer(Player player) {
        PlayerData playerData = plugin.playerController.getPlayer(player);
        for (FactionData factionData : factions.values()) {
            if (factionData.players.contains(playerData)) {
                return factionData;
            }
        }
        return null;
    }

    public List<Player> getPlayersInFaction(FactionData factionData) {
        List<Player> players = new ArrayList<>();
        for (PlayerData playerData : factionData.players) {
            Player player = Bukkit.getPlayer(UUID.fromString(playerData.uuid));
            players.add(player);
        }
        return players;
    }

    public List<Player> getPlayersInFaction(String factionName) {
        FactionData factionData = getFaction(factionName);
        return getPlayersInFaction(factionData);
    }

}
