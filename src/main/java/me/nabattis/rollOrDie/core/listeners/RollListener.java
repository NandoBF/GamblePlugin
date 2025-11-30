package me.nabattis.rollOrDie.core.listeners;

import me.nabattis.rollOrDie.RollOrDie;
import me.nabattis.rollOrDie.core.RollingHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;

public class RollListener implements Listener {
    private final ArrayList<UUID> pendingDeaths = new ArrayList<>();
    private final RollOrDie plugin;

    public RollListener(RollOrDie plugin){
        this.plugin = plugin;
    }

    public void addPendingDeaths(ArrayList<UUID> playersUUIDS){
        pendingDeaths.addAll(playersUUIDS);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        UUID playerID = event.getEntity().getUniqueId();

        if (pendingDeaths.contains(playerID)){
            event.setKeepLevel(true);
            event.setDroppedExp(0);
        }
        pendingDeaths.remove(playerID);
        event.deathMessage(Component.text(
                event.getEntity().getName() + " lost the gambling...",
                NamedTextColor.DARK_RED
        ));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        plugin.getGamblingHandler().checkGameStatus();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        RollingHandler gh = plugin.getGamblingHandler();
       plugin.getServer().getScheduler().runTaskLater(
                JavaPlugin.getProvidingPlugin(RollListener.class),
                gh::checkGameStatus,
                1L
        );
    }
}
