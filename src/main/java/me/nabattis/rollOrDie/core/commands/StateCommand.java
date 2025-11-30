package me.nabattis.rollOrDie.core.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.nabattis.rollOrDie.core.RollingHandler;
import org.jspecify.annotations.Nullable;

public class StateCommand implements BasicCommand {
    private RollingHandler rollingHandler;

    public StateCommand(RollingHandler gh){
        this.rollingHandler = gh;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] strings) {

    }

    @Override
    public @Nullable String permission() {
        return "minecraft.command.kill";
    }
}
