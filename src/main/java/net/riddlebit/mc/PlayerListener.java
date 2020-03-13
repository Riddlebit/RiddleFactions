package net.riddlebit.mc;

import net.riddlebit.mc.data.MongoPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private RiddleFactions plugin;

    public PlayerListener(RiddleFactions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        MongoPlayer mongoPlayer = new MongoPlayer();
        mongoPlayer.uuid = player.getUniqueId().toString();
        mongoPlayer.name = player.getDisplayName();
        mongoPlayer.reputation = 0;

        plugin.datastore.save(mongoPlayer);
    }

}
