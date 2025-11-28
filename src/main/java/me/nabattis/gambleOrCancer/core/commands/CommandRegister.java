package me.nabattis.gambleOrCancer.core.commands;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.nabattis.gambleOrCancer.GambleOrCancer;
import me.nabattis.gambleOrCancer.core.GamblingHandler;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class CommandRegister {
    private final GambleOrCancer plugin;

    public CommandRegister(GambleOrCancer plugin){
        this.plugin = plugin;
    }

    public void registerCommands(){
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                event -> {
            final Commands commands = event.registrar();

            commands.register(
                    literal("togglegamba")
                            .requires(source -> source.getSender().hasPermission("minecraft.command.kill"))
                            .executes(context -> {
                                plugin.getGamblingHandler().toggle(context.getSource().getSender());
                                return 1;
                            })
                            .build(),
                    "Toggles the gambling"
            );
        });


    }

}
