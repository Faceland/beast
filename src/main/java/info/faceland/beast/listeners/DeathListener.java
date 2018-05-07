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

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.beast.BeastData;
import info.faceland.beast.BeastPlugin;
import info.faceland.beast.DropData;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final BeastPlugin plugin;
    private final Random random;

    public DeathListener(BeastPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random(System.currentTimeMillis());
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
