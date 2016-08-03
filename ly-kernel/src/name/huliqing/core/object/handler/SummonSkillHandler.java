/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.handler;

import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.core.Config;
import name.huliqing.core.Factory;
import name.huliqing.core.constants.IdConstants;
import name.huliqing.core.constants.ResConstants;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.data.HandlerData;
import name.huliqing.core.xml.ProtoData;
import name.huliqing.core.enums.MessageType;
import name.huliqing.core.mvc.network.PlayNetwork;
import name.huliqing.core.mvc.service.ItemService;
import name.huliqing.core.mvc.service.SkillService;
import name.huliqing.core.manager.ResourceManager;
import name.huliqing.core.object.skill.Skill;
import name.huliqing.core.object.skill.SummonSkill;

/**
 * 召唤角色，但是需要学会召唤技能先，成功召唤后目标角色的物品会减少
 * @deprecated 以后使用ItemSkillHandler代替
 * @author huliqing
 * @since 1.2
 */
public class SummonSkillHandler extends AbstractHandler {
    private final PlayNetwork playNetwork = Factory.get(PlayNetwork.class);
    private final SkillService skillService = Factory.get(SkillService.class);
    private final ItemService itemService = Factory.get(ItemService.class);
    
    // 召换哪一个角色
    private String actorId;
    // 需要依赖的技能
    private String skillId;

    @Override
    public void setData(HandlerData data) {
        super.setData(data);
        this.actorId = data.getAttribute("actorId");
        this.skillId = data.getAttribute("skillId");
    }

    @Override
    public boolean canUse(Actor actor, ProtoData data) {
        if (!super.canUse(actor, data)) {
            return false;
        }
        if (actorId == null) {
            if (Config.debug) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "No actorId set, summonActor do nothing!");
            }
            return false;
        }
        // 需要召唤技能
        Skill skill = skillService.getSkillInstance(actor, skillId);
        if (skill == null || !(skill instanceof SummonSkill)) {
            String skillName = ResourceManager.getObjectName(IdConstants.SKILL_SUMMON);
            playNetwork.addMessage(actor, 
                    ResourceManager.get(ResConstants.SKILL_NEED_SKILL, new Object[]{skillName}), MessageType.notice);
            return false;
        }
        return true;
    }

    @Override
    protected void useObject(Actor actor, ProtoData data) {
        Skill skill = skillService.getSkillInstance(actor, skillId);
        if (skill == null || !(skill instanceof SummonSkill)) {
            return;
        }
        
        // TODO: 重构，需要解除依赖
        SummonSkill ssk = (SummonSkill) skill;
        ssk.setSummonActorId(actorId);
        
        if (skillService.playSkill(actor, ssk.getSkillData().getId(), false)) {
//            remove(actor, data.getId(), 1);
            itemService.removeItem(actor, data.getId(), 1);
        }
    }

}