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
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.nunnerycode.kern.apache.commons.lang3.math.NumberUtils;
import org.nunnerycode.kern.shade.google.common.base.CharMatcher;

import java.util.List;
import java.util.Map;

public final class BeastTask extends BukkitRunnable {

    private final BeastPlugin plugin;

    public BeastTask(BeastPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (!(e instanceof LivingEntity)) {
                    continue;
                }
                LivingEntity le = (LivingEntity) e;
                if (le.getCustomName() == null) {
                    continue;
                }
                BeastData data = plugin.getData(le.getType());
                if (data == null) {
                    continue;
                }
                if (le.getCustomName() == null) {
                    continue;
                }
                int level = NumberUtils.toInt(
                        CharMatcher.DIGIT.or(CharMatcher.is('-')).retainFrom(le.getCustomName()));
                for (Map.Entry<Integer, List<PotionEffect>> entry : data.getPotionEffectMap().entrySet()) {
                    if (level >= entry.getKey()) {
                        le.addPotionEffects(entry.getValue());
                    }
                }
            }
        }
    }
}
