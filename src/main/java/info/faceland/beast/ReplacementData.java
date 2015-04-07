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

import com.tealcube.minecraft.bukkit.kern.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.kern.shade.google.common.base.Splitter;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public final class ReplacementData {

    private final EntityType entityType;
    private final Biome biome;
    private final Map<Integer, List<SubReplacementData>> subReplacementData;

    public Map<Integer, List<SubReplacementData>> getSubReplacementData() {
        return subReplacementData;
    }

    public ReplacementData(EntityType entityType, Biome biome) {
        this.entityType = entityType;
        this.biome = biome;
        this.subReplacementData = new HashMap<>();
    }

    public ReplacementData setSubReplacementData(int level, List<SubReplacementData> datas) {
        this.subReplacementData.put(level, datas);
        return this;
    }

    public List<SubReplacementData> setSubReplacementData(int level, String data) {
        List<SubReplacementData> datas = new ArrayList<>();
        List<String> subsets = Splitter.on(";").trimResults().omitEmptyStrings().splitToList(data);
        for (String subset : subsets) {
            datas.add(SubReplacementData.fromString(subset));
        }
        return datas;
    }

    public SubReplacementData getRandomSubReplacementData(int level, Random random) {
        int selectionLevel = 0;
        for (int i : subReplacementData.keySet()) {
            if (level > i && i > selectionLevel) {
                selectionLevel = i;
            }
        }
        List<SubReplacementData> datas = subReplacementData.get(selectionLevel);
        double selected = random.nextDouble();
        double current = 0D;
        for (SubReplacementData data : datas) {
            current += data.getChance();
            if (current >= selected) {
                return data;
            }
        }
        return null;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Biome getBiome() {
        return biome;
    }

    public static class SubReplacementData {
        private final double chance;
        private final EntityType[] entityType;
        private final int amount;

        public SubReplacementData(double chance, EntityType[] entityType, int amount) {
            this.chance = chance;
            this.entityType = entityType;
            this.amount = amount;
        }

        public double getChance() {
            return chance;
        }

        public EntityType[] getEntityType() {
            return entityType;
        }

        public int getAmount() {
            return amount;
        }

        public void spawnAtLocation(Location location) {
            for (int i = 0; i < amount; i++) {
                LivingEntity previous = null;
                for (EntityType anEntityType : entityType) {
                    LivingEntity current = (LivingEntity) location.getWorld().spawnEntity(location, anEntityType);
                    if (current != null && previous != null) {
                        current.setPassenger(previous);
                    }
                    previous = current;
                }
            }
        }

        public static SubReplacementData fromString(String s) {
            String[] splitOnPercent = s.split("%");
            String[] splitOnColon = splitOnPercent[1].split(":");
            return new SubReplacementData(NumberUtils.toDouble(splitOnPercent[0]) / 2,
                    ReplacementData.fromString(splitOnColon[0], "|"), NumberUtils.toInt(splitOnColon[1]));
        }
    }

    private static EntityType[] fromString(String string, String split) {
        String[] splitter = string.split(split);
        EntityType[] types = new EntityType[splitter.length];
        for (int i = 0; i < splitter.length; i++) {
            types[i] = EntityType.valueOf(splitter[i]);
        }
        return types;
    }

}
