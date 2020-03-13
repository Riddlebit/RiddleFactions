package net.riddlebit.mc;

import dev.morphia.query.Query;
import net.riddlebit.mc.data.PlayerData;
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

        Query<PlayerData> query = plugin.datastore.createQuery(PlayerData.class);
        PlayerData playerData = query.field("uuid").equal(player.getUniqueId().toString()).first();

        if (playerData == null) {
            playerData = new PlayerData();
            playerData.uuid = player.getUniqueId().toString();
            playerData.name = player.getDisplayName();
            playerData.reputation = 0;
            plugin.datastore.save(playerData);
        } else {
            // player exists in database
        }

    }

}
