/******************************************************************************
 * Copyright (c) 2014, Richard Harrah                                         *
 *                                                                            *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

package info.faceland.beast;

import com.google.common.base.CharMatcher;
import info.faceland.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class BeastListener implements Listener {

    private final BeastPlugin plugin;

    public BeastListener(BeastPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreatureSpawnLowest(final CreatureSpawnEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Monster)) {
            return;
        }
        final EntityType replacementType = EntityType.fromName(
                plugin.getSettings().getString("replacements." + event.getEntity().getType().name() + "." +
                                               event.getLocation().getBlock().getBiome().name(),
                                               event.getEntity().getType().name()));
        if (replacementType == event.getEntity().getType()) {
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
        BeastData data = plugin.getData(event.getEntityType());
        if (data == null) {
            return;
        }
        int startingLevel =
                plugin.getSettings().getInt("config.enabled-worlds." + event.getLocation().getWorld().getName() +
                                            ".starting-level", -1);
        if (startingLevel < 0) {
            return;
        }
        double distanceFromSpawn = event.getLocation().distanceSquared(
                event.getLocation().getWorld().getSpawnLocation());
        int level = (int) (distanceFromSpawn / Math.pow(plugin.getSettings().getInt("config.enabled-worlds." + event
                .getLocation().getWorld().getName() + ".distance-per-level", 150), 2));
        event.getEntity().setCustomName(TextUtils.color(TextUtils.args(
                data.getNameFormat(), new String[][]{{"%level%", String.valueOf(level)}})));
        double currentMaxHealth = event.getEntity().getMaxHealth();
        double newMaxHealth = data.getHealthExpression().setVariable("LEVEL", level).evaluate();
        event.getEntity().setCustomNameVisible(true);
        event.getEntity().setHealth(Math.min(currentMaxHealth, newMaxHealth) / 2);
        event.getEntity().setMaxHealth(newMaxHealth);
        event.getEntity().setHealth(event.getEntity().getMaxHealth());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (event instanceof PlayerDeathEvent) {
            return;
        }
        BeastData data = plugin.getData(event.getEntityType());
        if (data == null) {
            return;
        }
        int level = Integer.parseInt(CharMatcher.DIGIT.retainFrom(ChatColor.stripColor(event.getEntity()
                                                                                            .getCustomName())));
        event.setDroppedExp((int) data.getExperienceExpression().setVariable("LEVEL", level).evaluate());
    }

}
