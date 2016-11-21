/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.mess;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.layer.network.EntityNetwork;
import name.huliqing.luoying.layer.service.EntityService;
import name.huliqing.luoying.layer.service.PlayService;
import name.huliqing.luoying.network.GameServer;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.xml.ObjectData;

/**
 * 给Entity添加物品
 * @author huliqing
 */
@Serializable
public class MessEntityAddData extends MessBase {
    
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
    public void applyOnClient() {
        super.applyOnClient();
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
