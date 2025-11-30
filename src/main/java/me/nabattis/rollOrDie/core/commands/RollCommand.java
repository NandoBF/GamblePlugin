package me.nabattis.rollOrDie.core.commands;

import me.nabattis.rollOrDie.core.RollingHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class RollCommand implements CommandExecutor {
    private final RollingHandler rollingHandler;


    public RollCommand(RollingHandler handler){
        this.rollingHandler = handler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)){
            commandSender.sendMessage("Only players can take their shot at gambling!");
            return true;
        }
        int rolled = ThreadLocalRandom.current().nextInt(1,21);
        rollingHandler.registerPlayerRoll(player, rolled);
        return true;
    }
}
