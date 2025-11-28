package me.nabattis.gambleOrCancer.core.commands;

import me.nabattis.gambleOrCancer.core.GamblingHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class RollCommand implements CommandExecutor {
    private final GamblingHandler gamblingHandler;


    public RollCommand(GamblingHandler handler){
        this.gamblingHandler = handler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)){
            commandSender.sendMessage("Only players can take their shot at gambling!");
            return true;
        }
        int rolled = ThreadLocalRandom.current().nextInt(21);
        gamblingHandler.registerPlayerRoll(player, rolled);
        return true;
    }
}
