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

import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.nunnerycode.kern.objecthunter.exp4j.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BeastData {

    private final EntityType type;
    private String nameFormat;
    private Expression healthExpression;
    private Expression experienceExpression;
    private Expression damageExpression;
    private Expression speedExpression;
    private Map<Integer, List<PotionEffect>> potionEffectMap;
    private List<DropData> drops;

    public BeastData(EntityType type) {
        this.type = type;
        this.drops = new ArrayList<>();
    }

    public EntityType getType() {
        return type;
    }

    public String getNameFormat() {
        return nameFormat;
    }

    public void setNameFormat(String nameFormat) {
        this.nameFormat = nameFormat;
    }

    public Expression getHealthExpression() {
        return healthExpression;
    }

    public void setHealthExpression(Expression healthExpression) {
        this.healthExpression = healthExpression;
    }

    public Expression getExperienceExpression() {
        return experienceExpression;
    }

    public void setExperienceExpression(Expression experienceExpression) {
        this.experienceExpression = experienceExpression;
    }

    public Expression getDamageExpression() {
        return damageExpression;
    }

    public void setDamageExpression(Expression damageExpression) {
        this.damageExpression = damageExpression;
    }

    public Map<Integer, List<PotionEffect>> getPotionEffectMap() {
        return potionEffectMap;
    }

    public void setPotionEffectMap(Map<Integer, List<PotionEffect>> potionEffectMap) {
        this.potionEffectMap = potionEffectMap;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BeastData beastData = (BeastData) o;

        return type == beastData.type;
    }

    public List<DropData> getDrops() {
        return drops;
    }

    public void setDrops(List<DropData> drops) {
        if (drops == null) {
            this.drops.clear();
        } else {
            this.drops = drops;
        }
    }

    public Expression getSpeedExpression() {
        return speedExpression;
    }

    public void setSpeedExpression(Expression speedExpression) {
        this.speedExpression = speedExpression;
    }

}
