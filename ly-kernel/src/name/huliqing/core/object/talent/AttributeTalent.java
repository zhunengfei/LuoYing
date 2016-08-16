/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.talent;

import name.huliqing.core.Factory;
import name.huliqing.core.data.TalentData;
import name.huliqing.core.mvc.service.AttributeService;
import name.huliqing.core.mvc.service.ElService;

/**
 *
 * @author huliqing
 * @param <T>
 */
public class AttributeTalent<T extends TalentData> extends AbstractTalent<T> {
    private final ElService elService = Factory.get(ElService.class);
    private final AttributeService attributeService = Factory.get(AttributeService.class);
    
    protected String applyAttribute;
    protected String levelEl;
    
    // ----
    private int level;
    private float applyValue;
    private boolean init;

    @Override
    public void setData(T data) {
        super.setData(data); 
        this.applyAttribute = data.getAsString("applyAttribute");
        this.levelEl = data.getAsString("levelEl");
        this.level = data.getLevel();
    }

    @Override
    public void init() {
        if (init) {
            return;
        }
        applyValue = getLevelValue(levelEl, level);
        attributeService.applyStaticValue(actor, applyAttribute, applyValue);
        attributeService.applyDynamicValue(actor, applyAttribute, applyValue);
        attributeService.clampDynamicValue(actor, applyAttribute);
        init = true;
    }

    @Override
    protected void doLogic(float tpf) {
        // dologic
    }

    @Override
    public void cleanup() {
        if (init) {
            attributeService.applyStaticValue(actor, applyAttribute, -applyValue);
            attributeService.applyDynamicValue(actor, applyAttribute, -applyValue);
            attributeService.clampDynamicValue(actor, applyAttribute);
            init = false;
        }
    }

    @Override
    public void updateLevel(int level) {
        cleanup();
        this.level = level;
        init();
    }
    
    protected float getLevelValue(String levelEl, int level) {
        float levelValue = elService.getLevelEl(levelEl, level);
        return levelValue;
    }
    
}
