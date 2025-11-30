package me.nabattis.rollOrDie;

import me.nabattis.rollOrDie.core.commands.CommandRegister;
import me.nabattis.rollOrDie.core.listeners.RollListener;
import me.nabattis.rollOrDie.core.RollingHandler;
import me.nabattis.rollOrDie.core.commands.RollCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class RollOrDie extends JavaPlugin {
    private RollingHandler rollingHandler;
    private RollListener deathListener;
    private CommandRegister commandRegister;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        deathListener = new RollListener(this);
        this.commandRegister = new CommandRegister(this);
        this.rollingHandler = new RollingHandler(this, deathListener);
        getServer().getPluginManager().registerEvents(deathListener, this);
        commandRegister.registerCommands();

        if (getCommand("gamba") != null){
            getCommand("gamba").setExecutor(new RollCommand(rollingHandler));
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (rollingHandler != null){
            rollingHandler.stop();
        }
    }

    public RollingHandler getGamblingHandler(){
        return this.rollingHandler;
    }
}
