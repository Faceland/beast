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
package info.faceland.beast;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

final class BeastListener implements Listener {

    private final BeastPlugin plugin;
    private final Random random;
    private static final PotionEffectType[] WITCH_SPELLS = {PotionEffectType.WEAKNESS, PotionEffectType.WITHER,
            PotionEffectType.POISON, PotionEffectType.SLOW_DIGGING, PotionEffectType.POISON};

    public BeastListener(BeastPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random(System.currentTimeMillis());
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
                event.getEntity().getEquipment().setItemInMainHand(new ItemStack(Material.STONE_SWORD));
            } else {
                event.getEntity().getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
            }
            event.getEntity().getEquipment().setItemInMainHandDropChance(0f);
        } else if (event.getEntity() instanceof Slime) {
            hpMult = (1 + (double) ((Slime) event.getEntity()).getSize()) / 4;
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

        double newMaxHealth = (1 + (rank * 0.75)) * hpMult * data.getHealthExpression().setVariable("LEVEL", level)
                .evaluate();
        double speed = data.getSpeedExpression().setVariable("LEVEL", level).evaluate();
        event.getEntity().setMaxHealth(Math.max(2, newMaxHealth));
        event.getEntity().setHealth(event.getEntity().getMaxHealth());
        event.getEntity().setCanPickupItems(false);
        event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 10, (int) speed,
                false, false));
        double damage = (data.getDamageExpression().setVariable("LEVEL", level).evaluate());
        event.getEntity().setMetadata("DAMAGE", new FixedMetadataValue(plugin, damage));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity)) {
            return;
        }
        if (plugin.getApi().isBoss(event.getDamager())) {
            return;
        }
        LivingEntity a;
        if (event.getDamager() instanceof Projectile) {
            a = (LivingEntity) ((Projectile) event.getDamager()).getShooter();
        } else {
            a = (LivingEntity) event.getDamager();
        }
        if (a.hasMetadata("DAMAGE")) {
            double damage = a.getMetadata("DAMAGE").get(0).asDouble();
            if (a instanceof Creeper) {
                if (a.getFireTicks() > 0) {
                    event.getEntity().setFireTicks(event.getEntity().getFireTicks() + 80);
                }
                if (((Creeper) a).isPowered()) {
                    damage = damage * Math.max(0.3, 3 - (a.getLocation().distance(event.getEntity().getLocation()) / 2));
                } else {
                    damage = damage * Math.max(0.3, 1 - (a.getLocation().distance(event.getEntity().getLocation()) / 3));
                }
            }
            event.setDamage(damage);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (event instanceof PlayerDeathEvent) {
            return;
        }
        BeastData data = plugin.getData(event.getEntityType());

        if (data == null) {
            if (event.getEntity().getKiller() == null) {
                if (random.nextDouble() < 0.85) {
                    event.getDrops().clear();
                }
            }
            return;
        }

        if (event.getEntity().getCustomName() == null) {
            return;
        }

        if (event.getEntity().getCustomName().startsWith(ChatColor.WHITE + "Spawned")) {
            if (random.nextDouble() < 0.5) {
                event.getDrops().clear();
            } else {
                dropDrops(event, data);
            }
            event.setDroppedExp(0);
            return;
        }

        dropDrops(event, data);

        if (event.getEntity().getKiller() == null) {
            event.setDroppedExp(0);
            return;
        }

        if (plugin.getApi().isBoss(event.getEntity())) {
            return;
        }

        double xpMult = 1D;
        if (event.getEntity() instanceof Slime) {
            xpMult = (1 + ((Slime) event.getEntity()).getSize()) / 4;
        }
        int level = NumberUtils.toInt(CharMatcher.DIGIT.retainFrom(ChatColor.stripColor(event.getEntity().getCustomName())));
        event.setDroppedExp((int) (data.getExperienceExpression().setVariable("LEVEL", level).evaluate() * xpMult));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathAutoOrb(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        World w = event.getEntity().getWorld();
        Entity e = w.spawnEntity(event.getEntity().getKiller().getEyeLocation(), EntityType.EXPERIENCE_ORB);
        ((ExperienceOrb) e).setExperience(event.getDroppedExp());
        event.setDroppedExp(0);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWitchPotionThrow(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Witch)) {
            return;
        }
        e.setCancelled(true);
        shootWitchBall((Witch) e.getEntity().getShooter());
        shootWitchBall((Witch) e.getEntity().getShooter());
        shootWitchBall((Witch) e.getEntity().getShooter());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWitchSpell(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof ShulkerBullet)) {
            return;
        }
        if (!(((Projectile)e.getDamager()).getShooter() instanceof Witch)) {
            return;
        }
        if (!(e.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (e.isCancelled()) {
            return;
        }
        LivingEntity t = (LivingEntity) e.getEntity();

        PotionEffectType effect = WITCH_SPELLS[random.nextInt(WITCH_SPELLS.length)];
        if (t.hasPotionEffect(effect)) {
            t.removePotionEffect(effect);
        }
        t.addPotionEffect(new PotionEffect(effect, 400, 0, true), false);

        t.removePotionEffect(PotionEffectType.LEVITATION);
        t.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 2, 5, true, false));

    }

    private void shootWitchBall(Witch w) {
        ShulkerBullet magicProj = w.getWorld().spawn(w.getEyeLocation(), ShulkerBullet.class);
        w.getWorld().playSound(w.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.9f, 2f);

        magicProj.setShooter(w);
        magicProj.setTarget(w.getTarget());
    }

    private void dropDrops(EntityDeathEvent event, BeastData data) {
        if (!data.getDrops().isEmpty()) {
            event.getDrops().clear();
            for (DropData dropData : data.getDrops()) {
                if (random.nextDouble() < dropData.getChance()) {
                    event.getDrops().add(dropData.toItemStack(dropData.getMinimumAmount(), dropData.getMaximumAmount()));
                }
            }
        }
    }
}
