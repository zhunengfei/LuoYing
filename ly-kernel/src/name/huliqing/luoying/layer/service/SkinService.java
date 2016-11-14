/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.layer.service;

import java.util.List;
import name.huliqing.luoying.layer.network.SkinNetwork;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.module.SkinListener;
import name.huliqing.luoying.object.skin.Skin;

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
    boolean isWeaponTakeOn(Entity actor);
    
    /**
     * 获取角色所有的皮肤
     * @param actor
     * @return 
     */
    List<Skin> getSkins(Entity actor);
    
    /**
     * 获取角色当前正在使用的装备
     * @param actor
     * @return 
     */
    List<Skin> getUsingSkins(Entity actor);
    
    /**
     * 获取角色当前正在使用的武器类型状态
     * @param actor
     * @return 
     */
    long getWeaponState(Entity actor);
    
    /**
     * 添加皮肤侦听器
     * @param actor
     * @param skinListener 
     */
    void addSkinListener(Entity actor, SkinListener skinListener);
    
    /**
     * 移除皮肤侦听器
     * @param actor
     * @param skinListener
     * @return 
     */
    boolean removeSkinListener(Entity actor, SkinListener skinListener);
}
