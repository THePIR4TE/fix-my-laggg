package net.thepir4te.fixmylaggg.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.thepir4te.fixmylaggg.config.ModConfig;
import net.thepir4te.fixmylaggg.manager.FakeFramerateManager;
import org.lwjgl.glfw.GLFW;

public class FIXMYLAGGGClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModConfig config = ModConfig.getInstance();
        config.load();

        FakeFramerateManager manager = FakeFramerateManager.getInstance();
        manager.setTargetFramerate(config.targetFakeFramerate);
        manager.setRealisticSinkEnabled(config.realisticSinkEnabled);
        manager.setRealisticSinkThreshold(config.realisticSinkThreshold);

        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("fix-my-laggg", "main")
        );

        KeyMapping guiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.fix-my-laggg.settings",
            GLFW.GLFW_KEY_F10,
            category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (guiKey.consumeClick()) {
                client.gui.setScreen(new FixLagConfigScreen());
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("fixlag")
                .executes(FIXMYLAGGGClient::executeHelp)
                .then(ClientCommands.literal("help")
                    .executes(FIXMYLAGGGClient::executeHelp))
                .then(ClientCommands.literal("status")
                    .executes(FIXMYLAGGGClient::executeStatus))
                .then(ClientCommands.literal("reload")
                    .executes(FIXMYLAGGGClient::executeReload))
                .then(ClientCommands.literal("gui")
                    .executes(FIXMYLAGGGClient::executeGui))
                .then(ClientCommands.literal("set")
                    .then(ClientCommands.argument("key", StringArgumentType.word())
                        .suggests(KEY_SUGGESTIONS)
                        .then(ClientCommands.argument("value", StringArgumentType.word())
                            .suggests(VALUE_SUGGESTIONS)
                            .executes(FIXMYLAGGGClient::executeSet))))
            );
        });
    }

    private static int executeGui(CommandContext<FabricClientCommandSource> context) {
        Minecraft.getInstance().gui.setScreen(new FixLagConfigScreen());
        return Command.SINGLE_SUCCESS;
    }

    private static final SuggestionProvider<FabricClientCommandSource> KEY_SUGGESTIONS = (context, builder) -> {
        builder.suggest("fps");
        builder.suggest("sink");
        builder.suggest("threshold");
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> VALUE_SUGGESTIONS = (context, builder) -> {
        String key = StringArgumentType.getString(context, "key");
        switch (key) {
            case "fps" -> {
                builder.suggest("60"); builder.suggest("120"); builder.suggest("240");
                builder.suggest("500"); builder.suggest("1000"); builder.suggest("10000");
            }
            case "sink" -> { builder.suggest("true"); builder.suggest("false"); }
            case "threshold" -> {
                builder.suggest("0.5"); builder.suggest("0.7"); builder.suggest("0.85");
                builder.suggest("0.95"); builder.suggest("1.0");
            }
        }
        return builder.buildFuture();
    };

    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.help.title"));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.help.set_fps"));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.help.set_sink"));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.help.set_threshold"));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.help.reload"));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.help.gui"));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.help.status"));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.help.help"));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.help.keybind"));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeStatus(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = ModConfig.getInstance();
        FakeFramerateManager manager = FakeFramerateManager.getInstance();
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.status.title"));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.status.target", config.targetFakeFramerate));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.status.sink",
            Component.translatable(config.realisticSinkEnabled ? "gui.fix-my-laggg.on" : "gui.fix-my-laggg.off")));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.status.threshold", config.realisticSinkThreshold));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.status.real_fps", manager.getRealFps()));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.status.fake_fps", manager.getCurrentFakeFps()));
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.status.footer"));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeReload(CommandContext<FabricClientCommandSource> context) {
        ModConfig config = ModConfig.getInstance();
        FakeFramerateManager manager = FakeFramerateManager.getInstance();
        config.load();
        manager.setTargetFramerate(config.targetFakeFramerate);
        manager.setRealisticSinkEnabled(config.realisticSinkEnabled);
        manager.setRealisticSinkThreshold(config.realisticSinkThreshold);
        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.reload"));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeSet(CommandContext<FabricClientCommandSource> context) {
        String key = StringArgumentType.getString(context, "key");
        String value = StringArgumentType.getString(context, "value");
        ModConfig config = ModConfig.getInstance();
        FakeFramerateManager manager = FakeFramerateManager.getInstance();

        try {
            switch (key.toLowerCase()) {
                case "fps" -> {
                    int fps = Integer.parseInt(value);
                    if (fps < 1 || fps > 12000) {
                        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.error.fps_range"));
                        return 0;
                    }
                    config.targetFakeFramerate = fps;
                    manager.setTargetFramerate(fps);
                    config.save();
                    context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.set.fps", fps));
                }
                case "sink" -> {
                    boolean enabled = Boolean.parseBoolean(value);
                    config.realisticSinkEnabled = enabled;
                    manager.setRealisticSinkEnabled(enabled);
                    config.save();
                    context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.set.sink",
                        Component.translatable(enabled ? "gui.fix-my-laggg.on" : "gui.fix-my-laggg.off")));
                }
                case "threshold" -> {
                    float threshold = Float.parseFloat(value);
                    if (threshold < 0.0f || threshold > 1.0f) {
                        context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.error.threshold_range"));
                        return 0;
                    }
                    config.realisticSinkThreshold = threshold;
                    manager.setRealisticSinkThreshold(threshold);
                    config.save();
                    context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.set.threshold", threshold));
                }
                default -> {
                    context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.error.unknown_key", key));
                    executeHelp(context);
                }
            }
        } catch (NumberFormatException e) {
            context.getSource().sendFeedback(Component.translatable("cmd.fix-my-laggg.error.invalid_value", value));
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }
}
