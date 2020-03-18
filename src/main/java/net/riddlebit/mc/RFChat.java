package net.riddlebit.mc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RFChat {

    public static void toPlayer(Player player, String message) {
        String finalMessage = ChatColor.RED + "-> ";
        finalMessage += ChatColor.GOLD + message;
        player.sendMessage(finalMessage);
    }

    public static void broadcast(String message) {
        String finalMessage = ChatColor.RED + "-> ";
        finalMessage += ChatColor.GOLD + message;
        Bukkit.broadcastMessage(finalMessage);
    }

}