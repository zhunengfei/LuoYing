/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.mvc.service;

import java.util.List;
import name.huliqing.core.mvc.network.SkinNetwork;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.object.module.SkinListener;
import name.huliqing.core.object.skin.Skin;

/**
 *
 * @author huliqing
 */
public interface SkinService extends SkinNetwork {
    
    /**
     * 判断目标角色的武器是否拿在手上。
     * @param actor
     * @return 
     */
    boolean isWeaponTakeOn(Actor actor);
    
    /**
     * 获取角色身上指定ID的Skin
     * @param actor
     * @param skinId 唯一ID
     * @return 
     */
    Skin getSkin(Actor actor, String skinId);
    
    /**
     * 获取角色所有的皮肤
     * @param actor
     * @return 
     */
    List<Skin> getSkins(Actor actor);
    
    /**
     * 获取角色当前正在使用的装备
     * @param actor
     * @return 
     */
    List<Skin> getUsingSkins(Actor actor);
    
    /**
     * 获取角色当前正在使用的武器类型状态
     * @param actor
     * @return 
     */
    long getWeaponState(Actor actor);
    
    /**
     * 添加皮肤侦听器
     * @param actor
     * @param skinListener 
     */
    void addSkinListener(Actor actor, SkinListener skinListener);
    
    /**
     * 移除皮肤侦听器
     * @param actor
     * @param skinListener
     * @return 
     */
    boolean removeSkinListener(Actor actor, SkinListener skinListener);
}
