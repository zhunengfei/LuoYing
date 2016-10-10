/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.mess;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import name.huliqing.ly.Factory;
import name.huliqing.ly.layer.network.ItemNetwork;
import name.huliqing.ly.layer.service.ItemService;
import name.huliqing.ly.layer.service.PlayService;
import name.huliqing.ly.network.GameServer;
import name.huliqing.ly.object.entity.Entity;

/**
 *
 * @author huliqing
 */
@Serializable
public class MessItemAdd extends MessBase {
    
    private long actorId;
    private String itemId;
    private int count;

    public long getActorId() {
        return actorId;
    }

    public void setActorId(long actorId) {
        this.actorId = actorId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
    @Override
    public void applyOnServer(GameServer gameServer, HostedConnection source) {
        Entity actor = Factory.get(PlayService.class).getEntity(actorId);
        if (actor == null) {
            return;
        }
        Factory.get(ItemNetwork.class).addItem(actor, itemId, count);
    }

    @Override
    public void applyOnClient() {
        Entity actor = Factory.get(PlayService.class).getEntity(actorId);
        if (actor == null) {
            return;
        }
        Factory.get(ItemService.class).addItem(actor, itemId, count);
    }
    
}
