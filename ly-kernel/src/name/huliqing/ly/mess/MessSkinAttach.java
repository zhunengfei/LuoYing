/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.mess;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import name.huliqing.ly.Factory;
import name.huliqing.ly.layer.network.SkinNetwork;
import name.huliqing.ly.layer.service.PlayService;
import name.huliqing.ly.layer.service.SkinService;
import name.huliqing.ly.network.GameServer;
import name.huliqing.ly.object.entity.Entity;
import name.huliqing.ly.object.skin.Skin;

/**
 *
 * @author huliqing
 */
@Serializable
public class MessSkinAttach extends MessBase {
    
    private long actorId;
    private String skinId;

    public long getActorId() {
        return actorId;
    }

    public void setActorId(long actorId) {
        this.actorId = actorId;
    }

    public String getSkinId() {
        return skinId;
    }

    public void setSkinId(String skinId) {
        this.skinId = skinId;
    }

    @Override
    public void applyOnServer(GameServer gameServer, HostedConnection source) {
        Entity actor = Factory.get(PlayService.class).getEntity(actorId);
        if (actor == null) {
            return;
        }
        Skin skin = Factory.get(SkinService.class).getSkin(actor, skinId);
        if (skin != null) {
            Factory.get(SkinNetwork.class).attachSkin(actor, skin);
        }
    }

    @Override
    public void applyOnClient() {
        Entity actor = Factory.get(PlayService.class).getEntity(actorId);
        if (actor == null) {
            return;
        }
        SkinService skinService = Factory.get(SkinService.class);
        Skin skin = skinService.getSkin(actor, skinId);
        if (skin != null) {
            skinService.attachSkin(actor, skin);
        }
    }
    
}
