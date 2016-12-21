/*
 * LuoYing is a program used to make 3D RPG game.
 * Copyright (c) 2014-2016 Huliqing <31703299@qq.com>
 * 
 * This file is part of LuoYing.
 *
 * LuoYing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LuoYing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LuoYing.  If not, see <http://www.gnu.org/licenses/>.
 */
package name.huliqing.luoying.data;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.network.serializing.Serializable;
import java.io.IOException;
import name.huliqing.luoying.LuoYingException;

/**
 *
 * @author huliqing
 */
@Serializable
public class DropItem implements Savable,Cloneable{
    // 物品ID
    private String itemId;
    // 掉落数量,默认为1，小于1的话没有意义
    private int count = 1;
    // 掉落机率[0.0~1.0]，如果为0，则永远不可能掉落；如果为1，则还可受全局
    // 机率影响,该机率最终会和全局机率相剩以获得最终机率。默认为1.
    private float factor = 1;

    public DropItem() {}
    
    public DropItem(String itemId) {
        this(itemId, 1, 1);
    }

    public DropItem(String itemId, int count) {
        this(itemId, count, 1);
    }

    public DropItem(String itemId, int count, float factor) {
        this.itemId = itemId;
        this.count = count;
        this.factor = factor;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public float getFactor() {
        return factor;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
    @Override
    public DropItem clone() {
        try {
            DropItem clone = (DropItem) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new LuoYingException(e);
        }
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(itemId, "itemId", null);
        oc.write(count, "count", 1);
        oc.write(factor, "factor", 1);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        itemId = ic.readString("itemId", null);
        count = ic.readInt("count", 1);
        factor = ic.readFloat("factor", 1);
    }
}
