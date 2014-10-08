/******************************************************************************
 * Copyright (c) 2014, Richard Harrah                                         *
 *                                                                            *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

package info.faceland.beast;

import info.faceland.api.FacePlugin;
import info.faceland.facecore.shade.nun.ivory.config.VersionedIvoryConfiguration;
import info.faceland.facecore.shade.nun.ivory.config.VersionedIvoryYamlConfiguration;
import info.faceland.facecore.shade.nun.ivory.config.settings.IvorySettings;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class BeastPlugin extends FacePlugin {

    private Map<EntityType, BeastData> beastDataMap;
    private VersionedIvoryYamlConfiguration configYAML;
    private VersionedIvoryYamlConfiguration monstersYAML;
    private VersionedIvoryYamlConfiguration replacementsYAML;
    private IvorySettings settings;

    @Override
    public void preEnable() {
        beastDataMap = new HashMap<>();

        configYAML = new VersionedIvoryYamlConfiguration(new File(getDataFolder(), "config.yml"),
                                                         getResource("config.yml"),
                                                         VersionedIvoryConfiguration.VersionUpdateType
                                                                 .BACKUP_AND_UPDATE);
        if (configYAML.update()) {
            getLogger().info("Updating config.yml");
        }
        monstersYAML = new VersionedIvoryYamlConfiguration(new File(getDataFolder(), "monsters.yml"),
                                                           getResource("monsters.yml"),
                                                           VersionedIvoryConfiguration.VersionUpdateType
                                                                   .BACKUP_AND_UPDATE);
        if (monstersYAML.update()) {
            getLogger().info("Updating monsters.yml");
        }
        replacementsYAML = new VersionedIvoryYamlConfiguration(new File(getDataFolder(), "replacements.yml"),
                                                               getResource("replacements.yml"),
                                                               VersionedIvoryConfiguration.VersionUpdateType
                                                                       .BACKUP_AND_UPDATE);
        if (replacementsYAML.update()) {
            getLogger().info("Updating replacements.yml");
        }

        settings = new IvorySettings();
    }

    @Override
    public void enable() {
        settings.load(configYAML, replacementsYAML, monstersYAML);
    }

    @Override
    public void postEnable() {

    }

    @Override
    public void preDisable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public void postDisable() {
        beastDataMap = null;
    }

}
