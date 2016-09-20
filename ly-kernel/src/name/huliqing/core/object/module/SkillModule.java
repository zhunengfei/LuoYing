/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.module;

import com.jme3.scene.control.Control;
import com.jme3.util.SafeArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import name.huliqing.core.constants.SkillConstants;
import name.huliqing.core.data.SkillData;
import name.huliqing.core.data.ModuleData;
import name.huliqing.core.object.Loader;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.object.skill.Skill;
import name.huliqing.core.object.skill.SkillTagFactory;

/**
 * @author huliqing
 */
public class SkillModule extends AbstractModule {
    private Actor actor;
    private Control updateControl;
    
    // 所有可用的技能处理器
    private final List<Skill> skills = new ArrayList<Skill>();
    // 一个map,用于优化获取查找技能的速度，skillMap中的内容与skills中的是一样的。
    private final Map<String, Skill> skillMap = new HashMap<String, Skill>();
    // 临听角色技能的执行和结束 
    private List<SkillListener> skillListeners;
    private List<SkillPlayListener> skillPlayListeners;
    
    // 当前正在执行的技能列表,
    // 如果runningSkills都执行完毕,则清空.但是lastSkill仍保持最近刚刚执行的技能的引用.
    private final SafeArrayList<Skill> runningSkills = new SafeArrayList<Skill>(Skill.class);
    // 当前正在运行的所有技能的类型，其中每一个二进制位表示一个技能标记。
    private long runningSkillTags;
    // 当前正在执行中的技能中优先级最高的值。
    private int runningPriorMax;
    
    // 最近一个执行的技能,这个技能可能正在执行，也可能已经停止。
    private Skill lastSkill;
    
    // 被锁定的技能列表，二进制表示，其中每1个二进制位表示一个技能类型。
    // 如果指定的位为1，则表示这个技能类型被锁定，则这类技能将不能执行。
    // 默认值0表示没有任何锁定。
    private long lockedSkillTags;
    
    // 默认的技能tag
    private String waitSkillTag;
    private String deadSkillTag;
    private Skill waitSkill;
    private Skill deadSkill;

    @Override
    public void setData(ModuleData data) {
        super.setData(data);
        lockedSkillTags = data.getAsLong("lockedSkillTags", 0);
        waitSkillTag = data.getAsString("waitSkillTag", "wait");
        deadSkillTag = data.getAsString("deadSkillTag", "dead");
    }
    
    protected void updateData() {
        data.setAttribute("lockedSkillTags", lockedSkillTags);
    }

    @Override
    public void initialize(Actor actor) {
        super.initialize(actor);
        this.actor = actor;
        
        // 技能的更新支持
        updateControl = new AdapterControl() {
            @Override
            public void update(float tpf) {skillUpdate(tpf);}
        };
        this.actor.getSpatial().addControl(updateControl);
        
        // 载入技能
        List<SkillData> skillDatas = actor.getData().getObjectDatas(SkillData.class, null);
        if (skillDatas != null && !skillDatas.isEmpty()) {
            for (SkillData sd : skillDatas) {
                addSkill((Skill) Loader.load(sd));
            }
        }
    }
    
    private void skillUpdate(float tpf) {
        // 更新并尝试移除已经结束的技能
        if (!runningSkills.isEmpty()) {

            int oldSize = runningSkills.size();
            for (Skill skill : runningSkills.getArray()) {
                if (skill.isEnd()) {
                    runningSkills.remove(skill);
                    // 执行侦听器
                    if (skillPlayListeners != null && !skillPlayListeners.isEmpty()) {
                        for (int i = 0; i < skillPlayListeners.size(); i++) {
                            skillPlayListeners.get(i).onSkillEnd(actor, skill);
                        }
                    }
                } else {
                    skill.update(tpf);
                }
            }
            
            // 如果有技能执行完了，并被移除了.
            // 1.重新缓存runningSkillTags
            // 2.重新缓存技能中最高优先级的值。
            // 3.修复、重启部分被覆盖的动画通道的动画，比如在走动时取武器后双手应该重新回到走动时的协调运动。
            if (runningSkills.size() != oldSize) {
                runningSkillTags = 0;
                for (Skill skill : runningSkills.getArray()) {
                    runningSkillTags |= skill.getData().getTags();
                    if (skill.getData().getPrior() > runningPriorMax) {
                        runningPriorMax = skill.getData().getPrior();
                    }
                    skill.restoreAnimation();
                }
            }
            
//            if (Config.debug) {
//                Log.get(getClass()).log(Level.INFO, "skillProcessor runningSkills.size={0}", runningSkills.size());
//            }
        }
    }
    
    @Override
    public void cleanup() {
        for (Skill skill : runningSkills.getArray()) {
            if (!skill.isEnd()) {
                skill.cleanup();
            }
        }
        runningSkills.clear();
        skills.clear();
        skillMap.clear();
        runningSkillTags = 0;
        if (updateControl != null) {
            actor.getSpatial().removeControl(updateControl);
        }
        super.cleanup();
    }
    
    /**
     * 添加一个新技能给角色,如果相同ID的技能已经存在，则该方法什么也不会处理。
     * @param skill 
     */
    public void addSkill(Skill skill) {
        if (skills.contains(skill))
            return;
        
        skill.setActor(actor);
        skills.add(skill);
        skillMap.put(skill.getData().getId(), skill);
        actor.getData().addObjectData(skill.getData());
        
        // 通知侦听器
        if (skillListeners != null) {
            for (int i = 0; i < skillListeners.size(); i++) {
                skillListeners.get(i).onSkillAdded(actor, skill);
            }
        }
    }
    
    /**
     * 从角色身上移除一个技能，注：被移除的技能必须是已经存在于角色身上的技能实例，
     * 否则该方法什么也不做，并返回false.<BR>
     * 使用{@link #getSkill(java.lang.String) } 来确保从当前角色身上获得一个存在的技能实例。
     * @param skill
     * @return 
     */
    public boolean removeSkill(Skill skill) {
        if (!skills.contains(skill)) 
            return false;
        
        skills.remove(skill);
        skillMap.remove(skill.getData().getId());
        actor.getData().removeObjectData(skill.getData());
        skill.cleanup();
        
        // 通知侦听器
        if (skillListeners != null) {
            for (int i = 0; i < skillListeners.size(); i++) {
                skillListeners.get(i).onSkillRemoved(actor, skill);
            }
        }
        return true;
    }
    
    /**
     * 从角色身上获取一个技能，如果角色没有指定ID的技能则返回null.
     * @param skillId
     * @return 
     */
    public Skill getSkill(String skillId) {
        return skillMap.get(skillId);
    }
    
    /**
     * 获取角色当前所有的技能，注意：返回的技能列表只能作为<b>只读</b>使用.
     * key=skillId, value=Skill
     * @return 
     */
    public List<Skill> getSkills() {
        return Collections.unmodifiableList(skills);
    }
    
    /**
     * 查找拥有指定tags的所有技能。
     * @param skillTags
     * @return 
     */
    public List<Skill> getSkillByTags(long skillTags) {
        if (skills == null) {
            return null;
        }
        List<Skill> tagSkills = new ArrayList<Skill>();
        for (Skill s : skills) {
            if ((s.getData().getTags() & skillTags) != 0) {
                tagSkills.add(s);
            }
        }
        return tagSkills;
    }
    
    /**
     * 获取“等待”的技能，如果没有则返回null.
     * @return 
     */
    public Skill getSkillWait() {
        if (waitSkill == null && waitSkillTag != null) {
            List<Skill> temp = getSkillByTags(SkillTagFactory.convert(waitSkillTag));
            if (temp != null && !temp.isEmpty()) {
                waitSkill = temp.get(0);
            }
        }
        return waitSkill;
    }
    
    /**
     * 获取“死亡”的技能，如果没有则返回null.
     * @return 
     */
    public Skill getSkillDead() {
        if (deadSkill == null && deadSkillTag != null) {
            List<Skill> temp = getSkillByTags(SkillTagFactory.convert(deadSkillTag));
            if (temp != null && !temp.isEmpty()) {
                deadSkill = temp.get(0);
            }
        }
        return deadSkill;
    }
    
    /**
     * 添加技能侦听器
     * @param skillListener 
     */
    public void addSkillListener(SkillListener skillListener) {
        if (skillListeners == null) {
            skillListeners = new ArrayList<SkillListener>();
        }
        if (!skillListeners.contains(skillListener)) {
            skillListeners.add(skillListener);
        }
    }
    
    /**
     * 移除技能侦听器
     * @param skillListener
     * @return 
     */
    public boolean removeSkillListener(SkillListener skillListener) {
        return skillListeners != null && skillListeners.remove(skillListener);
    }
    
    /**
     * 添加技能"执行“侦听器
     * @param skillPlayListener 
     */
    public void addSkillPlayListener(SkillPlayListener skillPlayListener) {
        if (skillPlayListeners == null) {
            skillPlayListeners = new ArrayList<SkillPlayListener>();
        }
        if (!skillPlayListeners.contains(skillPlayListener)) {
            skillPlayListeners.add(skillPlayListener);
        }
    }
    
    /**
     * 移除技能"执行"侦听器
     * @param skillPlayListener
     * @return 
     */
    public boolean removeSkillPlayListener(SkillPlayListener skillPlayListener) {
        return skillPlayListeners != null && skillPlayListeners.remove(skillPlayListener);
    }
    
    /**
     * 检查技能在当前状态下是否可以执行，如果返回值为 {@link SkillConstants#STATE_OK} 则表示可以执行，
     * 否则不能执行。
     * @param skill
     * @return 
     */
    public int checkStateCode(Skill skill) {
        SkillData skillData = skill.getData();
        
        // 如果技能被锁定中，则不能执行
        if (isLockedSkillTags(skill.getData().getTags())) {
            return SkillConstants.STATE_SKILL_LOCKED;
        }
        
        // 如果新技能自身判断不能执行，例如加血技能或许就不可能给敌军执行。
        // 有很多特殊技能是不能对一些特定目标执行的，所以这里需要包含技能自身的判断
        int stateCode = skill.canPlay(actor);
        if (stateCode != SkillConstants.STATE_OK) {
            return stateCode;
        }
        
        // 通过钩子来判断是否可以执行, 如果有一个钩子返回不允许执行则后面不再需要判断。
        if (skillPlayListeners != null && !skillPlayListeners.isEmpty()) {
            for (SkillPlayListener sl : skillPlayListeners) {
                if (!sl.onSkillHookCheck(actor, skill)) {
                    return SkillConstants.STATE_HOOK;
                }
            }
        }
        
        // 如果当前正在执行的所有技能都可以被新技能覆盖，则直接返回OK.
        // 注意：overlaps判断要放在interrupts判断之前，因为如果可以覆盖正在执行
        // 的技能就不需要中断它们
        if ((skillData.getOverlapTags() & runningSkillTags) == runningSkillTags) {
            return SkillConstants.STATE_OK;
        }
        
        // 如果当前正在执行的所有技能都可以被新技能打断，则直接返回OK.
        if ((skillData.getInterruptTags() & runningSkillTags) == runningSkillTags) {
            return SkillConstants.STATE_OK;
        }
        
        if (skillData.getPrior() > runningPriorMax) {
            return SkillConstants.STATE_OK;
        }
        
        return SkillConstants.STATE_CAN_NOT_INTERRUPT;
    }
    
    // remove20160920
//    /**
//     * 执行"等待"技能
//     * @param force
//     * @return 
//     */
//    public boolean playWait(boolean force) {
//        if (waitSkill != null) {
//            return playSkill(waitSkill, force);
//        }
//        return false;
//    }
//    
//    /**
//     * 执行"死亡"技能
//     * @param force
//     * @return 
//     */
//    public boolean playDead(boolean force) {
//        if (deadSkill != null) {
//            return playSkill(deadSkill, force);
//        }
//        return false;
//    }
    
    /**
     * 执行技能，如果成功执行则返回true,否则返回false, <br>
     * 在执行技能之前可以通过 {@link #checkStateCode(Skill) }来查询当前状态下技能是否可以执行。<br>
     * 如果需要强制执行技能，则可以将参数force设置为true,这可以保证技能始终执行。
     * @param newSkill
     * @param force
     * @return 
     */
    public boolean playSkill(Skill newSkill, boolean force) {
        if (force || checkStateCode(newSkill) == SkillConstants.STATE_OK) {
            playSkillInner(newSkill);
            return true;
        }
        return false;
    }
    
    /**
     * 强制执行一个技能,这个方法是强制执行的，如果需要判断技能是否可以合理执行,可以调用 
     * {@link #checkStateCode(Skill, boolean) }来进行判断。<br>
     * 执行逻辑是这样的：<br>
     * 1.如果当前没有任何正在执行的技能则直接执行新技能并返回,不再执行后续判断，否则继续。<br>
     * 2.如果当前有正在执行的技能，并且新技能可以与这些技能重叠执行，则执行新技能并返回，否则继续。<br>
     * 3.打断当前正在执行的所有可以打断的技能,然后执行新技能并返回。<br>
     * @param newSkill 
     */
    private void playSkillInner(Skill newSkill) {
        // 1.如果当前没有任何正在执行的技能则直接执行技能
        if (runningSkills.isEmpty()) {
            startNewSkill(newSkill);
            return;
        }
        
        // 2.如果新技能可以与正在执行的所有技能进行重叠则直接执行, 不需要再通过后面的判断。
        long overlaps = newSkill.getData().getOverlapTags();
        if (overlaps > 0 && (overlaps & runningSkillTags) == runningSkillTags) {
            startNewSkill(newSkill);
            return;
        }
        
        // 3.把可以打断的技能都打断,然后执行新技能。
        long interrupts = newSkill.getData().getInterruptTags();
        if (interrupts > 0) {
            for (Skill skill : runningSkills.getArray()) {
                if ((interrupts & skill.getData().getTags()) == skill.getData().getTags()) {
                    skill.cleanup();
                }
            }            
        }

        startNewSkill(newSkill);
    }
    
    private void startNewSkill(Skill newSkill) {
        // 执行技能
        lastSkill = newSkill;
        lastSkill.setActor(actor);
        lastSkill.initialize();
        // 记录当前正在运行的所有技能类型
        if (!runningSkills.contains(lastSkill)) {
            runningSkills.add(lastSkill);
            runningSkillTags |= lastSkill.getData().getTags();
        }
        
        // 执行侦听器
        if (skillPlayListeners != null && !skillPlayListeners.isEmpty()) {
            for (int i = 0; i < skillPlayListeners.size(); i++) {
                skillPlayListeners.get(i).onSkillStart(actor, lastSkill);
            }
        }
    }
    
    /**
     * 获取最近一个执行的技能，如果没有执行过任何技能则返回null.<br>
     * 注：返回的技能有可能正在执行，也有可能已经结束。
     * @return 
     */
    public Skill getLastSkill() {
        return lastSkill;
    }
    
    /**
     * 获取当前正在执行的所有技能。
     * @return 
     */
    public List<Skill> getRunningSkills() {
        return runningSkills;
    }
    
    /**
     * 获取当前正在执行的技能标记(tags)，返回值中每个二进制位表示一个技能(标记),<br>
     * 如果没有正在执行的任何技能则该值会返回 0. <br>
     * 通过 {@link SkillTagFactory#toStringTags(long) } 来查看tag的字符串描述
     * @return 
     * @see SkillTagFactory#toStringTags(long) 
     */
    public long getRunningSkillTags() {
        return runningSkillTags;
    }
    
    /**
     * 判断skillTags标记的技能是否正在执行。如果skillTags标记的技能中有<b>任何一个</b>正在执行则该方法返回true.
     * @param skillTags
     * @return 
     */
    public boolean isRunningSkill(long skillTags) {
        return (runningSkillTags & skillTags) != 0;
    }

    /**
     * 获取技能的锁定状态，返回的整数中每一个二进制位表示一个被锁定的技能标记(tag)
     * @return 
     */
    public long getLockedSkillTags() {
        return lockedSkillTags;
    }
    
    /**
     * 判断技能标记是否被锁定, 如果skillTags中存在<b>任何一个</b>标记被锁定则该方法将返回true.
     * @param skillTags
     * @return 
     */
    public boolean isLockedSkillTags(long skillTags) {
        return (lockedSkillTags & skillTags) != 0;
    }

    /**
     * 锁定指定技能标记的技能, 被锁定后的技能将不能执行。
     * @param skillTags 
     * @see #getLockedSkillTags() 
     */
    public void lockSkillTags(long skillTags) {
        this.lockedSkillTags |= skillTags;
        updateData();
    }
    
    /**
     * 解锁指定技能标记的技能。 
     * @param skillTags 
     */
    public void unlockSkillTags(long skillTags) {
        lockedSkillTags &= ~skillTags;
        updateData();
    }

}
