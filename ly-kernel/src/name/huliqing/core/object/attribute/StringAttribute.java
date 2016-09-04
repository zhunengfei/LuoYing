/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.attribute;

import name.huliqing.core.data.AttributeData;

/**
 * 字符串类型的属性
 * @author huliqing
 */
public class StringAttribute extends AbstractSimpleAttribute<String> {

    @Override
    public void setData(AttributeData data) {
        super.setData(data); 
        value = data.getAsString("value");
    }
    
    // 更新data值，以避免在外部使用data时获取不到实时的数据
    protected void updateData() {
        data.setAttribute("value", value);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        updateData();
    }
    
    @Override
    protected void notifyValueChangeListeners(String oldValue, String newValue) {
        if (!oldValue.equals(newValue)) {
            super.notifyValueChangeListeners(oldValue, newValue); 
        }
    }
    
    public boolean match(final Object other) {
        if (other instanceof Number) {
            return value.equals(other.toString());
        }
        return value.equals(other);
    }
    
    
}