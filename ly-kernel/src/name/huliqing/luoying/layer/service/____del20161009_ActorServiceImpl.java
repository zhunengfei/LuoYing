package name.huliqing.luoying.layer.service;

///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package name.huliqing.ly.layer.service;
//
//import com.jme3.animation.LoopMode;
//import com.jme3.bounding.BoundingBox;
//import com.jme3.math.ColorRGBA;
//import com.jme3.math.FastMath;
//import com.jme3.math.Ray;
//import com.jme3.math.Vector3f;
//import com.jme3.util.TempVars;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import name.huliqing.ly.Ly;
//import name.huliqing.ly.Factory;
//import name.huliqing.ly.utils.NpcNameUtils;
//import name.huliqing.ly.object.actor.Actor;
//import name.huliqing.ly.data.ActorData;
//import name.huliqing.ly.enums.Sex;
//import name.huliqing.ly.view.talk.Talk;
//import name.huliqing.ly.object.Loader;
//import name.huliqing.ly.view.talk.SpeakManager;
//import name.huliqing.ly.view.talk.TalkManager;
//import name.huliqing.ly.xml.DataFactory;
//import name.huliqing.ly.object.module.ActorListener;
//import name.huliqing.ly.object.channel.Channel;
//import name.huliqing.ly.utils.GeometryUtils;
//import name.huliqing.ly.utils.Temp;
//import name.huliqing.ly.object.module.ActorModule;
//import name.huliqing.ly.object.module.ChannelModule;
//import name.huliqing.ly.object.module.LevelModule;
//
///**
// *
// * @author huliqing
// */
//public class ActorServiceImpl implements ActorService {
//
//    private static final Logger LOG = Logger.getLogger(ActorServiceImpl.class.getName());
//
//    private PlayService playService;
//    private SkillService skillService;
//    
//    @Override
//    public void inject() {
//        playService = Factory.get(PlayService.class);
//        skillService = Factory.get(SkillService.class);
//    }
//
//    @Override
//    public Actor loadActor(String actorId) {
//        ActorData data = DataFactory.createData(actorId);
//        return loadActor(data);
//    }
//
//    @Override
//    public Actor loadActor(ActorData actorData) {
//        Actor actor = Loader.loadActor(actorData);
//        actor.initialize();
//        return actor;
//    }
//    
////    @Override
////    public String createRandomName(Sex sex) {
////        return NpcNameUtils.createRandomName(sex);
////    }
//    
//    @Override
//    public boolean hasObstacleActor(Actor self, List<Actor> actors) {
//        TempVars tv = TempVars.get();
//        Temp temp = Temp.get();
//        
//        Vector3f origin = tv.vect1.set(self.getSpatial().getWorldBound().getCenter());
//        Vector3f dir = tv.vect2.set(getViewDirection(self)).normalizeLocal();
//        float zExtent = GeometryUtils.getBoundingVolumeZExtent(self.getSpatial());
//        origin.addLocal(dir.mult(zExtent, tv.vect3));
//        
////        DebugDynamicUtils.debugArrow(self.toString(), origin, dir, zExtent);
//        
//        // 检查碰撞
////        float checkDistance = zExtent;
//        float checkDistance = zExtent * 1.5f; // modify20160504, * 1.5f,稍微加大了一点距离
//        float checkDistanceSquare = checkDistance * checkDistance;
//        Vector3f targetOrigin = tv.vect4;
//        
//        boolean obstacle = false;
//        Ray ray = temp.ray;
//        ray.setOrigin(origin);
//        ray.setDirection(dir);
//        ray.setLimit(checkDistance);
//        for (Actor a : actors) {
//            if (a == self) {
//                continue;
//            }
//            
//            // 如果距离跳过checkDistance,则不视为障碍（该判断用于优化性能）
//            // 减少ray检测
//            targetOrigin.set(a.getSpatial().getWorldBound().getCenter());
//            if (targetOrigin.distanceSquared(origin) > checkDistanceSquare) {
//                continue;
//            }
//            
//            // 使用ray也可以，但是使用WorldBound可能性能更好一些。
//            if (a.getSpatial().getWorldBound().intersects(ray)) {
//                obstacle = true;
//                break;
//            }
//        }
//        
//        // 释放缓存
//        tv.release();
//        temp.release();
//        return obstacle;
//    }
//
//    @Override
//    public Actor findNearestEnemyExcept(Actor actor, float maxDistance, Actor except) {
//        List<Actor> actors = Ly.getPlayState().getActors();
//        float minDistanceSquared = maxDistance * maxDistance;
//        float distanceSquared;
//        Actor enemy = null;
//        for (Actor target : actors) {
//            if (target == except) { // 被排除的角色（同一实例）
//                continue;
//            }
//            // 负值的派系不作为敌人搜寻
//            if (getGroup(target) < 0) {
//                continue;
//            }
//            // 角色已经死亡或同一派别
//            if (isDead(target) || !isEnemy(target, actor)) {
//                continue;
//            }
//            // 判断可视范围内的敌人.
//            distanceSquared = target.getSpatial().getWorldTranslation()
//                    .distanceSquared(actor.getSpatial().getWorldTranslation());
//            if (distanceSquared < minDistanceSquared) { // 找出最近的敌人
//                minDistanceSquared = distanceSquared;
//                enemy = target;
//            }
//        }
//        return enemy;
//    }
//    
//    @Override
//    public List<Actor> findNearestEnemies(Actor actor, float maxDistance, List<Actor> store) {
//        if (store == null) {
//            store = new ArrayList<Actor>();
//        }
//        List<Actor> actors = Ly.getPlayState().getActors();
//        float maxDistanceSquared = maxDistance * maxDistance;
//        float distanceSquared;
//        for (Actor target : actors) {
//            // 负值的派系不作为敌人搜寻
//            if (getGroup(target) < 0) {
//                continue;
//            }
//            // 角色已经死亡或同一派别
//            if (isDead(target) || !isEnemy(target, actor)) {
//                continue;
//            }
//            // 找出范围内的敌人
//            distanceSquared = target.getSpatial().getWorldTranslation()
//                    .distanceSquared(actor.getSpatial().getWorldTranslation());
//            if (distanceSquared <= maxDistanceSquared) { 
//                store.add(target);
//            }
//        }
//        return store;
//    }
//    
//    @Override
//    public List<Actor> findNearestFriendly(Actor actor, float maxDistance, List<Actor> store) {
//        if (store == null) {
//            store = new ArrayList<Actor>();
//        }
//        List<Actor> actors = Ly.getPlayState().getActors();
//        for (Actor a : actors) {
//            if (isDead(a) || isEnemy(a, actor) 
//                    || a.getSpatial().getWorldTranslation().distance(actor.getSpatial().getWorldTranslation()) > maxDistance) {
//                continue;
//            }
//            if (getGroup(a) == getGroup(actor)) {
//                store.add(a);
//            }
//        }
//        return store;
//    }
//
//    @Override
//    public List<Actor> findNearestActors(Actor actor, float maxDistance, List<Actor> store) {
//        return findNearestActors(actor, maxDistance, 360, store);
//    }
//
//    @Override
//    public List<Actor> findNearestActors(Actor actor, float maxDistance, float angle, List<Actor> store) {
//        if (angle <= 0)
//            return store;
//        if (store == null) {
//            store = new ArrayList<Actor>();
//        }
//        float maxDistanceSquared = FastMath.pow(maxDistance, 2);
//        List<Actor> actors = Ly.getPlayState().getActors();
//        float halfAngle = angle * 0.5f;
//        for (Actor a : actors) {
//            
//            if (a == actor || a.getSpatial().getWorldTranslation().distanceSquared(actor.getSpatial().getWorldTranslation()) > maxDistanceSquared) 
//                continue;
//            
//            if (angle >= 360 || getViewAngle(actor, a.getSpatial().getWorldTranslation()) < halfAngle) {
//                store.add(a);
//            }
//        }
//        return store;
//    }
//
//    @Override
//    public float getHeight(Actor actor) {
//        BoundingBox bb = (BoundingBox) actor.getSpatial().getWorldBound();
//        return bb.getXExtent() * 2;
//    }
//
//    @Override
//    public void setPartner(Actor owner, Actor partner) {
//        // 设置为同一派别,及所有者
//        setGroup(partner, getGroup(owner));
//        setOwner(partner, owner.getData().getUniqueId());
//        setFollow(partner, owner.getData().getUniqueId());
//    }
//
//    @Override
//    public void speak(Actor actor, String mess, float useTime) {
//        SpeakManager.getInstance().doSpeak(actor, mess, useTime);
//    }
//
//    @Override
//    public void talk(Talk talk) {
//        // 不要在这里设置setNetwork(false),这会覆盖掉actorNetwork.talk的设置
//        // 因为actorNetwork.talk是直接调用actorService.talk的方法
////        talk.setNetwork(false);
//        
//        TalkManager.getInstance().startTalk(talk);
//    }
//
//    // remove201609228
////    @Override
////    public Vector3f getLocalToWorld(Actor actor, Vector3f localPos, Vector3f store) {
////        if (store == null) {
////            store = new Vector3f();
////        }
////        actor.getSpatial().getWorldRotation().mult(localPos, store);
////        store.addLocal(actor.getSpatial().getWorldTranslation());
////        return store;
////    }
//
//    // remove20160925
////    @Override
////    public boolean checkAndLoadAnim(Actor actor, String animName) {
////        if (animName == null) {
////            return false;
////        }
////        AnimControl ac = actor.getSpatial().getControl(AnimControl.class);
////        if (ac.getAnim(animName) != null) {
////            return true;
////        } else {
////            return ActorModelLoader.loadExtAnim(actor, animName);
////        }
////    }
//
//    @Override
//    public void kill(Actor actor) {
//        getActorModule(actor).kill();
//        skillService.playSkill(actor, skillService.getSkillDeadDefault(actor), false);
//    }
//    
//    @Override
//    public void reborn(Actor actor) {
//        getActorModule(actor).resurrect();
//        skillService.playSkill(actor, skillService.getSkillWaitDefault(actor), false);
//    }
//    
//    @Override
//    public void setTarget(Actor actor, Actor target) {
//        ActorModule actorModule = actor.getModule(ActorModule.class);
//        if (actorModule == null)
//            return;
//        
//        actorModule.setTarget(target);
//    }
//
//    @Override
//    public Actor getTarget(Actor actor) {
//        ActorModule actorModule = actor.getModule(ActorModule.class);
//        if (actorModule == null)
//            return null;
//        
//        return actorModule.getTarget();
//    }
//
//    @Override
//    public boolean isDead(Actor actor) {
//        if (!actor.isInitialized()) {
//            return true;
//        }
//        return getActorModule(actor).isDead();
//    }
//    
//    @Override
//    public boolean isEnemy(Actor actor, Actor target) {
//        if (target == null)
//            return false;
//        return actor.getModule(ActorModule.class).isEnemy(target);
//    }
//
//    @Override
//    public void setColor(Actor actor, ColorRGBA color) {
//        GeometryUtils.setColor(actor.getSpatial(), color);
//    }
//
//    @Override
//    public void hitNumberAttribute(Actor beHit, Actor hitter, String hitAttribute, float hitValue) {
//        getActorModule(beHit).applyHit(hitter, hitAttribute, hitValue);
//    }
//
//    @Override
//    public int getLevel(Actor actor) {
//        LevelModule module = actor.getModule(LevelModule.class);
//        if (module != null) {
//            return module.getLevel();
//        }
//        return 0;
//    }
//
//    @Override
//    public void setLevel(Actor actor, int level) {
//        LevelModule module = actor.getModule(LevelModule.class);
//        if (module != null) {
//            module.setLevel(level);
//        }
//    }
//    
//    @Override
//    public void addActorListener(Actor actor, ActorListener actorListener) {
//        getActorModule(actor).addActorListener(actorListener);
//    }
//
//    @Override
//    public boolean removeActorListener(Actor actor, ActorListener actorListener) {
//        return getActorModule(actor).removeActorListener(actorListener);
//    }
//    
//    @Override
//    public void setName(Actor actor, String name) {
//        actor.getData().setName(name);
//        actor.getSpatial().setName(name);
//    }
//
//    @Override
//    public String getName(Actor actor) {
//        return actor.getData().getName();
//    }
//    
//    @Override
//    public int getGroup(Actor actor) {
//        return actor.getModule(ActorModule.class).getGroup();
//    }
//    
//    @Override
//    public void setGroup(Actor actor, int group) {
//        actor.getModule(ActorModule.class).setGroup(group);
//    }
//
//    @Override
//    public int getTeam(Actor actor) {
//        return actor.getModule(ActorModule.class).getTeam();
//    }
//    
//    @Override
//    public void setTeam(Actor actor, int team) {
//        actor.getModule(ActorModule.class).setTeam(team);
//        Ly.getPlayState().getTeamView().checkAddOrRemove(actor);
//    }
//    
//    @Override
//    public boolean isEssential(Actor actor) {
//        return actor.getModule(ActorModule.class).isEssential();
//    }
//
//    @Override
//    public void setEssential(Actor actor, boolean essential) {
//        actor.getModule(ActorModule.class).setEssential(essential);
//    }
//
//    @Override
//    public boolean isBiology(Actor actor) {
//        return actor.getModule(ActorModule.class).isBiology(); 
//    }
//
//    @Override
//    public void setOwner(Actor actor, long ownerId) {
//        actor.getModule(ActorModule.class).setOwner(ownerId);
//    }
//
//    @Override
//    public long getOwner(Actor actor) {
//        return actor.getModule(ActorModule.class).getOwner();
//    }
//
//    @Override
//    public void setFollow(Actor actor, long targetId) {
//        actor.getModule(ActorModule.class).setFollowTarget(targetId);
//    }
//
//    @Override
//    public long getFollow(Actor actor) {
//        return actor.getModule(ActorModule.class).getFollowTarget();
//    }
//
//    @Override
//    public void syncTransform(Actor actor, Vector3f location, Vector3f viewDirection) {
//        if (location != null)
//            setLocation(actor, location);
//        
//        if(viewDirection != null)
//            setViewDirection(actor, viewDirection);
//    }
//
//    @Override
//    public void syncAnimation(Actor actor, String[] channelIds, String[] animNames, byte[] loopModes, float[] speeds, float[] times) {
//        if (channelIds == null) 
//            return;
//        
//        ChannelModule module = actor.getModule(ChannelModule.class);
//        if (module == null)
//            return;
//        
//        for (int i = 0; i < channelIds.length; i++) {
//            Channel channel = module.getChannel(channelIds[i]);
//            if (channel == null)
//                continue;
//            if (animNames[i] == null) 
//                continue;
//            
//            // 同步动画通道时先解锁，因为可能存在一些正处于锁定状态的通道。
//            // 比如正在抽取武器的过程，手部通道可能会锁定。同步完动画之后再把状态设置回去。
//            boolean oldLocked = channel.isLocked();
//            channel.setLock(false);
//            byte loopByte = loopModes[i];
//            LoopMode lm = LoopMode.DontLoop;
//            if (loopByte == 1) {
//                lm = LoopMode.Loop;
//            } else if (loopByte == 2) {
//                lm = LoopMode.Cycle;
//            }
//            
//            // 同步动画
//            module.checkAndLoadAnim(animNames[i]);
//            channel.playAnim(animNames[i], lm, speeds[i], times[i], 0); 
//            
//            channel.setLock(oldLocked);
//        }
//    }
//
//    @Override
//    public void setLocation(Actor actor, Vector3f location) {
//        actor.getModule(ActorModule.class).setLocation(location);
//    }
//
//    @Override
//    public Vector3f getLocation(Actor actor) {
//        return actor.getSpatial().getWorldTranslation();
//    }
//
//    @Override
//    public void setPhysicsEnabled(Actor actor, boolean enabled) {
//        actor.getModule(ActorModule.class).setEnabled(enabled);
//    }
//    
//    @Override
//    public boolean isPhysicsEnabled(Actor actor) {
//        return actor.getModule(ActorModule.class).isEnabled();
//    }
//    
//    @Override
//    public void setViewDirection(Actor actor, Vector3f viewDirection) {
//        actor.getModule(ActorModule.class).setViewDirection(viewDirection);
//    }
//    
//    @Override
//    public Vector3f getViewDirection(Actor actor) {
//        ActorModule module = actor.getModule(ActorModule.class);
//        if (module != null) {
//            return module.getViewDirection();
//        }
//        return null;
//    }
//
//    @Override
//    public void setLookAt(Actor actor, Vector3f position) {
//        ActorModule module = actor.getModule(ActorModule.class);
//        if (module != null) {
//            module.setLookAt(position);
//        }
//    }
//
//    @Override
//    public void setWalkDirection(Actor actor, Vector3f walkDirection) {
//        ActorModule module = actor.getModule(ActorModule.class);
//        if (module != null) {
//            module.setWalkDirection(walkDirection);
//        }
//    }
//
//    @Override
//    public Vector3f getWalkDirection(Actor actor) {
//        ActorModule module = actor.getModule(ActorModule.class);
//        if (module != null) {
//            return module.getWalkDirection();
//        }
//        return null;
//    }
//   
//    // remove20160926
////    @Override
////    public void playAnim(Actor actor, String animName, LoopMode loop, float useTime, float startTime, String... channelIds) {
////        ChannelModule module = actor.getModule(ChannelModule.class);
////        if (module != null) {
////            module.playAnim(animName, channelIds, loop, useTime, startTime);
////        }
////    }
//    
//    @Override
//    public void setChannelLock(Actor actor, boolean locked, String... channelIds) {
//        ChannelModule module = actor.getModule(ChannelModule.class);
//        if (module != null) {
//            module.setChannelLock(locked, channelIds);
//        }
//    }
//
//    @Override
//    public void restoreAnimation(Actor actor, String animName, LoopMode loop, float useTime, float startTime, String... channelIds) {
//        ChannelModule module = actor.getModule(ChannelModule.class);
//        if (module != null) {
//            module.restoreAnimation(animName, channelIds, loop, useTime, startTime);
//        }
//    }
//
//    @Override
//    public boolean reset(Actor actor) {
//        ChannelModule module = actor.getModule(ChannelModule.class);
//        if (module != null) {
//            module.reset();
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void resetToAnimationTime(Actor actor, String animation, float timePoint) {
//        ChannelModule module = actor.getModule(ChannelModule.class);
//        if (module != null) {
//            module.resetToAnimationTime(animation, timePoint);
//        }
//    }
//    
//    @Override
//    public boolean isPlayer(Actor actor) {
//        return actor.getModule(ActorModule.class).isPlayer();
//    }
//
//    @Override
//    public void setPlayer(Actor actor, boolean player) {
//        actor.getModule(ActorModule.class).setPlayer(player);
//    }
//    
//    @Override
//    public float getViewAngle(Actor actor, Vector3f position) {
//        // 优化性能
//        TempVars tv = TempVars.get();
//        Vector3f view = tv.vect1.set(getViewDirection(actor)).normalizeLocal();
//        Vector3f dir = tv.vect2.set(position).subtractLocal(actor.getSpatial().getWorldTranslation()).normalizeLocal();
//        float dot = dir.dot(view);
//        float angle = 90;
//        if (dot > 0) {
//            angle = (1.0f - dot) * 90;
//        } else if (dot < 0) {
//            angle = -dot * 90 + 90;
//        } else {
////            angle = 90;
//        }
//        tv.release();
//        return angle;
//    }
//
//    @Override
//    public float getMass(Actor actor) {
//        ActorModule module = actor.getModule(ActorModule.class);
//        return module != null ? module.getMass() : 0;
//    }
//
//    @Override
//    public boolean isKinematic(Actor actor) {
//        ActorModule module = actor.getModule(ActorModule.class);
//        return module != null ? module.isKinematic() : false;
//    }
//
//    @Override
//    public void setKinematic(Actor actor, boolean kinematic) {
//        ActorModule module = actor.getModule(ActorModule.class);
//        if (module != null) {
//            module.setKinematic(kinematic);
//        }
//    }
//    
//    private ActorModule getActorModule(Actor actor) {
//        ActorModule module = actor.getModule(ActorModule.class);
//        if (module == null) {
//            LOG.log(Level.WARNING, "Actor need a ActorModule!!! actorId={0}", actor.getData().getId());
//        }
//        return module;
//    }
//
//    @Override
//    public float distance(Actor actor, Actor target) {
//        return actor.getSpatial().getWorldTranslation().distance(target.getSpatial().getWorldTranslation());
//    }
//
//    @Override
//    public float distanceSquared(Actor actor, Actor target) {
//        return actor.getSpatial().getWorldTranslation().distanceSquared(target.getSpatial().getWorldTranslation());
//    }
//
//    @Override
//    public float distance(Actor actor, Vector3f position) {
//        return actor.getSpatial().getWorldTranslation().distance(position);
//    }
//
//    @Override
//    public boolean isAvailableEnemy(ActorModule actor, ActorModule target) {
//        if (target == null) {
//            return false;
//        }
//        if (target.isDead()) {
//            return false;
//        }
//        if (!actor.isEnemy(target.getActor())) {
//            return false;
//        }
//        if (!playService.isInScene(target.getActor())) {
//            return false;
//        }
//        return true;
//    }
//
//}