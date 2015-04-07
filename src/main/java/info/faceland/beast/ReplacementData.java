package info.faceland.beast;

import com.tealcube.minecraft.bukkit.kern.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.kern.shade.google.common.base.Splitter;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

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
        private final EntityType entityType;
        private final int amount;

        public SubReplacementData(double chance, EntityType entityType, int amount) {
            this.chance = chance;
            this.entityType = entityType;
            this.amount = amount;
        }

        public double getChance() {
            return chance;
        }

        public EntityType getEntityType() {
            return entityType;
        }

        public int getAmount() {
            return amount;
        }

        public static SubReplacementData fromString(String s) {
            String[] splitOnPercent = s.split("%");
            String[] splitOnColon = splitOnPercent[1].split(":");
            return new SubReplacementData(NumberUtils.toDouble(splitOnPercent[0]) / 2,
                    EntityType.valueOf(splitOnColon[0]), NumberUtils.toInt(splitOnColon[1]));
        }
    }

}
