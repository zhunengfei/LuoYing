/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.object.state;

import name.huliqing.luoying.data.StateData;
import name.huliqing.luoying.object.module.ActorModule;

/**
 *
 * @author huliqing
 */
public class EssentialState extends AbstractState {
    private ActorModule actorModule;
    
    private boolean essential;
    private boolean restore;
    
    // ---- 保存原始状态以便恢复
    private boolean oldEssential;

    @Override
    public void setData(StateData data) {
        super.setData(data);
        this.essential = data.getAsBoolean("essential", essential);
        this.restore = data.getAsBoolean("restore", restore);
    }

    @Override
    public void initialize() {
        super.initialize();
        actorModule = actor.getEntityModule().getModule(ActorModule.class);
        
        oldEssential = actorModule.isEssential();
        actorModule.setEssential(essential);
    }

    @Override
    public void cleanup() {
        if (restore && actorModule != null) {
            actorModule.setEssential(oldEssential);
        }
        super.cleanup();
    }
    
}