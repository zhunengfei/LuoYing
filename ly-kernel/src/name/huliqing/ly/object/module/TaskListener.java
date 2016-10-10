/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.object.module;

import name.huliqing.ly.object.entity.Entity;
import name.huliqing.ly.object.task.Task;

/**
 * 监听角色接受任务、完成任务、等操作
 * @author huliqing
 */
public interface TaskListener {
    
    /**
     * 当角色添加了一个任务后触发该方法，即在接受了任务之后该方法会立即被调用。
     * @param actor 新添加了任务的角色
     * @param task 新添加的任务
     */
    void onTaskAdded(Entity actor, Task task);
    
    /**
     * 当角色被移除了一个任务后触发该方法。
     * @param actor 被移除了任务的角色
     * @param taskRemoved 已被移除的任务
     */
    void onTaskRemoved(Entity actor, Task taskRemoved);
    
    /**
     * 当角色完成了某个任务的时候触发该方法。
     * @param actor
     * @param task 
     */
    void onTaskCompleted(Entity actor, Task task);
}
