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

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.kern.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.kern.shade.google.common.base.CharMatcher;
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

import java.util.Random;

public final class BeastListener implements Listener {

    private final BeastPlugin plugin;
    private final Random random;

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
        ReplacementData data = plugin.getReplacementDataTable().get(event.getEntity().getType(),
                event.getLocation().getBlock().getBiome());
        if (data == null) {
            return;
        }
        int startingLevel =
                plugin.getSettings().getInt("config.enabled-worlds." + event.getLocation().getWorld().getName() +
                        ".starting-level", -1);
        if (startingLevel < 0) {
            return;
        }
        Vec2 pos = new Vec2(event.getLocation().getX(), event.getLocation().getZ());
        Vec2 worldPos = new Vec2(event.getLocation().getWorld().getSpawnLocation().getX(),
                event.getLocation().getWorld().getSpawnLocation().getZ());
        double distanceFromSpawn = pos.distance(worldPos);
        double pow = plugin.getSettings().getInt("config.enabled-worlds." + event.getLocation().getWorld().getName() +
                ".distance-per-level", 150);
        int level = (int) (startingLevel + distanceFromSpawn / pow);
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            level += 10;
        }
        final ReplacementData.SubReplacementData subdata = data.getRandomSubReplacementData(level, random);
        if (subdata != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < subdata.getAmount(); i++) {
                        subdata.spawnAtLocation(event.getLocation());
                    }
                }
            });
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawnHighest(CreatureSpawnEvent event) {
        BeastData data = plugin.getData(event.getEntity().getType());
        if (data == null || event.isCancelled()) {
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
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            level += 10;
        }
        String name = TextUtils.color(TextUtils.args(
                data.getNameFormat(), new String[][]{{"%level%", String.valueOf(level)}}));
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            name += "*";
        }
        event.getEntity().setCustomName(name);
        double currentMaxHealth = event.getEntity().getMaxHealth();
        double newMaxHealth = data.getHealthExpression().setVariable("LEVEL", level).evaluate();
        double speed = data.getSpeedExpression().setVariable("LEVEL", level).evaluate();
        event.getEntity().setHealth(Math.min(currentMaxHealth, newMaxHealth) / 2);
        event.getEntity().setMaxHealth(newMaxHealth);
        event.getEntity().setHealth(event.getEntity().getMaxHealth());
        event.getEntity().setCanPickupItems(false);
        event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 10, (int) speed,
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
            mult *= 0.2D;
        }
        if (event.getEntity().getCustomName().endsWith("*")) {
            mult *= (1D/3D);
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
                if (dropData.getMaterial() == Material.AIR) {
                    continue;
                }
                event.getDrops().add(dropData.toItemStack(random));
            }
        }
    }
}
