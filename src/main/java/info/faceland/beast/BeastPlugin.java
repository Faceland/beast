/*
 * This file is part of Mint, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package info.faceland.beast;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.nunnerycode.facecore.configuration.MasterConfiguration;
import org.nunnerycode.facecore.configuration.VersionedSmartConfiguration.VersionUpdateType;
import org.nunnerycode.facecore.configuration.VersionedSmartYamlConfiguration;
import org.nunnerycode.facecore.plugin.FacePlugin;
import org.nunnerycode.kern.apache.commons.lang3.math.NumberUtils;
import org.nunnerycode.kern.objecthunter.exp4j.ExpressionBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BeastPlugin extends FacePlugin {

    private Map<EntityType, BeastData> beastDataMap;
    private VersionedSmartYamlConfiguration configYAML;
    private VersionedSmartYamlConfiguration monstersYAML;
    private VersionedSmartYamlConfiguration replacementsYAML;
    private MasterConfiguration settings;

    @Override
    public void enable() {
        beastDataMap = new HashMap<>();

        configYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
                getResource("config.yml"),
                VersionUpdateType.BACKUP_AND_UPDATE);
        if (configYAML.update()) {
            getLogger().info("Updating config.yml");
        }
        monstersYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "monsters.yml"),
                getResource("monsters.yml"),
                VersionUpdateType.BACKUP_AND_UPDATE);
        if (monstersYAML.update()) {
            getLogger().info("Updating monsters.yml");
        }
        replacementsYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "replacements.yml"),
                getResource("replacements.yml"),
                VersionUpdateType.BACKUP_AND_UPDATE);
        if (replacementsYAML.update()) {
            getLogger().info("Updating replacements.yml");
        }

        settings = new MasterConfiguration();

        settings.load(configYAML, replacementsYAML, monstersYAML);

        for (String key : monstersYAML.getKeys(false)) {
            if (!monstersYAML.isConfigurationSection(key)) {
                continue;
            }
            EntityType entityType = EntityType.valueOf(key);
            if (entityType == null) {
                continue;
            }
            ConfigurationSection cs = monstersYAML.getConfigurationSection(key);
            BeastData data = new BeastData(entityType);
            data.setNameFormat(cs.getString("name"));
            String healthExpStr = cs.getString("health");
            String damageExpStr = cs.getString("damage");
            String experienceExpStr = cs.getString("experience");
            data.setHealthExpression(new ExpressionBuilder(healthExpStr).variables("LEVEL").build());
            data.setDamageExpression(new ExpressionBuilder(damageExpStr).variables("LEVEL").build());
            data.setExperienceExpression(new ExpressionBuilder(experienceExpStr).variables("LEVEL").build());
            if (cs.isConfigurationSection("potion-effects")) {
                Map<Integer, List<PotionEffect>> effects = new HashMap<>();
                ConfigurationSection peCS = cs.getConfigurationSection("potion-effects");
                for (String k : peCS.getKeys(false)) {
                    List<String> list = peCS.getStringList(k);
                    List<PotionEffect> pe = new ArrayList<>();
                    for (String s : list) {
                        PotionEffect pot = parsePotionEffect(s);
                        if (pot != null) {
                            pe.add(pot);
                        }
                    }
                    effects.put(NumberUtils.toInt(k), pe);
                }
                data.setPotionEffectMap(effects);
            }
            if (cs.isConfigurationSection("drops")) {
                List<DropData> drops = new ArrayList<>();
                ConfigurationSection dCS = cs.getConfigurationSection("drops");
                for (String k : dCS.getKeys(false)) {
                    if (!dCS.isConfigurationSection(k)) {
                        continue;
                    }
                    ConfigurationSection inner = dCS.getConfigurationSection(k);
                    DropData dropData = new DropData(Material.getMaterial(k), inner.getInt("min-amount"),
                                                     inner.getInt("max-amount"), inner.getDouble("chance"));
                    if (dropData.getMaterial() == Material.AIR) {
                        continue;
                    }
                    drops.add(dropData);
                }
                data.setDrops(drops);
            }
            beastDataMap.put(entityType, data);
        }

        Bukkit.getPluginManager().registerEvents(new BeastListener(this), this);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        settings = null;
        replacementsYAML = null;
        monstersYAML = null;
        configYAML = null;
        beastDataMap = null;
    }

    private PotionEffect parsePotionEffect(String s) {
        String[] spl = s.split(":");
        if (spl.length < 2) {
            return null;
        }
        PotionEffectType type = PotionEffectType.getByName(spl[0]);
        if (type == null) {
            return null;
        }
        int i = NumberUtils.toInt(spl[1]);
        return new PotionEffect(type, 20 * 5, i);
    }

    public MasterConfiguration getSettings() {
        return settings;
    }

    public BeastData getData(EntityType type) {
        if (beastDataMap.containsKey(type)) {
            return beastDataMap.get(type);
        }
        return null;
    }

}
