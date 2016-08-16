/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.skill;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import name.huliqing.core.Factory;
import name.huliqing.core.constants.IdConstants;
import name.huliqing.core.constants.SkillConstants;
import name.huliqing.core.data.MagicData;
import name.huliqing.core.data.SkillData;
import name.huliqing.core.mvc.network.StateNetwork;
import name.huliqing.core.mvc.service.ActorService;
import name.huliqing.core.mvc.service.ElService;
import name.huliqing.core.mvc.service.HitCheckerService;
import name.huliqing.core.mvc.service.MagicService;
import name.huliqing.core.mvc.service.PlayService;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.object.el.HitEl;
import name.huliqing.core.object.hitchecker.HitChecker;
import name.huliqing.core.object.magic.Magic;
import name.huliqing.core.utils.ConvertUtils;

/**
 * Hit技能，可与目标角色进行交互的技能。例如攻击技能，BUFF技能，射击技能。
 * @author huliqing
 * @param <T>
 */
public abstract class HitSkill<T extends SkillData> extends AbstractSkill<T> {
    private final ActorService actorService = Factory.get(ActorService.class);
    private final PlayService playService = Factory.get(PlayService.class);
    private final MagicService magicService = Factory.get(MagicService.class);
    private final ElService elService = Factory.get(ElService.class);
    private final HitCheckerService hitCheckerService = Factory.get(HitCheckerService.class);
    private final StateNetwork stateNetwork = Factory.get(StateNetwork.class);
    
    protected HitChecker hitChecker;
    // 指定HIT目标的哪一个属性
    protected String hitAttribute;
    // HIT的属性值，可正，可负。
    protected float hitValue;
    // Hit公式，这个公式用于计算当前技能对目标角色可以产生的影响值。
    protected String hitEl;
    // hit的距离限制，注：hitDistance和hitAngle决定了hit的面积,举例，
    // 如果 hitDistance = 3, hitAngle=360， 则在角色周围3码内的目标都在这个技能范围内
    protected float hitDistance = 3;
    // Hit的角度范围限制
    protected float hitAngle = 30;
    // HIT后可添加到目标角色上时的状态列表
    protected List<SkillStateWrap> hitStates;
    // HIT后可添加到目标角色上的魔法
    protected String[] hitMagics;
    
    // ---- inner
    private TargetsComparator sorter;
    // 缓存hitDistance,优化inHitDistance
    private float hitDistanceSquared;
    
    @Override
    public void setData(T data) {
        super.setData(data); 
        
        this.hitChecker = hitCheckerService.loadHitChecker(data.getAsString("hitChecker", IdConstants.HIT_CHECKER_FIGHT_DEFAULT));
        this.hitAttribute = data.getAsString("hitAttribute");
        this.hitValue = data.getAsFloat("hitValue", 0);
        this.hitEl = data.getAsString("hitEl");
        this.hitDistance = data.getAsFloat("hitDistance", hitDistance);
        this.hitDistanceSquared = hitDistance * hitDistance;
        this.hitAngle = data.getAsFloat("hitAngle", hitAngle);
        
        // 状态和机率，　格式："stateId1|factor, stateId2|factor"
        String[] tempHitStates = data.getAsArray("hitStates");
        if (tempHitStates != null && tempHitStates.length > 0) {
            hitStates = new ArrayList<SkillStateWrap>(tempHitStates.length);
            String[] tempArr;
            for (String ts : tempHitStates) {
                if (ts.trim().equals("")) {
                    continue;
                }
                tempArr = ts.split("\\|");
                SkillStateWrap sw = new SkillStateWrap();
                sw.stateId = tempArr[0];
                if (tempArr.length >= 2) {
                    sw.factor = ConvertUtils.toFloat(tempArr[1], 1.0f);
                } else {
                    sw.factor = 1.0f;
                }
                hitStates.add(sw);
            }
        }
        hitMagics = data.getAsArray("hitMagics");
    }

    @Override
    protected void init() {
        // 注：这里target必须不能是自己(actor),否则在faceTo时会导致在执行animation
        // 的时候模型的头发和武器错位,即不能faceTo自己，所以target != actor的判断必须的。
        Actor target = actorService.getTarget(actor);
        if (target != null && target != actor) {
            actorService.setLookAt(actor, actorService.getLocation(target));
        }
        
        super.init();
    }
    
    @Override
    protected float getSkillValue() {
        float lv = getLevelValue();
        if (lv != -1) {
            return lv * hitValue;
        }
        return hitValue;
    }
    
    /**
     * 获取技能范围内及限制角度内的所有角色，但不包含当前角色自身.
     * @param store
     * @param sort 是否进行排序（从离当前角色近到远进行排序）
     */
    protected void getCanHitActors(List<Actor> store, boolean sort) {
        // 查找角度限制范围内的敌人。
        actorService.findNearestActors(actor, hitDistance, hitAngle, store);

        // 移除不能作为目标的角色,注：mainTarget要单独处理
        Actor mainTarget = actorService.getTarget(actor);
        Iterator<Actor> it = store.iterator();
        Actor temp;
        while (it.hasNext()) {
            temp = it.next();
            if (temp == mainTarget || !hitChecker.canHit(actor, temp)) {
                it.remove();
            }
        }
        
        // 进行排序，从离当前角色“近到远”
        if (sort) {
            if (sorter == null) {
                sorter = new TargetsComparator();
            }
            Collections.sort(store, sorter);
        }
        
        // 添加角色自身
        if (hitChecker.canHit(actor, actor)) {
            store.add(0, actor);
        }
        
        // 添加主目标（注意避免重覆）
        // 主目标是放在最优先，然后是自身角色
        if (mainTarget != actor && hitChecker.canHit(actor, mainTarget)) {
            store.add(0, mainTarget);
        }
    }
    
    /**
     * HIT目标角色。由子类调用
     * @param target 
     */
    protected void applyHit(Actor target) {
        // 角色刚好已经脱离场景，则什么也不做。
        if (!playService.isInScene(target)) {
            return;
        }
        if (hitChecker.canHit(actor, target)) {
            // 状态和魔法先添加，然后再添加applyHit.
            // 1.因为applyHit后角色可能死亡，这个时候如果再添加状态，可能会导致角色
            // 重新做出一些奇怪动作，因为状态和魔法可能会让角色重新执行一些技能,如reset,
            // 而本应该是“死亡”动作。
            // 2.可能一些状态如“冰冻”要冻住目标角色，如果先执行applyHit可能会
            // 一直冻住“受伤”时的角色动画，无法冻住其它角色动画。
            applyStates(target, hitStates);
            applyMagics(target, hitMagics);
            if (hitAttribute != null) {
                applyHit(actor, target, getSkillValue(), hitEl, hitAttribute);
            }
        }
    }
    
    /**
     * 给角色添加魔法
     * @param target
     * @param hitMagics 
     */
    private void applyMagics(Actor target, String[] hitMagics) {
        if (hitMagics == null)
            return;
        for (String mId : hitMagics) {
            MagicData md = magicService.loadMagic(mId);
            md.setSourceActor(actor.getData().getUniqueId());
            md.setTargetActor(target.getData().getUniqueId());
            md.setTraceActor(target.getData().getUniqueId());
            Magic magic = magicService.loadMagic(md);
            playService.addPlayObject(magic);
        }
    }
    
    private void applyStates(Actor target, List<SkillStateWrap> stateWraps) {
        if (stateWraps == null)
            return;
        
        // remove20160408,移除 !target.isDead(),看下面解释
//        // 当角色死亡时不添加状态到角色身上。
//        if (!target.isDead()) {
//            for (SkillStateWrap sw : stateWraps) {
//                if (sw.factor >= FastMath.nextRandomFloat()) {
//                    // 状态存在机率影响，为同步服务端与客户端状态所以统以服务端为准。
//                    stateNetwork.addState(target, sw.stateId);
//                }
//            }
//        }
        
        // 在角色死亡的时候仍可以添加状态，以避免角色刚好被射死后却没有看到状态特效.
        // 如：当寒冰箭刚好射击"死"敌人后应允许添加状态，否则会看不到冰冻效果
        // 因为“冰冻效果”是在状态上的，只有冰冻状态运行的时候才能看到。
        for (SkillStateWrap sw : stateWraps) {
            if (sw.factor >= FastMath.nextRandomFloat()) {
                // 状态存在机率影响，为同步服务端与客户端状态所以统以服务端为准。
                stateNetwork.addState(target, sw.stateId, actor);
            }
        }
    }
    
    /**
     * @param attacker 攻击者
     * @param target 被攻击者
     * @param skillValue 技能值
     * @param hitEl 技能计算公式
     * @param attribute 指定攻击的是哪一个属性
     */
    private void applyHit(Actor attacker, Actor target, float skillValue, String hitEl, String attribute) {
        if (hitEl == null) {
            return;
        }
        float finalValue = 0;
        HitEl de = elService.getHitEl(hitEl);
        if (de != null) {
            finalValue = de.getValue(attacker, skillValue, target);
        }
        HitUtils.getInstance().applyHit(attacker, target, hitAttribute, finalValue);
    }
    
    // remove20160517 后续使用HitUtils代替
//    /**
//     * @param attacker 攻击者
//     * @param target 被攻击者
//     * @param skillValue 技能值
//     * @param hitEl 技能计算公式
//     * @param attribute 指定攻击的是哪一个属性
//     */
//    private void applyHit(Actor attacker, Actor target, float skillValue, String hitEl, String attribute) {
//        if (hitEl == null) {
//            return;
//        }
//        // 记住HIT之前属性值。
//        float oldAttrValue = attributeService.getDynamicValue(target, attribute);
//        
//        float finalValue = 0;
//        HitEl de = elService.getHitEl(hitEl);
//        if (de != null) {
//            finalValue = de.getValue(attacker, skillValue, target);
//        }
//        actorNetwork.hitAttribute(target, attacker, hitAttribute, finalValue);
//        
//        if (actorService.isDead(target)) {
//            
//            // 1.让死亡目标执行死亡技能
//            SkillData deadSkill = skillService.getSkill(target, SkillType.dead);
//            if (deadSkill != null) {
//                skillNetwork.playSkill(target, deadSkill.getId(), false);
//            }
//
//            // 2.通知目标角色死亡
//            if (target.isPlayer()) {
//                // 告诉所有玩家
//                String attackerName = actorService.getName(attacker);
//                String targetName = actorService.getName(target);
//                String killedMess = ResourceManager.get("common.killed", new Object[] {targetName, attackerName});
//                playNetwork.addMessage(killedMess, MessageType.notice);
//                // 告诉目标:"你已经死亡"
//                playNetwork.addMessage(target, ResourceManager.get("common.dead"), MessageType.notice);
//            }
//            
//            // 3.对攻击者奖励经验
//            int xpReward = actorService.getXpReward(attacker, target);
//            actorNetwork.applyXp(attacker, xpReward);
//            
//            // 4.对攻击者奖励物品
//            List<ProtoData> dropItems = dropService.getRandomDropFull(target, null);
//            for (ProtoData item : dropItems) {
//                itemNetwork.addItem(attacker, item.getId(), item.getTotal());
//            }
//            
//            // remove0221由 actorNetwork.applyXp(attacker, xpReward);内部处理
////            // 5.对玩家提示经验获得,注：这里的玩家可能为其他客户端的玩家
////            // 需要向特定的客户端发送通知"经验获得"。
////            if (attacker.isPlayer()) {
////                playNetwork.addMessage(attacker, ResourceManager.get(ResConstants.COMMON_GET_XP, new Object[]{xpReward}), MessageType.info);
////            }
//        } else {
//            // 注：hit技能不一定是减益的。当目标受hit后属性值降低才是减益技能，
//            // 当减益发生时则判断为受伤并执行hurt.
//            float newAttrValue = attributeService.getDynamicValue(target, attribute);
//            if (newAttrValue < oldAttrValue) {
//                SkillData sd = skillService.getSkill(target, SkillType.hurt);
//                if (sd != null) {
//                    skillNetwork.playSkill(target, sd.getId(), false);
//                }
//            }
//        }
//        
//        // TODO: 重构，因为hit不一定是伤害值。
//        // 播放攻击伤害数字效果
//        DamageManager.show(target, finalValue);
//    }

    @Override
    public int canPlay() {
        if (actor == null || hitChecker == null)
            return SkillConstants.STATE_UNDEFINE;
        
        Actor target = actorService.getTarget(actor);
        if (target == null)
            return SkillConstants.STATE_TARGET_NOT_FOUND;
        
        if (!isInHitDistance(target))
            return SkillConstants.STATE_TARGET_NOT_IN_RANGE;
        
        if (!hitChecker.canHit(actor, target))
            return SkillConstants.STATE_TARGET_UNSUITABLE;
        
        return SkillConstants.STATE_OK;
        
    }
    
    /**
     * 判断目标是否在技能的hit距离之内
     * @param target
     * @return 
     */
    public boolean isInHitDistance(Actor target) {
        if (target == null) {
            return false;
        }
        
        if (actorService.distanceSquared(actor, target) <= hitDistanceSquared
                || actor.getModel().getWorldBound().intersects(target.getModel().getWorldBound())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 判断目标是否在技能的hit角度之内
     * @param target
     * @return 
     */
    public boolean isInHitAngle(Actor target) {
        // remove20160504
//        // 如果在技能的攻击视角之外，则视为false(限制distance > 1是避免当距离太近时角度判断不正确。)
//        boolean inAngle = actor.getViewAngle(target.getModel().getWorldTranslation()) * 2 < hitAngle;
//        if (!inAngle && actor.getDistanceSquared(target) > 1) {
//            return false;
//        }
//        return true;
        
        // 如果在技能的攻击视角之外，则视为false(限制distance > 1是避免当距离太近时角度判断不正确。)
        boolean inAngle = actorService.getViewAngle(actor, target.getModel().getWorldTranslation()) * 2 < hitAngle;
        return inAngle;
    }
    
    /**
     * 用于来对目标进行排序，按距离当前角色从近到远
     */
    private class TargetsComparator implements Comparator<Actor> {
        @Override
        public int compare(Actor o1, Actor o2) {
            Vector3f selfPos = actorService.getLocation(actor);
            float dis1 = o1.getModel().getWorldTranslation().distanceSquared(selfPos);
            float dis2 = o2.getModel().getWorldTranslation().distanceSquared(selfPos);
            if (dis1 < dis2) {
                return -1;
            } else if (dis1 > dis2) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
