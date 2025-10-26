package com.floorplugins.block_ban;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class BlockBan extends JavaPlugin implements Listener {
    private final Map<String, WorldConfig> worldConfigs = new HashMap<>();
    private String globalBannedMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void loadConfigValues() {
        FileConfiguration cfg = getConfig();
        globalBannedMessage = cfg.getString("default-banned-message", "<red>You cannot place that block!</red>");
        ConfigurationSection worlds = cfg.getConfigurationSection("worlds");

        if (worlds != null) {
            for (String worldName : worlds.getKeys(false)) {
                ConfigurationSection worldSection = worlds.getConfigurationSection(worldName);

                if (worldSection != null) {
                    String worldMessage = worldSection.getString("banned-message", null);
                    ConfigurationSection bannedBlocksSection = worldSection.getConfigurationSection("banned-blocks");
                    Map<Material, String> bannedBlocks = new HashMap<>();

                    if (bannedBlocksSection != null) {
                        for (String blockName : bannedBlocksSection.getKeys(false)) {
                            Material mat = Material.getMaterial(blockName.toUpperCase());

                            if (mat != null) {
                                String blockMsg = bannedBlocksSection.getConfigurationSection(blockName).getString("banned-message", null);
                                bannedBlocks.put(mat, blockMsg);
                            }
                        }
                    }

                    worldConfigs.put(worldName, new WorldConfig(worldMessage, bannedBlocks));
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Material placed = event.getBlockPlaced().getType();
        String worldName = event.getBlock().getWorld().getName();
        WorldConfig worldConfig = worldConfigs.get(worldName);
        WorldConfig serverConfig = worldConfigs.get("__server__");
        boolean isBanned = false;
        String message = null;

        if (worldConfig != null && worldConfig.bannedBlocks.containsKey(placed)) {
            isBanned = true;
            message = worldConfig.bannedBlocks.get(placed);

            if (message == null)
                message = worldConfig.worldBannedMessage;
        } else if (serverConfig != null && serverConfig.bannedBlocks.containsKey(placed)) {
            isBanned = true;
            message = serverConfig.bannedBlocks.get(placed);

            if (message == null)
                message = globalBannedMessage;
        }

        if (isBanned) {
            event.setCancelled(true);
            BlockState previous = event.getBlockReplacedState();
            event.getBlock().setType(previous.getType(), false);

            if (message == null)
                message = globalBannedMessage;

            MiniMessage mm = MiniMessage.miniMessage();
            Component msg = mm.deserialize(message);
            event.getPlayer().sendMessage(msg);
        }
    }
}
