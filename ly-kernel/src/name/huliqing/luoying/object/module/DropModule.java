/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.object.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import name.huliqing.luoying.data.DropData;
import name.huliqing.luoying.data.ModuleData;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.attribute.Attribute;
import name.huliqing.luoying.object.attribute.BooleanAttribute;
import name.huliqing.luoying.object.drop.Drop;
import name.huliqing.luoying.object.entity.DataHandler;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.sound.SoundManager;
import name.huliqing.luoying.object.entity.EntityAttributeListener;

/**
 * DropModule用于处理物品、经验掉落，给实体增加这个模块之后，当实体被攻击致死的时候，
 * 会给目标攻击者掉落物品、属性
 * @author huliqing
 */
public class DropModule extends AbstractModule implements DataHandler<DropData> { 

    // 绑定角色的“死亡”属性
    private String bindDeadAttribute;
    
    // 掉落物品时的默认提示声效
    private String[] sounds; 
    
    // ---- inner
    private List<Drop> drops;
    private BooleanAttribute deadAttribute;
    
    // 用于监听Entity受到攻击致死事件,并处理物品掉落
    private final EntityAttributeListener hitDeadEntityListener = new EntityAttributeListener() {
        
        private boolean deadStateBeforeHit;
        
        @Override
        public void onHitAttributeBefore(Attribute attribute, Object hitValue, Entity hitter) {
            if (deadAttribute == null) 
                return;
            deadStateBeforeHit = deadAttribute.getValue();
        }
        
        @Override
        public void onHitAttributeAfter(Attribute attribute, Object hitValue, Entity hitter, Object oldValue) {
            if (deadAttribute == null)
                return;
            // killed
            if (!deadStateBeforeHit && deadAttribute.getValue()) {
                doDrop(hitter);
            }
        }
    };

    @Override
    public void setData(ModuleData data) {
        super.setData(data); 
        bindDeadAttribute = data.getAsString("bindDeadAttribute");
        sounds = data.getAsArray("sounds");
    }

    @Override
    public void updateDatas() {
        // xxx updateDatas.
    }
    
    @Override
    public void initialize(Entity actor) {
        super.initialize(actor); 
        if (bindDeadAttribute != null) {
            deadAttribute = entity.getAttributeManager().getAttribute(bindDeadAttribute, BooleanAttribute.class);
        }
        
        List<DropData> dropDatas = actor.getData().getObjectDatas(DropData.class, null);
        if (dropDatas != null) {
            for (DropData id : dropDatas) {
                addDrop((Drop) Loader.load(id));
            }
        }
        entity.addEntityAttributeListener(hitDeadEntityListener);
    }

    @Override
    public void cleanup() {
        if (drops != null) {
            drops.clear();
        }
        entity.removeEntityAttributeListener(hitDeadEntityListener);
        super.cleanup(); 
    }
    
    /**
     * 添加一个掉落设置
     * @param drop 
     * @return  
     */
    public boolean addDrop(Drop drop) {
        if (drops == null) {
            drops = new ArrayList<Drop>(5);
        }
        if (drops.contains(drop))
            return false;
        
        drops.add(drop);
        entity.getData().addObjectData(drop.getData());
        return true;
    }
    
    /**
     * 移除一个掉落设置
     * @param drop
     * @return 
     */
    public boolean removeDrop(Drop drop) {
        if (drops == null || !drops.contains(drop)) 
            return false;
        
        drops.remove(drop);
        entity.getData().removeObjectData(drop.getData());
        return true;
    }
    
//    /**
//     * 获取掉落设置, 返回的列表只允许只读。
//     * @return 
//     */
//    public List<Drop> getDrops() {
//        if (drops != null) {
//            return Collections.unmodifiableList(drops);
//        }
//        return Collections.EMPTY_LIST;
//    }
    
    /**
     * 处理掉落物品给指定角色, 注：物品是从当前角色掉落到指定角色(target)身上。
     * @param target
     */
    public void doDrop(Entity target) {
        if (drops == null) {
            return;
        }
        boolean hasDrop = false;
        for (int i = 0; i < drops.size(); i++) {
            if (drops.get(i).doDrop(entity, target)) {
                hasDrop = true;
            }
        }
        if (hasDrop) {
            playDefaultDropSound();
        }
    }
    
    // 播放默认的掉落声音
    private void playDefaultDropSound() {
        if (sounds == null)
            return;
        
        for (String s : sounds) {
            SoundManager.getInstance().playSound(s, entity.getSpatial().getWorldTranslation());
        }
    }

    @Override
    public Class<DropData> getHandleType() {
        return DropData.class;
    }

    @Override
    public boolean handleDataAdd(DropData data, int amount) {
        if (drops != null) {
            for (Drop d : drops) {
                if (d.getData() == data) {
                    return false; // 已经存在
                }
            }
        }
        return addDrop((Drop) Loader.load(data));
    }

    @Override
    public boolean handleDataRemove(DropData data, int amount) {
        if (drops == null) {
            return false;
        }
        Drop found = null;
        for (Drop d : drops) {
            if (d.getData() == data) {
                found = d;
                break;
            }
        }
        if (found == null) {
            return false;
        }
        return removeDrop(found);
    }

    @Override
    public boolean handleDataUse(DropData data) {
        Drop drop = Loader.load(data);
        return drop.doDrop(entity, null);
    }
}
