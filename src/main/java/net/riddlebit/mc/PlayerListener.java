package net.riddlebit.mc;

import dev.morphia.query.Query;
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

        Query<MongoPlayer> query = plugin.datastore.createQuery(MongoPlayer.class);
        MongoPlayer mongoPlayer = query.field("uuid").equal(player.getUniqueId().toString()).first();

        if (mongoPlayer == null) {
            mongoPlayer = new MongoPlayer();
            mongoPlayer.uuid = player.getUniqueId().toString();
            mongoPlayer.name = player.getDisplayName();
            mongoPlayer.reputation = 0;
            plugin.datastore.save(mongoPlayer);
        } else {
            // player exists in database
        }

    }

}
