/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.beast;

import com.tealcube.minecraft.bukkit.config.MasterConfiguration;
import com.tealcube.minecraft.bukkit.config.VersionedConfiguration;
import com.tealcube.minecraft.bukkit.config.VersionedSmartYamlConfiguration;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.Lorinth.BossApi.BossApi;

public final class BeastPlugin extends FacePlugin {

    private Map<EntityType, BeastData> beastDataMap;
    private VersionedSmartYamlConfiguration configYAML;
    private VersionedSmartYamlConfiguration monstersYAML;
    private MasterConfiguration settings;

    private BossApi api;

    @Override
    public void enable() {
        beastDataMap = new HashMap<>();

        configYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
                getResource("config.yml"),
                VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
        if (configYAML.update()) {
            getLogger().info("Updating config.yml");
        }
        monstersYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "monsters.yml"),
                getResource("monsters.yml"),
                VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
        if (monstersYAML.update()) {
            getLogger().info("Updating monsters.yml");
        }

        api = BossApi.getPlugin();

        settings = new MasterConfiguration();

        settings.load(configYAML, monstersYAML);

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
            String healthExpStr = cs.getString("health", "1");
            String damageExpStr = cs.getString("damage", "1");
            String experienceExpStr = cs.getString("experience", "1");
            String movementSpeedExpStr = cs.getString("movement-speed", "1");
            data.setHealthExpression(new ExpressionBuilder(healthExpStr).variables("LEVEL").build());
            data.setDamageExpression(new ExpressionBuilder(damageExpStr).variables("LEVEL").build());
            data.setExperienceExpression(new ExpressionBuilder(experienceExpStr).variables("LEVEL").build());
            data.setSpeedExpression(new ExpressionBuilder(movementSpeedExpStr).variables("LEVEL").build());
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
                    DropData dropData = new DropData();
                    Material material = Material.getMaterial(inner.getString("material"));
                    int minimumAmount = inner.getInt("min-amount");
                    int maximumAmount = inner.getInt("max-amount");
                    double chance = inner.getDouble("chance");
                    String name = inner.getString("name");
                    List<String> lore = inner.getStringList("lore");
                    Map<Enchantment, Integer> enchantmentMap = new HashMap<>();
                    if (inner.isConfigurationSection("enchantments")) {
                        ConfigurationSection enchants = inner.getConfigurationSection("enchantments");
                        for (String s : enchants.getKeys(false)) {
                            enchantmentMap.put(Enchantment.getByName(s), enchants.getInt(s));
                        }
                    }
                    dropData.setMaterial(material).setMinimumAmount(minimumAmount).setMaximumAmount(maximumAmount)
                            .setChance(chance).setName(name).setLore(lore).setEnchantmentMap(enchantmentMap);
                    getLogger().info(entityType.name() + " : " + k + " : " + material);
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
        Bukkit.getPluginManager().registerEvents(new EliteAbilities(this), this);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        api = null;
        settings = null;
        monstersYAML = null;
        configYAML = null;
        beastDataMap = null;
    }

    public BossApi getApi() {
        return api;
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
