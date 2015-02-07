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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Skeleton;
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
import org.nunnerycode.facecore.reflect.ClassType;
import org.nunnerycode.facecore.reflect.Mirror;
import org.nunnerycode.facecore.utilities.TextUtils;
import org.nunnerycode.kern.apache.commons.lang3.math.NumberUtils;
import org.nunnerycode.kern.shade.google.common.base.CharMatcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.UUID;

public final class BeastListener implements Listener {

    private final BeastPlugin plugin;
    private final Random random;
    private static final UUID MOVEMENT_SPEED_UUID = UUID.randomUUID();

    public BeastListener(BeastPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreatureSpawnLowest(final CreatureSpawnEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Monster) ||
            event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }
        final EntityType replacementType = EntityType.valueOf(
                plugin.getSettings().getString("replacements." + event.getEntity().getType().name() + "." +
                                               event.getLocation().getBlock().getBiome().name(),
                                               event.getEntity().getType().name()));
        if (replacementType == event.getEntity().getType() || replacementType == null) {
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                event.getLocation().getWorld().spawnEntity(event.getEntity().getLocation(), replacementType);
            }
        });
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawnHighest(CreatureSpawnEvent event) {
        BeastData data = plugin.getData(event.getEntity().getType());
        if (data == null || event.isCancelled() || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }
        int startingLevel =
                plugin.getSettings().getInt("config.enabled-worlds." + event.getLocation().getWorld().getName() +
                                            ".starting-level", -1);
        if (startingLevel < 0) {
            return;
        }
        event.getEntity().getEquipment().clear();
        if (event.getEntity() instanceof Skeleton) {
            if (random.nextDouble() < plugin.getSettings().getDouble("config.give-skeletons-sword-chance", 0.1)) {
                event.getEntity().getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD));
            } else {
                event.getEntity().getEquipment().setItemInHand(new ItemStack(Material.BOW));
            }
            event.getEntity().getEquipment().setItemInHandDropChance(0f);
        }
        Vec2 pos = new Vec2(event.getLocation().getX(), event.getLocation().getZ());
        Vec2 worldPos = new Vec2(event.getLocation().getWorld().getSpawnLocation().getX(),
                                 event.getLocation().getWorld().getSpawnLocation().getZ());
        double distanceFromSpawn = pos.distance(worldPos);
        double pow = plugin.getSettings().getInt("config.enabled-worlds." + event.getLocation().getWorld().getName() +
                                                 ".distance-per-level", 150);
        int level = (int) (startingLevel + distanceFromSpawn / pow);
        event.getEntity().setCustomName(TextUtils.color(TextUtils.args(
                data.getNameFormat(), new String[][]{{"%level%", String.valueOf(level)}})));
        double currentMaxHealth = event.getEntity().getMaxHealth();
        double newMaxHealth = data.getHealthExpression().setVariable("LEVEL", level).evaluate();
        double speed = data.getSpeedExpression().setVariable("LEVEL", level).evaluate();
        event.getEntity().setHealth(Math.min(currentMaxHealth, newMaxHealth) / 2);
        event.getEntity().setMaxHealth(newMaxHealth);
        event.getEntity().setHealth(event.getEntity().getMaxHealth());
        event.getEntity().setCanPickupItems(false);
        event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 10, (int) Math.ceil(speed),
                false, false));
        if (event.getEntity() instanceof Wolf) {
            ((Wolf) event.getEntity()).setAngry(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
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
        double mult = 1D;
        if (event.getEntity().getLastDamageCause() == null) {
            return;
        }
        EntityDamageEvent.DamageCause cause = event.getEntity().getLastDamageCause().getCause();
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            mult = 0.2D;
        }
        int level = NumberUtils.toInt(
                CharMatcher.DIGIT.retainFrom(ChatColor.stripColor(event.getEntity().getCustomName())));
        event.setDroppedExp((int) (data.getExperienceExpression().setVariable("LEVEL", level).evaluate() * mult));
        if (data.getDrops().isEmpty()) {
            return;
        }
        event.getDrops().clear();
        for (DropData dropData : data.getDrops()) {
            if (random.nextDouble() < dropData.getChance()) {
                int amount = dropData.getMinimumAmount() +
                             (int) (random.nextDouble() * (dropData.getMaximumAmount() - dropData.getMinimumAmount()));
                if (amount <= 0 || dropData.getMaterial() == Material.AIR) {
                    continue;
                }
                event.getDrops().add(new ItemStack(dropData.getMaterial(), amount));
            }
        }
    }
}
