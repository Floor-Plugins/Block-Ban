package com.floorplugins.block_ban;

import org.bukkit.Material;

import java.util.Map;

public class WorldConfig {
    Map<Material, String> bannedBlocks;
    String worldBannedMessage;

    WorldConfig(String worldBannedMessage, Map<Material, String> bannedBlocks) {
        this.worldBannedMessage = worldBannedMessage;
        this.bannedBlocks = bannedBlocks;
    }
}
