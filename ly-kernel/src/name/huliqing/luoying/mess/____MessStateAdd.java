///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package name.huliqing.luoying.mess;
//
//import com.jme3.network.serializing.Serializable;
//import name.huliqing.luoying.Factory;
//import name.huliqing.luoying.layer.service.PlayService;
//import name.huliqing.luoying.layer.service.StateService;
//import name.huliqing.luoying.object.entity.Entity;
//
///**
// *
// * @author huliqing
// */
//@Serializable
//public class MessStateAdd extends MessBase {
//    
//    // 状态添加者的ID
//    private long actorId;
//    // 状态ID
//    private String stateId;
//    // 对于状态的抵消率{0.0~1.0}
//    private float resist;
//    // 源角色（产生这个状态的角色）的唯一ID，如果不存在这样一个角色，则该值为小于或等于0的值。
//    private long sourceActorId;
//
//    public long getActorId() {
//        return actorId;
//    }
//
//    public void setActorId(long actorId) {
//        this.actorId = actorId;
//    }
//
//    public String getStateId() {
//        return stateId;
//    }
//
//    public void setStateId(String stateId) {
//        this.stateId = stateId;
//    }
//
//    public float getResist() {
//        return resist;
//    }
//
//    public void setResist(float resist) {
//        this.resist = resist;
//    }
//
//    public long getSourceActorId() {
//        return sourceActorId;
//    }
//
//    public void setSourceActorId(long sourceActorId) {
//        this.sourceActorId = sourceActorId;
//    }
//
//    @Override
//    public void applyOnClient() {
//        PlayService playService = Factory.get(PlayService.class);
//        StateService stateService = Factory.get(StateService.class);
//        Entity actor = playService.getEntity(actorId);
//        Entity sourceActor = playService.getEntity(sourceActorId);
//        if (actor != null) {
//            stateService.addStateForce(actor, stateId, resist, sourceActor);
//        }
//    }
//    
//}
