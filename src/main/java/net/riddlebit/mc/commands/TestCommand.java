package net.riddlebit.mc.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        // Give the player a diamond ;)
        Player player = (Player) sender;
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        player.getInventory().addItem(diamond);

        return true;
    }

}
