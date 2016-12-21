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
package name.huliqing.luoying.object.shape;

import com.jme3.scene.Geometry;
import name.huliqing.luoying.data.ShapeData;
import name.huliqing.luoying.utils.MaterialUtils;

/**
 *
 * @author huliqing
 * @param <T>
 */
public abstract class AbstractShape<T extends ShapeData> implements Shape<T> {

    protected T data;
    protected Geometry geometry;

    @Override
    public void setData(T data) {
        this.data = data;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public void updateDatas() {
        // ignore
    }

    @Override
    public Geometry getGeometry() {
        if (geometry == null) {
            geometry = new Geometry("AbstractShape", getMesh());
            geometry.setMaterial(MaterialUtils.createWireFrame());
        }
        return geometry;
    }
    
}
