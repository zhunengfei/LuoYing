/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.object.logic;

import name.huliqing.luoying.Factory;
import name.huliqing.luoying.object.action.FightAction;
import name.huliqing.luoying.data.LogicData;
import name.huliqing.luoying.layer.service.ActionService;
import name.huliqing.luoying.layer.service.ActorService;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.module.ActorModule;

/**
 * 只用于player角色的逻辑,不会有idle行为，因为不让玩角角色在停下来的时候或
 * 没有目标的时候做idle行为
 * @author huliqing
 * @param <T>
 */
public class PlayerLogic<T extends LogicData> extends Logic<T> {
    private final ActionService actionService = Factory.get(ActionService.class);
    private final ActorService actorService = Factory.get(ActorService.class);
    private ActorModule actorModule;
    
    protected FightAction fightAction;
    
    @Override
    public void setData(T data) {
        super.setData(data); 
        fightAction = (FightAction) Loader.load(data.getAsString("fightAction"));
    }

    @Override
    public void setActor(Entity actor) {
        super.setActor(actor); 
        actorModule = actor.getModuleManager().getModule(ActorModule.class);
    }
    
    @Override
    protected void doLogic(float tpf) {
        // 如果玩家正在控制走路，则不执行逻辑
        if (actionService.isPlayingRun(actor)) {
            return;
        }
        
        Entity target = actorModule.getTarget();
        
        if (target != null && !actorService.isDead(target) 
                && target.getSpatial().getWorldTranslation().distance(actor.getSpatial().getWorldTranslation()) < actorModule.getViewDistance()
                &&  actorModule.isEnemy(target)
                ) {
            fightAction.setEnemy(target);
            actionService.playAction(actor, fightAction);
        }
    }
    
}
