package info.faceland.beast;

import org.bukkit.ChatColor;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class EliteAbilities implements Listener {

    private final BeastPlugin plugin;
    private final Random random;

    public EliteAbilities(BeastPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            return;
        }
        if (event.getDamager() == null || event.getEntity() == null) {
            return;
        }
        if (event.getDamager().getCustomName() == null) {
            return;
        }
        LivingEntity monster = (LivingEntity) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();
        String mobName = monster.getCustomName();
        if (!monster.hasMetadata("RANK")) {
            if (mobName.startsWith(ChatColor.BLUE + "Magic")) {
                monster.setMetadata("RANK", new FixedMetadataValue(plugin, 2));
                monster.setMetadata("SKILL1", new FixedMetadataValue(plugin, random.nextInt(3)));
            } else if (mobName.startsWith(ChatColor.DARK_PURPLE + "Rare")) {
                monster.setMetadata("RANK", new FixedMetadataValue(plugin, 3));
                monster.setMetadata("SKILL1", new FixedMetadataValue(plugin, random.nextInt(3)));
                monster.setMetadata("SKILL2", new FixedMetadataValue(plugin, random.nextInt(4)));
            } else if (mobName.startsWith(ChatColor.RED + "Epic")) {
                monster.setMetadata("RANK", new FixedMetadataValue(plugin, 4));
                monster.setMetadata("SKILL1", new FixedMetadataValue(plugin, random.nextInt(3)));
                monster.setMetadata("SKILL2", new FixedMetadataValue(plugin, random.nextInt(4)));
                monster.setMetadata("SKILL3", new FixedMetadataValue(plugin, random.nextInt(3)));
            } else if (mobName.startsWith(ChatColor.GOLD + "Legendary")) {
                monster.setMetadata("RANK", new FixedMetadataValue(plugin, 5));
                monster.setMetadata("SKILL1", new FixedMetadataValue(plugin, random.nextInt(3)));
                monster.setMetadata("SKILL2", new FixedMetadataValue(plugin, random.nextInt(4)));
                monster.setMetadata("SKILL3", new FixedMetadataValue(plugin, random.nextInt(3)));
                monster.setMetadata("SKILL4", new FixedMetadataValue(plugin, random.nextInt(3)));
            } else {
                monster.setMetadata("RANK", new FixedMetadataValue(plugin, 1));
            }
        }
        if (monster.getMetadata("RANK").get(0).asInt() > 1) {
            if (monster.getMetadata("RANK").get(0).asInt() == 2) {
                triggerSkillOne(target, monster.getMetadata("SKILL1").get(0).asInt());
            } else {
                triggerSkillOne(target, monster.getMetadata("SKILL1").get(0).asInt());
                triggerSkillTwo(monster, target, monster.getMetadata("SKILL2").get(0).asInt());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        if (event.getEntity() == null) {
            return;
        }
        if (event.getEntity().getCustomName() == null) {
            return;
        }
        LivingEntity monster = event.getEntity();
        if (monster.hasMetadata("RANK")) {
            if (monster.getMetadata("RANK").get(0).asInt() > 3) {
                triggerDeathSkill(monster.getKiller(), monster.getMetadata("SKILL3").get(0).asInt());
            }
        }

    }


    private void triggerSkillOne(LivingEntity t, int skill) {
        switch (skill) {
            case 0:
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1, true));
                break;
            case 1:
                t.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 0, true));
                break;
            case 2:
                t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 50, 0));
                break;
        }
    }

    private void triggerSkillTwo(LivingEntity a, LivingEntity t, int skill) {
        switch (skill) {
            case 0:
                t.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 50, 1, true));
                break;
            case 1:
                t.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1, true));
                break;
            case 2:
                t.setFireTicks(65);
                break;
            case 3:
                if (a instanceof Creeper) {
                    break;
                }
                a.setHealth(Math.min(a.getHealth() + (a.getMaxHealth() / 10), a.getMaxHealth()));
                break;
        }
    }

    private void triggerDeathSkill(LivingEntity t, int skill) {
        switch (skill) {
            case 0:
                t.getLocation().getWorld().spawnEntity(t.getLocation(), t.getType());
                t.getLocation().getWorld().spawnEntity(t.getLocation(), t.getType());
                t.getLocation().getWorld().spawnEntity(t.getLocation(), t.getType());
                t.getLocation().getWorld().spawnEntity(t.getLocation(), EntityType.BAT);
                t.getLocation().getWorld().spawnEntity(t.getLocation(), EntityType.BAT);
                t.getLocation().getWorld().spawnEntity(t.getLocation(), EntityType.BAT);
                break;
            case 1:
                t.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 2, true));
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 3, true));
                break;
            case 2:
                t.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 2));
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 200, 2));
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 7));
                break;
        }
    }
}
