package me.nabattis.gambleOrCancer;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.nabattis.gambleOrCancer.core.commands.CommandRegister;
import me.nabattis.gambleOrCancer.core.listeners.GamblingListener;
import me.nabattis.gambleOrCancer.core.GamblingHandler;
import me.nabattis.gambleOrCancer.core.commands.RollCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class GambleOrCancer extends JavaPlugin {
    private GamblingHandler gamblingHandler;
    private GamblingListener deathListener;
    private CommandRegister commandRegister;

    @Override
    public void onEnable() {
        // Plugin startup logic
        deathListener = new GamblingListener(this);
        this.commandRegister = new CommandRegister(this);
        this.gamblingHandler = new GamblingHandler(this, deathListener);
        getServer().getPluginManager().registerEvents(deathListener, this);
        commandRegister.registerCommands();

        if (getCommand("roll") != null){
            getCommand("roll").setExecutor(new RollCommand(gamblingHandler));
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (gamblingHandler != null){
            gamblingHandler.stop();
        }
    }

    public GamblingHandler getGamblingHandler(){
        return this.gamblingHandler;
    }
}
