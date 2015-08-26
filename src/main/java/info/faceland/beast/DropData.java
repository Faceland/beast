/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.beast;

import com.tealcube.minecraft.bukkit.hilt.HiltItemStack;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

public final class DropData {

    private Material material;
    private int minimumAmount;
    private int maximumAmount;
    private double chance;
    private String name;
    private List<String> lore;
    private Map<Enchantment, Integer> enchantmentMap;

    public DropData() {
        this.lore = new ArrayList<>();
        this.enchantmentMap = new HashMap<>();
    }

    public Material getMaterial() {
        return material;
    }

    public DropData setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public int getMinimumAmount() {
        return minimumAmount;
    }

    public DropData setMinimumAmount(int minimumAmount) {
        this.minimumAmount = minimumAmount;
        return this;
    }

    public int getMaximumAmount() {
        return maximumAmount;
    }

    public DropData setMaximumAmount(int maximumAmount) {
        this.maximumAmount = maximumAmount;
        return this;
    }

    public double getChance() {
        return chance;
    }

    public DropData setChance(double chance) {
        this.chance = chance;
        return this;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = material != null ? material.hashCode() : 0;
        result = 31 * result + minimumAmount;
        result = 31 * result + maximumAmount;
        temp = Double.doubleToLongBits(chance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DropData)) {
            return false;
        }

        DropData dropData = (DropData) o;

        return Double.compare(dropData.chance, chance) == 0 && maximumAmount == dropData.maximumAmount &&
                minimumAmount == dropData.minimumAmount && material == dropData.material;
    }

    public String getName() {
        return name;
    }

    public DropData setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getLore() {
        return lore;
    }

    public DropData setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public Map<Enchantment, Integer> getEnchantmentMap() {
        return enchantmentMap;
    }

    public DropData setEnchantmentMap(Map<Enchantment, Integer> enchantmentMap) {
        this.enchantmentMap = enchantmentMap;
        return this;
    }

    public HiltItemStack toItemStack(Random random) {
        HiltItemStack hiltItemStack = new HiltItemStack(material);
        hiltItemStack.setName(name);
        hiltItemStack.setLore(lore);
        hiltItemStack.addUnsafeEnchantments(enchantmentMap);
        hiltItemStack.setAmount(Math.max(1, (int) (random.nextDouble() * (maximumAmount - minimumAmount)) +
                minimumAmount));
        return hiltItemStack;
    }

}
