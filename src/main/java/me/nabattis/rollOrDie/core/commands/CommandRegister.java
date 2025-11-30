package me.nabattis.rollOrDie.core.commands;

import com.mojang.brigadier.arguments.LongArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.nabattis.rollOrDie.RollOrDie;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class CommandRegister {
    private final RollOrDie plugin;

    public CommandRegister(RollOrDie plugin){
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

            registerSettingsCommands(commands);
        });
    }

    private void registerSettingsCommands(Commands commands){
        commands.register(literal("rod-settings")
                .requires(source -> source.getSender().hasPermission("minecraft.command.kill"))
                .then(Commands.literal("time_between_sessions")
                        .then(argument("seconds", LongArgumentType.longArg(30))
                                .executes(context -> {
                                long input = context.getArgument("seconds", Long.class);
                                plugin.getGamblingHandler().setTimeBetweenSessions(input, context.getSource().getSender());
                                return 1;
                                })
                        )
                )
                .then(Commands.literal("time_until_warn")
                        .then(argument("seconds", LongArgumentType.longArg(5))
                                .executes(context -> {
                                long input = context.getArgument("seconds", Long.class);
                                plugin.getGamblingHandler().setTimeUntilWarn(input, context.getSource().getSender());
                                return 1;
                                })
                        )
                )
                .then(Commands.literal("time_after_warn")
                        .then(argument("seconds", LongArgumentType.longArg(2))
                                .executes(context -> {
                                    long input = context.getArgument("seconds", Long.class);
                                    plugin.getGamblingHandler().setTimeAfterWarn(input, context.getSource().getSender());
                                    return 1;
                                })
                        )
                )
                .build());
    }
}
