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

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;

import net.elseland.xikage.MythicMobs.Mobs.ActiveMobHandler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public final class BeastListener implements Listener {

    private final BeastPlugin plugin;
    private final Random random;

    public BeastListener(BeastPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCreatureSpawnHighest(CreatureSpawnEvent event) {
        if (!ActiveMobHandler.isRegisteredMob(event.getEntity().getUniqueId())) {
            return;
        }
        BeastData data = plugin.getData(event.getEntity().getType());
        if (data == null || event.isCancelled()) {
            return;
        }

        int startingLevel = plugin.getSettings().getInt("config.enabled-worlds." + event.getLocation().getWorld()
                .getName() + ".starting-level", -1);

        if (startingLevel < 0) {
            return;
        }
        double healthMult = 1.0;
        int rank = 0;
        int level = 0;
        if (event.getEntity().getCustomName() == null) {
            Vec2 pos = new Vec2(event.getLocation().getX(), event.getLocation().getZ());
            Vec2 worldPos = new Vec2(event.getLocation().getWorld().getSpawnLocation().getX(),
                    event.getLocation().getWorld().getSpawnLocation().getZ());
            double distanceFromSpawn = pos.distance(worldPos);
            double pow = plugin.getSettings().getInt("config.enabled-worlds." + event.getLocation().getWorld().getName() +
                    ".distance-per-level", 150);
            level = (int) (startingLevel + distanceFromSpawn / pow);
            level += -2 + random.nextInt(5);
            level = Math.max(level, 1);

            event.getEntity().getEquipment().clear();
            if (event.getEntity() instanceof PigZombie) {
                event.getEntity().getEquipment().setHelmet(new ItemStack(Material.GOLD_HELMET));
                if (random.nextDouble() < 0.5) {
                    event.getEntity().getEquipment().setItemInHand(new ItemStack(Material.GOLD_AXE));
                } else {
                    event.getEntity().getEquipment().setItemInHand(new ItemStack(Material.GOLD_SWORD));
                }
                event.getEntity().getEquipment().setItemInHandDropChance(0f);
                event.getEntity().getEquipment().setHelmetDropChance(0f);
            } else if (event.getEntity() instanceof Skeleton) {
                if (random.nextDouble() < plugin.getSettings().getDouble("config.give-skeletons-sword-chance", 0.1)) {
                    event.getEntity().getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD));
                } else {
                    event.getEntity().getEquipment().setItemInHand(new ItemStack(Material.BOW));
                }
                event.getEntity().getEquipment().setItemInHandDropChance(0f);
            } else if (event.getEntity() instanceof Slime) {
                Slime slime = (Slime) event.getEntity();
                healthMult = slime.getSize() / 4;
            } else if (event.getEntity() instanceof Wolf) {
                Wolf wolf = (Wolf) event.getEntity();
                wolf.setAngry(true);
            }

            double rankUp = plugin.getSettings().getDouble("config.mob-rankup-chance", 0.1);
            String rankName = "";

            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                rankName = ChatColor.WHITE + "Spawned ";
                level += 7;
            } else {
                while (random.nextDouble() < rankUp && rank < 4) {
                    rank++;
                }
                switch (rank) {
                    case 0:
                        rankName = "";
                        break;
                    case 1:
                        rankName = ChatColor.BLUE + "Magic ";
                        event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 9999, 1, false, false));
                        break;
                    case 2:
                        rankName = ChatColor.DARK_PURPLE + "Rare ";
                        event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 9999, 1, false, false));
                        break;
                    case 3:
                        rankName = ChatColor.RED + "Epic ";
                        event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 9999, 1, false, false));
                        break;
                    case 4:
                        rankName = ChatColor.GOLD + "Legendary ";
                        event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 9999, 1, false, false));
                        break;
                }
            }

            String name = TextUtils.color(TextUtils.args(data.getNameFormat(), new String[][]{{"%level%", String.valueOf(level)}}));
            name = rankName + name;

            event.getEntity().setCustomName(name);
        } else {
            level = NumberUtils.toInt(CharMatcher.DIGIT.retainFrom(ChatColor.stripColor(event.getEntity()
                    .getCustomName())));
            if (event.getEntity().getCustomName().startsWith(ChatColor.DARK_RED + "Boss")) {
                rank = 8;
                event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 9999, 1, false, false));
            }
        }

        double newMaxHealth = healthMult * (1 + (rank * 0.75)) * data.getHealthExpression().setVariable("LEVEL", level)
                .evaluate();
        double speed = data.getSpeedExpression().setVariable("LEVEL", level).evaluate();
        event.getEntity().setHealth(Math.min(2, newMaxHealth) / 2);
        event.getEntity().setMaxHealth(newMaxHealth);
        event.getEntity().setHealth(event.getEntity().getMaxHealth());
        event.getEntity().setCanPickupItems(false);
        event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 10, (int) speed,
                false, false));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (event instanceof PlayerDeathEvent) {
            return;
        }
        BeastData data = plugin.getData(event.getEntityType());
        if (data == null) {
            return;
        }
        if (event.getEntity().getCustomName() == null) {
            return;
        }
        if (!data.getDrops().isEmpty()) {
            event.getDrops().clear();
            for (DropData dropData : data.getDrops()) {
                if (random.nextDouble() < dropData.getChance()) {
                    event.getDrops().add(dropData.toItemStack(random));
                }
            }
        }
        EntityDamageEvent.DamageCause damageCause = event.getEntity().getLastDamageCause().getCause();
        if (event.getEntity().getKiller() == null && damageCause != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                damageCause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;
        }
        double mult = 1D;
        if (event.getEntity().getCustomName().startsWith(ChatColor.WHITE + "Spawned")) {
            mult *= 0.4D;
        }
        int level = NumberUtils.toInt(CharMatcher.DIGIT.retainFrom(ChatColor.stripColor(event.getEntity().getCustomName())));
        event.setDroppedExp((int)(data.getExperienceExpression().setVariable("LEVEL", level).evaluate() * mult));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathAutoOrb(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        World w = event.getEntity().getWorld();
        Entity e = w.spawnEntity(event.getEntity().getKiller().getLocation(), EntityType.EXPERIENCE_ORB);
        ((ExperienceOrb)e).setExperience(event.getDroppedExp());
        event.setDroppedExp(0);
    }
}
