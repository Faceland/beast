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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.PigZombie;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawnHighest(CreatureSpawnEvent event) {
        BeastData data = plugin.getData(event.getEntity().getType());
        if (data == null || event.isCancelled()) {
            return;
        }

        int startingLevel = plugin.getSettings().getInt("config.enabled-worlds." + event.getLocation().getWorld()
                .getName() + ".starting-level", -1);

        if (startingLevel < 0) {
            return;
        }
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
        } else if (event.getEntity() instanceof Wolf) {
            Wolf wolf = (Wolf) event.getEntity();
            wolf.setAngry(true);
        }
        Vec2 pos = new Vec2(event.getLocation().getX(), event.getLocation().getZ());
        Vec2 worldPos = new Vec2(event.getLocation().getWorld().getSpawnLocation().getX(),
                event.getLocation().getWorld().getSpawnLocation().getZ());
        double distanceFromSpawn = pos.distance(worldPos);
        double pow = plugin.getSettings().getInt("config.enabled-worlds." + event.getLocation().getWorld().getName() +
                ".distance-per-level", 150);
        double rankUp = plugin.getSettings().getDouble("config.mob-rankup-chance", 0.1);
        String rankName = "";
        int rank = 0;

        int level = (int) (startingLevel + distanceFromSpawn / pow);
        level += -2 + random.nextInt(5);
        level = Math.max(level, 1);

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            level += 10;
        } else {
            boolean rankingUp = true;
            while (rankingUp) {
                rankingUp = false;
                if (random.nextDouble() < rankUp && rank < 4) {
                    rank++;
                    rankingUp = true;
                }
            }
            switch (rank) {
                case 0:
                    rankName = "";
                    break;
                case 1:
                    rankName = ChatColor.BLUE + "[M]";
                    level += 5;
                    break;
                case 2:
                    rankName = ChatColor.DARK_PURPLE + "[R]";
                    level += 10;
                    break;
                case 3:
                    rankName = ChatColor.RED + "[E]";
                    level += 15;
                    break;
                case 4:
                    rankName = ChatColor.GOLD + "[L]";
                    level += 20;
                    break;
            }
        }

        String name = TextUtils.color(TextUtils.args(
                data.getNameFormat(), new String[][]{{"%level%", String.valueOf(level)}}));
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            name += "*";
        } else {
            name = rankName + " " + name + " " + rankName;
        }

        event.getEntity().setCustomName(name);
        double newMaxHealth = data.getHealthExpression().setVariable("LEVEL", level).evaluate();
        double speed = data.getSpeedExpression().setVariable("LEVEL", level).evaluate();
        event.getEntity().setHealth(Math.min(2, newMaxHealth) / 2);
        event.getEntity().setMaxHealth(newMaxHealth);
        event.getEntity().setHealth(event.getEntity().getMaxHealth());
        event.getEntity().setCanPickupItems(false);
        event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 10, (int) speed,
                false, false));
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
        Bukkit.getLogger().info("Reached data drops");
        if (!data.getDrops().isEmpty()) {
            Bukkit.getLogger().info("data drops is not empty");
            event.getDrops().clear();
            Bukkit.getLogger().info("default drops cleared");
            for (DropData dropData : data.getDrops()) {
                Bukkit.getLogger().info("Looping drops");
                if (random.nextDouble() < dropData.getChance()) {
                    Bukkit.getLogger().info("Data drop material: " + dropData.getMaterial().name());
                    Bukkit.getLogger().info("Data drop chance: " + dropData.getChance());
                    event.getDrops().add(dropData.toItemStack(random));
                    Bukkit.getLogger().info("ADDED!");
                }
            }
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
            mult *= (1D / 3D);
        }
        int level = NumberUtils.toInt(
                CharMatcher.DIGIT.retainFrom(ChatColor.stripColor(event.getEntity().getCustomName())));
        event.setDroppedExp((int) (data.getExperienceExpression().setVariable("LEVEL", level).evaluate() * mult));
    }
}
