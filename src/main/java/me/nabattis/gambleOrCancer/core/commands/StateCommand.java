package me.nabattis.gambleOrCancer.core.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.nabattis.gambleOrCancer.core.GamblingHandler;
import org.jspecify.annotations.Nullable;

public class StateCommand implements BasicCommand {
    private GamblingHandler gamblingHandler;

    public StateCommand(GamblingHandler gh){
        this.gamblingHandler = gh;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] strings) {

    }

    @Override
    public @Nullable String permission() {
        return "minecraft.command.kill";
    }
}
