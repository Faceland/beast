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

import info.faceland.beast.BeastPlugin;
import java.util.Random;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class MagicListener implements Listener {

    private final BeastPlugin plugin;
    private final Random random;

    private ItemStack skeletonWand;

    private static final PotionEffectType[] WITCH_SPELLS = {
        PotionEffectType.WEAKNESS,
        PotionEffectType.WITHER,
        PotionEffectType.POISON,
        PotionEffectType.SLOW_DIGGING,
        PotionEffectType.POISON
    };

    public MagicListener(BeastPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random(System.currentTimeMillis());
        skeletonWand = SpawnListener.buildSkeletonWand();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWitchPotionThrow(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Witch)) {
            return;
        }
        if (!(e.getEntity() instanceof ThrownPotion)) {
            return;
        }
        e.setCancelled(true);
        shootWitchBall((Witch) e.getEntity().getShooter());
        shootWitchBall((Witch) e.getEntity().getShooter());
        shootWitchBall((Witch) e.getEntity().getShooter());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShulkerBulletHit(EntityDamageByEntityEvent e) {
        if (e.isCancelled() || !(e.getDamager() instanceof ShulkerBullet)) {
            return;
        }
        if ((((Projectile)e.getDamager()).getShooter() instanceof Shulker)) {
            return;
        }
        if (!(e.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity t = (LivingEntity) e.getEntity();
        t.removePotionEffect(PotionEffectType.LEVITATION);
        t.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 1, 3, true, false));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWitchSpell(EntityDamageByEntityEvent e) {
        if (e.isCancelled() || !(e.getDamager() instanceof ShulkerBullet)) {
            return;
        }
        if (!(((Projectile)e.getDamager()).getShooter() instanceof Witch)) {
            return;
        }if (!(e.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity t = (LivingEntity) e.getEntity();
        PotionEffectType effect = WITCH_SPELLS[random.nextInt(WITCH_SPELLS.length)];
        if (t.hasPotionEffect(effect)) {
            t.removePotionEffect(effect);
        }
        t.addPotionEffect(new PotionEffect(effect, 200, 0, true), false);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSkeletonSpell(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Skeleton)) {
            return;
        }
        if (!((Skeleton) e.getEntity().getShooter()).getEquipment().getItemInMainHand().isSimilar(skeletonWand)) {
            return;
        }
        if (!(e.getEntity() instanceof Arrow)) {
            return;
        }
        e.setCancelled(true);
        Skeleton skelly = (Skeleton) e.getEntity().getShooter();
        skelly.getWorld().playSound(skelly.getLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
        ShulkerBullet magicProj = skelly.getWorld().spawn(skelly.getEyeLocation().clone().add(0, -0.45, 0), ShulkerBullet.class);
        magicProj.setShooter(skelly);
        Vector vec = skelly.getLocation().getDirection();
        magicProj.setVelocity(new Vector(vec.getX() * 1.2, vec.getY() * 1.2 + 0.255, vec.getZ() * 1.2));
    }

    private void shootWitchBall(Witch w) {
        ShulkerBullet magicProj = w.getWorld().spawn(w.getEyeLocation(), ShulkerBullet.class);
        w.getWorld().playSound(w.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.9f, 2f);

        magicProj.setShooter(w);
        magicProj.setTarget(w.getTarget());
    }
}
