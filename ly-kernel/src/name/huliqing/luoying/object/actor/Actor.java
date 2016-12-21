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
package name.huliqing.luoying.object.actor;

import com.jme3.scene.Spatial;
import name.huliqing.luoying.data.ActorData;
import name.huliqing.luoying.object.entity.ModelEntity;

/**
 * 角色，角色由数据(ObjectData)和模块处理器(Module)组成。
 * @author huliqing
 * @param <T>
 */
public class Actor<T extends ActorData> extends ModelEntity<T> {
    
    @Override
    public void setData(T data) {
        super.setData(data); 
    }

    @Override
    public T getData() {
        return super.getData(); 
    }
    
    /**
     * 载入基本模型
     * @return 
     */
    @Override
    protected Spatial loadModel() {
        return ActorModelLoader.loadActorModel(this);
    }
    
    
}
