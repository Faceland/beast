/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.beast.listeners;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.beast.BeastData;
import info.faceland.beast.BeastPlugin;
import info.faceland.beast.DropData;
import info.faceland.beast.Vec2;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class SpawnListener implements Listener {

    private final BeastPlugin plugin;
    private final Random random;

    private ItemStack skeletonSword;
    private ItemStack skeletonWand;
    private ItemStack witchHat;

    public SpawnListener(BeastPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random(System.currentTimeMillis());
        skeletonSword = buildSkeletonSword();
        skeletonWand = buildSkeletonWand();
        witchHat = buildWitchHat();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpawnerSpawnAnimals(SpawnerSpawnEvent event) {
        if (event.getEntity() instanceof Animals && plugin.getSettings()
            .getDouble("config.animal-spawner-cancel-chance", 0) < random.nextDouble()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawnHighest(CreatureSpawnEvent event) {
        if (plugin.getApi().isBoss(event.getEntity())) {
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

        if (event.getEntity() instanceof Witch) {
            if (random.nextDouble() < plugin.getSettings().getDouble("config.replace-witch-evoker", 0.1)) {
                event.getEntity().getWorld().spawnEntity(event.getLocation(), EntityType.EVOKER);
                event.setCancelled(true);
                return;
            }
            if (random.nextDouble() < plugin.getSettings().getDouble("config.replace-witch-illusioner", 0.1)) {
                event.getEntity().getWorld().spawnEntity(event.getLocation(), EntityType.ILLUSIONER);
                event.setCancelled(true);
                return;
            }
        }

        if (event.getEntity() instanceof Rabbit) {
            if (random.nextDouble() > plugin.getSettings().getDouble("config.killer-bunny-chance", 0.05)) {
                return;
            }
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
                return;
            }
            Rabbit rabbit = (Rabbit)event.getEntity();
            rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
            rabbit.setAdult();
            rabbit.setAgeLock(true);
        }

        int rank = 0;
        int level = 1;
        double hpMult = 1;
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
                event.getEntity().getEquipment().setItemInMainHand(new ItemStack(Material.GOLD_AXE));
            } else {
                event.getEntity().getEquipment().setItemInMainHand(new ItemStack(Material.GOLD_SWORD));
            }
            event.getEntity().getEquipment().setItemInMainHandDropChance(0f);
            event.getEntity().getEquipment().setHelmetDropChance(0f);
        } else if (event.getEntity() instanceof Skeleton) {
            if (random.nextDouble() < plugin.getSettings().getDouble("config.give-skeletons-sword-chance", 0.1)) {
                event.getEntity().getEquipment().setItemInMainHand(skeletonSword);
            } else if (random.nextDouble() < plugin.getSettings().getDouble("config.give-skeletons-wand-chance", 0.1)) {
                event.getEntity().getEquipment().setItemInMainHand(skeletonWand);
                event.getEntity().getEquipment().setHelmet(witchHat);
            } else {
                event.getEntity().getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
            }
            event.getEntity().getEquipment().setItemInMainHandDropChance(0f);
        } else if (event.getEntity() instanceof Slime) {
            hpMult = (1 + (double) ((Slime) event.getEntity()).getSize()) / 4;
        } else if (event.getEntity() instanceof Vindicator) {
            event.getEntity().getEquipment().setItemInMainHand(new ItemStack(Material.IRON_AXE));
        } else if (event.getEntity() instanceof Illusioner) {
            event.getEntity().getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
        }
        double rankUp = plugin.getSettings().getDouble("config.mob-rankup-chance", 0.1);
        if (event.getEntity() instanceof Creeper) {
            rankUp = 0;
        }
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
                    event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 99999, 0,
                            false, false));
                    break;
                case 2:
                    rankName = ChatColor.DARK_PURPLE + "Rare ";
                    event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 99999, 1,
                            false, false));
                    break;
                case 3:
                    rankName = ChatColor.RED + "Epic ";
                    event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 99999, 2,
                            false, false));
                    break;
                case 4:
                    rankName = ChatColor.GOLD + "Legendary ";
                    event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 99999, 3,
                            false, false));
                    break;
            }
        }

        String name = TextUtils.color(TextUtils.args(data.getNameFormat(), new String[][]{{"%level%", String.valueOf(level)}}));
        name = rankName + name;

        event.getEntity().setCustomName(name);
        event.getEntity().setCanPickupItems(false);

        double health = (1 + (rank * 0.75)) * hpMult * data.getHealthExpression().setVariable("LEVEL", level).evaluate();
        event.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Math.max(2, health));
        event.getEntity().setHealth(health);

        if (event.getEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            double speed = event.getEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() *
                data.getSpeedExpression().setVariable("LEVEL", level).evaluate();
            event.getEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
        }
        if (event.getEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            double damage = (data.getDamageExpression().setVariable("LEVEL", level).evaluate());
            event.getEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);
        }

        if (event.getEntity().getAttribute(Attribute.GENERIC_FLYING_SPEED) != null) {
            double flySpeed = event.getEntity().getAttribute(Attribute.GENERIC_FLYING_SPEED).getBaseValue() *
                data.getSpeedExpression().setVariable("LEVEL", level).evaluate();
            event.getEntity().getAttribute(Attribute.GENERIC_FLYING_SPEED).setBaseValue(flySpeed);
        }
    }

    static ItemStack buildSkeletonWand() {
        ItemStack wand = new ItemStack(Material.BOW);
        ItemMeta wandMeta = wand.getItemMeta();
        wandMeta.setDisplayName("WAND");
        wand.setItemMeta(wandMeta);
        return wand;
    }

    private static ItemStack buildSkeletonSword() {
        return new ItemStack(Material.STONE_SWORD);
    }

    private static ItemStack buildWitchHat() {
        ItemStack hat = new ItemStack(Material.SHEARS);
        hat.setDurability((short)2);
        return hat;
    }
}
