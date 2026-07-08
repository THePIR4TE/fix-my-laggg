package net.thepir4te.fixmylaggg.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.thepir4te.fixmylaggg.FIXMYLAGGG;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final String CONFIG_FILE_NAME = "fixmylaggg-config.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig INSTANCE;

    public int targetFakeFramerate = 500;
    public boolean realisticSinkEnabled = true;
    public float realisticSinkThreshold = 0.85f;
    public boolean cloudSyncEnabled = false;
    public String cloudApiEndpoint = "";

    public static synchronized ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModConfig();
            INSTANCE.load();
        }
        return INSTANCE;
    }

    public void load() {
        Path configPath = getConfigPath();
        try {
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath, StandardCharsets.UTF_8);
                ModConfig loadedConfig = GSON.fromJson(json, ModConfig.class);
                if (loadedConfig != null) {
                    this.targetFakeFramerate = loadedConfig.targetFakeFramerate;
                    this.realisticSinkEnabled = loadedConfig.realisticSinkEnabled;
                    this.realisticSinkThreshold = loadedConfig.realisticSinkThreshold;
                    this.cloudSyncEnabled = loadedConfig.cloudSyncEnabled;
                    this.cloudApiEndpoint = loadedConfig.cloudApiEndpoint;
                }
            } else {
                save();
            }
        } catch (Exception e) {
            FIXMYLAGGG.LOGGER.warn("Failed to load config, using defaults: " + e.getMessage());
            save();
        }
    }

    public void save() {
        Path configPath = getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(this);
            Files.writeString(configPath, json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            FIXMYLAGGG.LOGGER.error("Failed to save config: " + e.getMessage());
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    }
}
