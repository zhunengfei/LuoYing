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
package name.huliqing.luoying.mess;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.layer.network.EntityNetwork;
import name.huliqing.luoying.layer.service.EntityService;
import name.huliqing.luoying.layer.service.PlayService;
import name.huliqing.luoying.network.GameClient;
import name.huliqing.luoying.network.GameServer;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.xml.ObjectData;

/**
 * 给Entity添加物品
 * @author huliqing
 */
@Serializable
public class EntityAddDataMess extends GameMess {
    
    private long entityId;
    private ObjectData objectData;
    private int amount;
 
    public long getEntityId() {
        return entityId;
    }

    /**
     * 设置EntityId
     * @param entityId 
     */
    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public ObjectData getObjectData() {
        return objectData;
    }

    /**
     * 设置要添加的物品
     * @param objectData 
     */
    public void setObjectData(ObjectData objectData) {
        this.objectData = objectData;
    }

    public int getAmount() {
        return amount;
    }

    /**
     * 设置要添加的物品数量
     * @param amount 
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    @Override
    public void applyOnClient(GameClient gameClient) {
        super.applyOnClient(gameClient);
        Entity entity = Factory.get(PlayService.class).getEntity(entityId);
        if (entity != null) {
            Factory.get(EntityService.class).addObjectData(entity, objectData, amount);
        }
    }

    @Override
    public void applyOnServer(GameServer gameServer, HostedConnection source) {
        super.applyOnServer(gameServer, source);
        Entity entity = Factory.get(PlayService.class).getEntity(entityId);
        if (entity != null) {
            Factory.get(EntityNetwork.class).addObjectData(entity, objectData, amount);
        }
    }
    
}
