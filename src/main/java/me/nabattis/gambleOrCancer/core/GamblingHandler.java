package me.nabattis.gambleOrCancer.core;

import me.nabattis.gambleOrCancer.GambleOrCancer;
import me.nabattis.gambleOrCancer.core.listeners.GamblingListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GamblingHandler {
    private static final long GAMBLING_TIMER = 20*30;
    private static final long TIME_UNTIL_WARN = 20*10;
    private static final long TIME_AFTER_WARN = 20*5;
    private static int MINIMUM_PLAYERS = 1;

    // Player UUID -> currentRoll
    private final HashMap<Player, Integer> rollsMap = new HashMap<>();
    private final GambleOrCancer plugin;
    private final GamblingListener deathListener;
    private boolean isRunning = false;

    private BukkitTask currentTask;
    private boolean gamblingEnabled = true;


    public GamblingHandler(GambleOrCancer plugin, GamblingListener deathListener){
        this.plugin = plugin;
        this.deathListener = deathListener;
    }

    public void checkGameStatus(){
        if (!gamblingEnabled) {
            stop();
            return;
        };
        int playerCount = plugin.getServer().getOnlinePlayers().size();
        if (isRunning && playerCount < MINIMUM_PLAYERS){
            plugin.getLogger().info("Players left. Stopping gambling");
            stop();
            // Stop the game if player count drops below 2
        } else if (!isRunning && playerCount >= 1){
            // Start if there are enough players
            plugin.getLogger().info("Enough players! Starting gambling game");
            start();
        }
    }


    public void start() {
        if (isRunning) return; // This shouldn't happen
        isRunning = true;
        currentTask = plugin.getServer().getScheduler().runTaskLater(
                plugin, this::startGamblingSession, GAMBLING_TIMER
        );
    }


    public void registerPlayerRoll(Player player, int numberRolled){
        if (rollsMap.containsKey(player)) {
            player.sendMessage(Component.text(" You have already rolled! ",
                    NamedTextColor.RED));
            return;
        }
        plugin.getServer().broadcast(Component.text(
                player.getName() + " rolled a " + numberRolled,
                NamedTextColor.GOLD
        ));
        rollsMap.put(player, numberRolled);
    }

    private void startGamblingSession(){
        if (!gamblingEnabled) return; // just a failsafe

        Bukkit.broadcast(Component.text(
                "Let's get gambling! You have " + (TIME_UNTIL_WARN + TIME_AFTER_WARN)/20 + " seconds use /roll",
                NamedTextColor.AQUA
        ));

        currentTask = plugin.getServer().getScheduler().runTaskLater(
                plugin, this::warnPlayers, TIME_UNTIL_WARN
        );
    }

    private void warnPlayers(){
        // give mega cancer
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!rollsMap.containsKey(player)){
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 777, 7));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 777, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 777, 3));
            }
        }

        currentTask = plugin.getServer().getScheduler().runTaskLater(
                plugin, this::scheduleNextGambling, TIME_AFTER_WARN
        );
    }

    private void scheduleNextGambling(){
        ArrayList<Player> playersToKill = new ArrayList<>();
        if (!rollsMap.isEmpty()){
            // Add to playersToKill the players that didn't roll
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!rollsMap.containsKey(player)){
                    playersToKill.add(player);
                    plugin.getServer().broadcast(Component.text(
                            player.getName() + " did not roll!",
                            NamedTextColor.RED
                    ));
                }
            }
            // Get the player that rolled the minimum value
            Map.Entry<Player, Integer> minimumRoll =
                    Collections.min(rollsMap.entrySet(), Map.Entry.comparingByValue());
            playersToKill.add(minimumRoll.getKey());
            plugin.getServer().broadcast(Component.text(
                    minimumRoll.getKey().getName() + " rolled the smallest: " + minimumRoll.getValue(),
                    NamedTextColor.RED
            ));
        } else {
            handleEmptyRolls(playersToKill);
        }

        // Delay the deaths a bit for suspense and play ticking sounds hehe
        for (int i = 0; i < 3; i++){
            long delay = i * 20L; // 20 ticks per second
            float pitch = 1.0f + (i * 0.5f);//change pitch between sounds

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);
                }}, delay);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> executeLosers(playersToKill), 60L);
    }

    private void executeLosers(ArrayList<Player> playersToKill){
        isRunning = false;
        // Tell the deathListener who we're going to kill
        if (plugin.getServer().getOnlinePlayers().size() < MINIMUM_PLAYERS){
            stop();
            return;
        }

        deathListener.addPendingDeaths(
                new ArrayList<>(playersToKill.stream().map(Entity::getUniqueId).toList()));

        // kill all registered players
        for (Player player : playersToKill) {
            player.setHealth(0.0f);
        }
        start();
    }

    public void stop(){
        isRunning = false;
        if (currentTask != null && !currentTask.isCancelled()){
            currentTask.cancel();
        }
        rollsMap.clear();
        plugin.getLogger().info("Stopping the gambler!");
    }

    private void handleEmptyRolls(ArrayList<Player> playersToKill){
        // Add everyone if no one rolled
        plugin.getServer().broadcast(Component.text(
                "No one rolled rolled! Public execution in 3 seconds",
                NamedTextColor.RED
        ));
        playersToKill.addAll(plugin.getServer().getOnlinePlayers());
    }

    public void toggle(CommandSender sender){
        this.gamblingEnabled = !gamblingEnabled;
        sender.sendMessage(Component.text("Gambling is now " + (gamblingEnabled ? "on" : "off"),
                gamblingEnabled ? NamedTextColor.GREEN : NamedTextColor.RED));

        checkGameStatus();
    }
}
