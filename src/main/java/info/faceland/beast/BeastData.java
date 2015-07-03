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

import com.tealcube.minecraft.bukkit.kern.objecthunter.exp4j.Expression;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;

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
