/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.loader;

import com.jme3.animation.AnimControl;
import com.jme3.animation.SkeletonControl;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3tools.optimize.GeometryBatchFactory;
import name.huliqing.fighter.Common;
import name.huliqing.fighter.Config;
import name.huliqing.fighter.Factory;
import name.huliqing.fighter.constants.IdConstants;
import name.huliqing.fighter.object.actor.ActorControl;
import name.huliqing.fighter.data.ActorData;
import name.huliqing.fighter.data.LogicData;
import name.huliqing.fighter.data.ProtoData;
import name.huliqing.fighter.data.StateData;
import name.huliqing.fighter.data.TalentData;
import name.huliqing.fighter.data.TaskData;
import name.huliqing.fighter.game.service.ConfigService;
import name.huliqing.fighter.object.action.ActionProcessor;
import name.huliqing.fighter.object.actor.Actor;
import name.huliqing.fighter.object.actor.CustomSkeletonControl;
import name.huliqing.fighter.object.channel.ChannelProcessor;
import name.huliqing.fighter.object.channel.ChannelProcessorImpl;
import name.huliqing.fighter.object.effect.Effect;
import name.huliqing.fighter.object.logic.ActorLogic;
import name.huliqing.fighter.object.logic.LogicProcessor;
import name.huliqing.fighter.object.logic.LogicProcessorImpl;
import name.huliqing.fighter.object.resist.ResistProcessor;
import name.huliqing.fighter.object.skill.SkillProcessor;
import name.huliqing.fighter.object.skill.SkillProcessorImpl;
import name.huliqing.fighter.object.state.State;
import name.huliqing.fighter.object.state.StateProcessor;
import name.huliqing.fighter.object.state.StateProcessorImpl;
import name.huliqing.fighter.object.talent.Talent;
import name.huliqing.fighter.object.talent.TalentProcessor;
import name.huliqing.fighter.object.talent.TalentProcessorImpl;
import name.huliqing.fighter.object.task.Task;
import name.huliqing.fighter.utils.ConvertUtils;
import name.huliqing.fighter.utils.GeometryUtils;

/**
 *
 * @author huliqing
 */
public class ActorModelLoader {
    private final static Logger LOG = Logger.getLogger(ActorModelLoader.class.getName());
    
    /**
     * character
     *      |- AnimControl
     *      |- SkeletonControl
     *      |- foot
     *      |- lowerBody
     *      |- upperBody
     *      |- hand
     *      |- face
     *      |- eye
     *      |- ear
     *      |- hair
     *      |- weaponLeft
     *      |- weaponRight
     * @param actor
     * @param data
     * @return 
     */
    public static Spatial loadActorModel(ActorData data, ActorControl actor) {
        // 0.==== Load base model : character
        String actorFile = data.getProto().getFile();
        
        if (Config.debug) {
            LOG.log(Level.INFO, "Load actor file={0}", actorFile);
        }
        
        // 需要确保最外层是Node类
        Spatial actorModel = AssetLoader.loadModel(actorFile);
        if (actorModel instanceof Geometry) {
            Spatial temp = actorModel;
            actorModel = new Node();
            ((Node) actorModel).attachChild(temp);
            // 当actor附带有effect时，必须把角色原始model设置为不透明的
            // 否则添加的效果可能会被角色model挡住在后面。
            temp.setQueueBucket(RenderQueue.Bucket.Opaque);
        }
        actorModel.setName(data.getName());
        actorModel.setUserData(ProtoData.USER_DATA, data);
        actorModel.setShadowMode(RenderQueue.ShadowMode.Cast);
        
        // remove 0222,放在service中处理
//        // 1.==== 是否使用硬件加速
//        checkEnableHardwareSkining(actorModel);
        
        // 4.====create character control
        
        // 4.1 注缩放必须放在碰撞盒加入之前，因为碰撞盒不能跟着缩放
        int scaleLen = data.getProto().checkAttributeLength("scale");
        if (scaleLen >= 3) {
            actorModel.setLocalScale(data.getAsVector3f("scale"));
        } else if (scaleLen == 1) {
            float s = data.getAsFloat("scale");
            actorModel.setLocalScale(new Vector3f(s,s,s));
        }
        
        // location
        Vector3f location = data.getAsVector3f("location");
        if (location != null) {
            actorModel.setLocalTranslation(location);
        }
        
        // 4.2 碰撞盒
        String collisionShape = data.getAttribute("collisionShape", "capsule");
        float collisionRadius = data.getAsFloat("collisionRadius", 0.4f);
        float collisionHeight = data.getAsFloat("collisionHeight", 2.8f);
        float mass = data.getAsFloat("mass", 0);
        
        TempVars tv = TempVars.get();
        if (collisionShape.equals("capsule")) {
            
            actor.setModel(actorModel, collisionRadius, collisionHeight, mass);
            
        } else if (collisionShape.equals("box")) {
            
            Vector3f boxScale = data.getAsVector3f("collisionBoxScale", new Vector3f(1, 1, 1)); // box碰撞盒的缩放
            BoundingBox bb = (BoundingBox)actorModel.getWorldBound();
            bb.getExtent(tv.vect1);
            tv.vect1.multLocal(boxScale);
            CompoundCollisionShape ccs = new CompoundCollisionShape();
            ccs.addChildShape(new BoxCollisionShape(tv.vect1), new Vector3f(0, tv.vect1.y, 0));
            actor.setModel(actorModel, collisionRadius, collisionHeight, mass, ccs);
            
        } else if (collisionShape.equals("mesh")) {
            
            CollisionShape cShape = CollisionShapeFactory.createMeshShape(actorModel);
            actor.setModel(actorModel, collisionRadius, collisionHeight, mass, cShape);
            
        } else if (collisionShape.equals("dynamicMesh")) {
            
            CollisionShape cShape = CollisionShapeFactory.createDynamicMeshShape(actorModel);
            actor.setModel(actorModel, collisionRadius, collisionHeight, mass, cShape);
            
        } else {
            throw new UnsupportedOperationException("Unsupported collisionShape=" + collisionShape);
        }
        tv.release();
        
        // 4.3 视角和初始正视角方向
        Vector3f forward = data.getAsVector3f("localForward");
        if (forward != null) {
            actor.setLocalForward(forward);
        }
        
        Vector3f viewDirection = data.getAsVector3f("viewDirection");
        if (viewDirection != null) {
            actor.setViewDirection(viewDirection);
        }
        
        // 6.==== 绑定特效
        String[] effects = data.getAsArray("effects");
        if (effects != null) {
            for (String eid : effects) {
                Effect ae = Loader.loadEffect(eid);
                ae.start();
                ((Node) actorModel).attachChild(ae.getDisplay());
            }
        }
        
        // =====.行为,技能,和逻辑
        LogicProcessor logicProcessor = new LogicProcessorImpl(Common.getApp(), actor);
        ActionProcessor actionProcessor = new ActionProcessor();
        SkillProcessor skillProcessor = new SkillProcessorImpl(actor);
        StateProcessor stateProcessor = new StateProcessorImpl(Common.getApp(), actor);
        ResistProcessor resistProcessor = new ResistProcessor();
        TalentProcessor talentProcessor = new TalentProcessorImpl(actor);
        actor.setLogicProcessor(logicProcessor);
        actor.setStateProcessor(stateProcessor);
        actor.setActionProcessor(actionProcessor);
        actor.setSkillProcessor(skillProcessor);
        actor.setResistProcessor(resistProcessor);
        actor.setTalentProcessor(talentProcessor);
        
        // 7.创建角色的动画通道,注意：不是所有角色都会有ChannelProcessor.
        // 如：防御塔，宝箱等一些静态类的角色就没有。
        // 对于包含骨骼动画的角色才有ChannelProcessor，对于这些角色如果指定了
        // channels，则根据设置为他们创建分别的通道，否则创建一个默认的包含
        // 所有骨骼的完整通道。单独的通道和默认全骨骼的通道不能同时使用，否则单独
        // 的通道将无效果。
        AnimControl ac = actor.getModel().getControl(AnimControl.class);
        if (ac != null) {
            ChannelProcessor acp = new ChannelProcessorImpl();
            actor.setChannelProcessor(acp);
            String[] channels = data.getAsArray("channels");
            if (channels == null) {
                channels = new String[]{IdConstants.CHANNEL_FULL};
            }
            for (String channelId : channels) {
                acp.addChannel(Loader.loadChannel(channelId, ac));
            }
        }
        
        // 8.==== 载入角色的逻辑
        List<LogicData> logics = data.getLogics();
        if (logics != null) {
            for (LogicData logicData : logics) {
                ActorLogic logic = Loader.loadLogic(logicData);
                logicProcessor.addLogic(logic);
            }
        }
        
        // 9.==== 载入角色的状态
        List<StateData> stateDatas = data.getStates();
        if (stateDatas != null) {
            for (StateData stateData : stateDatas) {
                State state = Loader.loadState(stateData);
                stateProcessor.addState(state);
            }
        }
        
        // 10.==== 载入天赋
        List<TalentData> talentDatas = data.getTalents();
        if (talentDatas != null) {
            for (TalentData td : talentDatas) {
                Talent talent = Loader.loadTalent(td);
                talentProcessor.addTalent(talent);
            }
        }
        
        // 11.==== 载入任务
        List<TaskData> tasks = data.getTasks();
        for (TaskData td : tasks) {
            Task task = Loader.loadTask(td);
            task.setActor(actor);
            task.initialize();
            actor.getTasks().add(task);
        }
        
        // 13.color
        if (data.getColor() != null) {
            GeometryUtils.setColor(actorModel, data.getColor());
        }
        
        // 14.偿试激活HardwareSkining
        checkEnableHardwareSkining(actor);
        
        return actorModel;
    }
    
    /**
     * 载入扩展的动画,该方法从角色所配置的extAnim目录中查找动画文件并进行加
     * 载。
     * @param actor
     * @param animName
     * @return 
     */
    public static boolean loadExtAnim(Actor actor, String animName) {
        String animDir = actor.getData().getAttribute("extAnim");
        if (animDir == null) {
            LOG.log(Level.WARNING, "Actor {0} no have a extAnim defined"
                    + ", could not load anim {1}", new Object[] {actor.getData().getId(), animName});
            return false;
        }
        String animFile = animDir + "/" + animName + ".mesh.j3o";
        try {
            Spatial animExtModel = AssetLoader.loadModelDirect(animFile);
            GeometryUtils.addSkeletonAnim(animExtModel, actor.getModel());
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not load extAnim, actor={0}, animName={1}, exception={2}"
                    , new Object[] {actor.getData().getId(), animName, e.getMessage()});
        }
        return false;
    }
    
    /**
     * @deprecated 暂不使用该功能，作用不是特别大
     * 检查是否打开了batch功能
     * @param actor 
     */
    public static void checkEnableBatch(Actor actor) {
        if (actor.getData().isBatchEnabled()) {
            if (actor.getModel() instanceof Node) {
                GeometryBatchFactory.optimize((Node) actor.getModel());
                String[] scaleArr = actor.getData().getAsArray("scale");
                if (scaleArr == null)
                    return;
                
                if (scaleArr.length >= 3) {
                    Vector3f scale = new Vector3f();
                    ConvertUtils.toVector3f(scaleArr, scale);
                    actor.getModel().setLocalScale(scale);
                } else {
                    actor.getModel().setLocalScale(ConvertUtils.toFloat(scaleArr[0], 1));
                }
            }
        }
    }
    
    /**
     * 检测并判断是否打开或关闭该模型的硬件skining加速
     * @param actor 角色模型
     */
    private static void checkEnableHardwareSkining(Actor actor) {
        ActorData data = actor.getData();
        SkeletonControl sc = actor.getModel().getControl(SkeletonControl.class);
        
        if (data == null || sc == null) {
            return;
        }
        
        // 全局没有打开的情况下则不处理。
        if (!Factory.get(ConfigService.class).isUseHardwareSkinning()) {
            return;
        }
        
        // 默认情冲下打开hardwareSkinning,除非在actor.xml中设置不打开。
        if (!data.getAsBoolean("hardwareSkinning", true)) {
            return;
        }
        
        // 代换自定义的SkeletonControl,因为默认的SkeletonControl会把带
        // SkeletonControl的子节点也进行处理。比如弓武器，当弓武器带有动画时可能
        // 导致角色的SkeletonControl和弓的SkeletonControl存在冲突导致弓模型变形
        CustomSkeletonControl csc = new CustomSkeletonControl(sc.getSkeleton());
        actor.getModel().removeControl(sc);
        actor.getModel().addControl(csc);
        csc.setHardwareSkinningPreferred(true);
    }
    
}