package me.nabattis.rollOrDie.core;

import me.nabattis.rollOrDie.RollOrDie;
import me.nabattis.rollOrDie.core.listeners.RollListener;
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

import static net.kyori.adventure.text.Component.text;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RollingHandler {
    private static long TIME_BETWEEN_SESSIONS;
    private static long TIME_BETWEEN_SESSIONS_DEF;
    private static long TIME_UNTIL_WARN;
    private static long TIME_AFTER_WARN;
    private static int MINIMUM_PLAYERS;
    private static long AFK_TIME;

    // Player UUID -> currentRoll
    private final HashMap<Player, Integer> rollsMap = new HashMap<>();
    private final ArrayList<Player> playersRolling = new ArrayList<>();
    private final RollOrDie plugin;
    private final RollListener deathListener;
    private boolean isRunning = false;

    private BukkitTask currentTask;
    private boolean gamblingEnabled = true;


    public RollingHandler(RollOrDie plugin, RollListener deathListener){
        this.plugin = plugin;
        this.deathListener = deathListener;
        TIME_BETWEEN_SESSIONS = plugin.getConfig().getLong("root.time_between_sessions", 300) * 20L;
        TIME_BETWEEN_SESSIONS_DEF = TIME_BETWEEN_SESSIONS;
        TIME_UNTIL_WARN = plugin.getConfig().getLong("root.time_until_warn", 20) * 20L;
        TIME_AFTER_WARN = plugin.getConfig().getLong("root.time_after_warn", 7) * 20L;
        MINIMUM_PLAYERS = plugin.getConfig().getInt("root.minimum_players_online", 2);
        AFK_TIME = plugin.getConfig().getLong("root.afk_time", 10L);
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
            //
            // Stop the game if player count drops below MINIMUM_PLAYERS
        } else if (!isRunning && playerCount >= MINIMUM_PLAYERS){
            // Start if there are enough players
            plugin.getLogger().info("Enough players! Starting gambling game");
            start();
        } else {
            plugin.getLogger().info("Not enough players. Pausing gambler");
        }

    }


    public void start() {
        if (isRunning) return; // This shouldn't happen
        isRunning = true;
        rollsMap.clear();
        playersRolling.clear();

        // Revert to default since it was probably changed by the random sum
        TIME_BETWEEN_SESSIONS = TIME_BETWEEN_SESSIONS_DEF;

        TIME_BETWEEN_SESSIONS += ThreadLocalRandom.current().nextInt((int) (TIME_BETWEEN_SESSIONS * 0.1f), (int)(TIME_BETWEEN_SESSIONS*0.5));
        if (TIME_BETWEEN_SESSIONS <= 0) TIME_BETWEEN_SESSIONS = Long.MAX_VALUE; // get fcked

        plugin.getLogger().info("Next session in " + TIME_BETWEEN_SESSIONS/20L);
        currentTask = plugin.getServer().getScheduler().runTaskLater(
                plugin, this::startGamblingSession, TIME_BETWEEN_SESSIONS
        );
    }


    public void registerPlayerRoll(Player player, int numberRolled){
        if (rollsMap.containsKey(player)) {
            final Component component =
                    text(" You have already rolled! ",
                    NamedTextColor.RED);
            player.sendMessage(component);
            return;
        } else if (!playersRolling.contains(player)){
            final Component component =
                    text(" You are not participating in this session! ",
                            NamedTextColor.YELLOW);
            player.sendMessage(component);
            return;
        }

        final Component component =
                text(player.getName() + " rolled a " + numberRolled,
                        NamedTextColor.GOLD
                );
        plugin.getServer().broadcast(component);
        rollsMap.put(player, numberRolled);
    }

    private void startGamblingSession(){
        if (!gamblingEnabled) return; // just a failsafe

        // Add players participating
        // This allows players to be afk without fear of being killed
        playersRolling.addAll(plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> p.getIdleDuration().toSeconds() <= AFK_TIME).toList());

        // Stop the game if not enough players are playing their fun lil game
        if (playersRolling.size() < MINIMUM_PLAYERS){
            plugin.getLogger().info("Not enough players not AFK! Stopping new session.");
            isRunning = false;
            start();
            return;
        }

        Bukkit.broadcast(Component.text(
                "Let's get gambling! You have " + (TIME_UNTIL_WARN + TIME_AFTER_WARN)/20 + " to seconds use /gamba",
                NamedTextColor.AQUA
        ));

        currentTask = plugin.getServer().getScheduler().runTaskLater(
                plugin, this::warnPlayers, TIME_UNTIL_WARN
        );
    }

    private void warnPlayers(){
        // give mega cancer
        for (Player player : playersRolling) {
            if (!rollsMap.containsKey(player)){
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 777, 7));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 777, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 777, 3));
                final Component component =
                        text("Those who don't roll get punished!",
                                NamedTextColor.RED);
                player.sendMessage(component);
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
            for (Player player : playersRolling) {
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
                for (Player p : playersRolling) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);
                }}, delay);
        }

        for (Player player : playersRolling) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.NAUSEA);
            player.removePotionEffect(PotionEffectType.SLOWNESS);
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> executeLosers(playersToKill), 60L);
    }

    private void executeLosers(ArrayList<Player> playersToKill){
        isRunning = false;
        // Tell the deathListener who we're going to kill
        if (playersRolling.size() < MINIMUM_PLAYERS){
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
        playersRolling.clear();
        plugin.getLogger().info("Stopping the gambler!");
    }

    private void handleEmptyRolls(ArrayList<Player> playersToKill){
        // Add everyone if no one rolled
        plugin.getServer().broadcast(Component.text(
                "No one rolled rolled! Public execution in 3 seconds",
                NamedTextColor.RED
        ));
        playersToKill.addAll(playersRolling);
    }

    public void toggle(CommandSender sender){
        this.gamblingEnabled = !gamblingEnabled;
        sender.sendMessage(Component.text("Gambling is now " + (gamblingEnabled ? "on" : "off"),
                gamblingEnabled ? NamedTextColor.GREEN : NamedTextColor.RED));

        checkGameStatus();
    }

    public void setTimeBetweenSessions(long input, CommandSender sender){
        TIME_BETWEEN_SESSIONS = input * 20L;
        TIME_BETWEEN_SESSIONS_DEF = TIME_BETWEEN_SESSIONS;
        plugin.getConfig().set("root.time_between_sessions", input);
        plugin.saveConfig();
        sender.sendMessage(Component.text("Time between sessions changed to " + input + " seconds!",
                NamedTextColor.GRAY));
    }

    public void setTimeUntilWarn(long input, CommandSender sender) {
        TIME_UNTIL_WARN = input * 20L;
        plugin.getConfig().set("root.time_until_warn", input);
        plugin.saveConfig();
        sender.sendMessage(Component.text("Time until warning changed to " + input + " seconds!",
                NamedTextColor.GRAY));
    }

    public void setTimeAfterWarn(long input, CommandSender sender) {
        TIME_AFTER_WARN = input * 20L;
        plugin.getConfig().set("root.time_after_warn", input);
        plugin.saveConfig();

        sender.sendMessage(Component.text("Time to roll after warning changed to " + input + " seconds!",
                NamedTextColor.GRAY));
    }
}
