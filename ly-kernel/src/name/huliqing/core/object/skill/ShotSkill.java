/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.skill;

import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import java.util.ArrayList;
import java.util.List;
import name.huliqing.core.Factory;
import name.huliqing.core.data.SkillData;
import name.huliqing.core.mvc.network.StateNetwork;
import name.huliqing.core.mvc.service.ActorService;
import name.huliqing.core.mvc.service.BulletService;
import name.huliqing.core.mvc.service.MagicService;
import name.huliqing.core.mvc.service.PlayService;
import name.huliqing.core.mvc.service.StateService;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.object.bullet.Bullet;
import name.huliqing.core.object.bullet.Bullet.Listener;
import name.huliqing.core.utils.ConvertUtils;
import name.huliqing.core.utils.MathUtils;

/**
 * 射击技能的基本类
 * @author huliqing
 * @param <T>
 */
public class ShotSkill<T extends SkillData> extends HitSkill<T> {

    private final ActorService actorService = Factory.get(ActorService.class);
    private final PlayService playService = Factory.get(PlayService.class);
    private final StateService stateService = Factory.get(StateService.class);
    private final BulletService bulletService = Factory.get(BulletService.class);
    private final MagicService magicService = Factory.get(MagicService.class);
    private final StateNetwork stateNetwork = Factory.get(StateNetwork.class);
    
    // 靶点类型
    protected enum ShotTargetType {
        /** 目标原点 */
        origin,
        /** Model bound的中心点 */
        center,
        /** 整个model bound */
        bound,
        ;
        public static ShotTargetType identifyByName(String name) {
            ShotTargetType[] stts = ShotTargetType.values();
            for (ShotTargetType stt : stts) {
                if (stt.name().equals(name)) {
                    return stt;
                }
            }
            return null;
        }
    }
    
    // 子弹id,关联到一个子弹(bullet.xml)
    protected String[] bullets;
    // 从哪一个位置开始发射，取值0~1， 0表示一开始发射，1表示技能执行完后发射
    protected float[] shotTimes;
    // 发射的起始位置，相对于角色的本地坐标
    protected Vector3f[] shotOffsets;
    // 发射的速度,默认1为标准速度，大于1则加大速度，小于1则减少速度
    protected float shotSpeed = 1;
    // 是否允许攻击到多个敌人，默认情况下只能攻击到当前的目标敌人。
    // 如果打开该功能，则在攻击范围内的敌人都可能被攻击到。比如挥剑
    // 的时候可能同时击中多个敌人。默认值false
    protected boolean multHit;
    // 是否射击多个目标，如果为false,则只射击当前角色的主要目标
    // 否则射击在一定范围内的目标
    protected boolean multTarget;
    // 靶点类型
    protected ShotTargetType shotTargetType = ShotTargetType.center;
    
    // ---- 内部
    
    private int indexBullet;
    private int indexShotOffset;
    // 经过cuttime处理后的实际shottime
    private float[] trueShotTimes;
    protected final PointChecker shotChecker = new PointChecker();
    
    // 在攻击技能范围内的目标，每次技能过后清空
    private int indexTarget;
    private final List<Actor> tempTargets = new ArrayList<Actor>();
    
    @Override
    public void setData(T data) {
        super.setData(data); 
        bullets = data.getAsArray("bullets");
        shotTimes = data.getAsFloatArray("shotTimes");
        trueShotTimes = new float[shotTimes.length];
        // 格式, "x|y|z,x|y|z,x|y|z,..."
        String[] tempShotOffsets = data.getAsArray("shotOffsets");
        if (tempShotOffsets != null) {
            shotOffsets = new Vector3f[tempShotOffsets.length];
            for (int i = 0; i < shotOffsets.length; i++) {
                String[] xyz = tempShotOffsets[i].split("\\|");
                Vector3f offset = ConvertUtils.toVector3f(xyz, null);
                shotOffsets[i] = offset;
            }
        } else {
           shotOffsets = new Vector3f[]{Vector3f.ZERO};
        }
        shotSpeed = data.getAsFloat("shotSpeed", shotSpeed);
        multHit = data.getAsBoolean("multHit", multHit);
        multTarget = data.getAsBoolean("multTarget", multTarget);
        String tempSTT = data.getAsString("shotTargetType");
        if (tempSTT != null) {
            shotTargetType = ShotTargetType.identifyByName(tempSTT);
        }
    }
    
    /**
     * 获取下一个ShotOffset位置
     * @return 
     */
    protected Vector3f getShotOffset() {
        if (indexShotOffset >= shotOffsets.length) {
            indexShotOffset = 0;
        }
        // 取得当前索引位置的offset
        return shotOffsets[indexShotOffset++];
    }
    
    /**
     * 获取下一个子弹ID
     * @return 
     */
    protected String getShotBullet() {
        if (indexBullet >= bullets.length)
            indexBullet = 0;
        return bullets[indexBullet++];
    }
    
    // 获取下一个目标
    protected Actor getShotTarget() {
        if (tempTargets.isEmpty()) 
            return null;
        if (indexTarget >= tempTargets.size())
            indexTarget = 0;
        return tempTargets.get(indexTarget++);
    }

    @Override
    protected void init() {
        super.init(); 
        
        for (int i = 0; i < shotTimes.length; i++) {
            float shotTime = MathUtils.clamp(fixTimePointByCutTime(shotTimes[i]), 0f, 1f);
            trueShotTimes[i] = shotTime;
        }
        shotChecker.setMaxTime(trueUseTime);
        shotChecker.setCheckPoint(trueShotTimes);
        shotChecker.rewind();
         
        // 应该在一开始时就确定目标，以避免在执行logic的过程被从服务端而来的消息
        // 命令改变了目标或清除了目标
         
        tempTargets.clear();
        if (multTarget) {
            // 多目标攻击
            getCanHitActors(tempTargets, true);
        } else {
            // 单目标攻击
            Actor mainTarget = actorService.getTarget(actor);
            if (mainTarget != null && isInHitDistance(mainTarget)) {
                tempTargets.add(mainTarget);
            }
        }
    }
    
    @Override
    protected void doUpdateLogic(float tpf) {
        checkShot();
    }

    /**
     * 检查逻辑，确认是否进行shot
     */
    protected void checkShot() {
        while (shotChecker.nextPoint(time) != -1) {
            doShotTarget();
        }
    }
    
    protected void doShotTarget() {
        final Actor mainTarget = getShotTarget();
        
        if (mainTarget == null) {
            return;
        }
        // 在所有可用的子弹类型中轮循
        // 在所有可用的Offset设置中轮循
        String bullet = getShotBullet();
        Vector3f offset = getShotOffset();
    
        Vector3f startPoint = new Vector3f(offset);
        Bullet bb = bulletService.loadBullet(bullet);
        bb.getData().setStartPoint(convertToWorldPos(startPoint));
        bb.getData().setEndPoint(getShotEndPoint(mainTarget));
        bb.getData().setSpeed(shotSpeed);
        
        bb.addListener(new Listener() {
            @Override
            public boolean onBulletFlying(Bullet bullet) {
                 if (shotHitCheck(bullet, mainTarget)) {
                    // 击中后结束子弹运行。
                    bullet.consume();
                    return true;
                }
                return false;
            }
        });
        playService.addBullet(bb);
    }
    
    /**
     * 获取射击的目标结束点
     * @param target
     * @return 
     */
    private Vector3f getShotEndPoint(Actor target) {
        switch (shotTargetType) {
            case bound:
            case center:
                return target.getModel().getWorldBound().getCenter();
            case origin:
            default:
                return target.getModel().getWorldTranslation();
        }
    }
    
    /**
     * 获取shotOffset的实际世界坐标点位置
     * @param store
     * @return 
     */
    protected Vector3f convertToWorldPos(Vector3f store) {
        TempVars tv = TempVars.get();
        tv.quat1.lookAt(actorService.getViewDirection(actor), Vector3f.UNIT_Y);
        tv.quat1.mult(store, store);
        store.addLocal(actor.getModel().getWorldTranslation());
        tv.release();
        return store;
    }
    
    protected boolean shotHitCheck(Bullet bullet, Actor mainTarget) {
        // 注：这里是为了提高性能，只有在击中主目标之后才会进行伤害检测。
        // 即使打开了multHit也必须在击中主目标之后才开始全部检测。否则在子弹
        // 飞行过程中作全面检测会非常伤性能。
        if (!isBulletHit(bullet, mainTarget)) {
            return false;
        }
        
        // 击中主目标
        applyHit(mainTarget);

        // 如果允许多重伤害时，则需要判断其他可能在攻击范围内的敌人
        if (multHit) {
            List<Actor> targets = playService.findAllActor();
            for (Actor t : targets) {
                if (t == mainTarget) {
                    continue;
                }
                if (isBulletHit(bullet, t)) {
                    applyHit(t);
                }
            }
        }
        return true;
    }
    
    // 判断子弹是否击中指定角色。
    private boolean isBulletHit(Bullet bullet, Actor target) {
        switch (shotTargetType) {
            case bound:
                return bullet.isHit(target.getModel());
            case center:
                return bullet.isHit(target.getModel().getWorldBound().getCenter());
            case origin:
                return bullet.isHit(target.getModel().getWorldTranslation());
        }
        return false;
    }

    @Override
    public void cleanup() {
        indexBullet = 0;
        indexShotOffset = 0;
        indexTarget = 0;
        super.cleanup();
    }
   
}
