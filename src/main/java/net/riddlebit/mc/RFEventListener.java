package net.riddlebit.mc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class RFEventListener implements Listener {

    private RiddleFactions plugin;

    public RFEventListener(RiddleFactions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.playerController.addPlayer(player);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        plugin.playerController.onPlayerPlaceBlock(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.playerController.onPlayerBreakBlock(event);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        plugin.playerController.onPlayerDeath(event);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        plugin.playerController.onPlayerMove(event);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        plugin.playerController.onPlayerTeleport(event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        plugin.playerController.onPlayerInteract(event);
    }

    @EventHandler
    public void onBlockExplodeEvent(BlockExplodeEvent event) {
        plugin.treasureController.removeTreasure(event.getBlock());
    }

}
