/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.layer.network;

import name.huliqing.luoying.Inject;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.task.Task;

/**
 *
 * @author huliqing
 */
public interface TaskNetwork extends Inject {
    
    /**
     * 完成指定的任务
     * @param actor
     * @param task
     */
    void completeTask(Entity actor, Task task);
    
    /**
     * 增加或减少任务物品的数量,任务物品并不作为普通物品一样存放在角色包裹上
     * ,因为任务物品不能使用、删除
     * @param actor 角色
     * @param task 任务
     * @param itemId 任务物品ID
     * @param amount 要增加或减少的任务物品数量，可正可负 
     */
    void applyItem(Entity actor, Task task, String itemId, int amount);
    
    
}
