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
 * 使用物品
 * @author huliqing
 */
@Serializable
public class MessItemUse extends MessBase {
    
    private long actorId;
    private String itemId;

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
    
    @Override
    public void applyOnServer(GameServer gameServer, HostedConnection source) {
        Entity actor = Factory.get(PlayService.class).getEntity(actorId);
        if (actor == null) {
            return;
        }
        Factory.get(ItemNetwork.class).useItem(actor, itemId);
    }

    @Override
    public void applyOnClient() {
        Entity actor = Factory.get(PlayService.class).getEntity(actorId);
        if (actor == null) {
            return;
        }
        Factory.get(ItemService.class).useItem(actor, itemId);
    }
    
}
