/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.object.env;

import com.jme3.math.Vector3f;

/**
 * 定义水体环境
 * @author huliqing
 */
public interface WaterEnv {
    
    /**
     * 判断目标位置点是否位于水体下面。
     * @param point
     * @return 
     */
    boolean isUnderWater(Vector3f point);
}
