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
        hiltItemStack.setAmount((int) (random.nextDouble() * (maximumAmount - minimumAmount)) + minimumAmount);
        return hiltItemStack;
    }

}
