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

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.Splitter;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public final class ReplacementData {

    private final EntityType entityType;
    private final Biome biome;
    private final Map<Integer, List<SubReplacementData>> subReplacementData;

    public ReplacementData(EntityType entityType, Biome biome) {
        this.entityType = entityType;
        this.biome = biome;
        this.subReplacementData = new HashMap<>();
    }

    private static EntityType[] fromString(String string, String split) {
        List<String> strings = Splitter.on(split).omitEmptyStrings().trimResults().splitToList(string);
        String[] splitter = strings.toArray(new String[strings.size()]);
        EntityType[] types = new EntityType[splitter.length];
        for (int i = 0; i < splitter.length; i++) {
            types[i] = EntityType.valueOf(splitter[i]);
        }
        return types;
    }

    public Map<Integer, List<SubReplacementData>> getSubReplacementData() {
        return subReplacementData;
    }

    public ReplacementData setSubReplacementData(int level, List<SubReplacementData> datas) {
        this.subReplacementData.put(level, datas);
        return this;
    }

    public ReplacementData setSubReplacementData(int level, String data) {
        List<SubReplacementData> datas = new ArrayList<>();
        List<String> subsets = Splitter.on(";").trimResults().omitEmptyStrings().splitToList(data);
        for (String subset : subsets) {
            datas.add(SubReplacementData.fromString(subset));
        }
        return setSubReplacementData(level, datas);
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

        public static SubReplacementData fromString(String s) {
            String[] splitOnPercent = s.split("%");
            String[] splitOnColon = splitOnPercent[1].split(":");
            return new SubReplacementData(NumberUtils.toDouble(splitOnPercent[0]) / 2,
                    ReplacementData.fromString(splitOnColon[0], "|"), NumberUtils.toInt(splitOnColon[1]));
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
                        if (current.getType() == previous.getType()) {
                            break;
                        }
                        current.setPassenger(previous);
                    }
                    previous = current;
                }
            }
        }
    }

}
