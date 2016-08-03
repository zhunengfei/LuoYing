/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.scene;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import name.huliqing.core.data.EnvData;
import name.huliqing.core.xml.Proto;
import name.huliqing.core.data.SceneData;
import name.huliqing.core.object.DataFactory;
import name.huliqing.core.xml.DataLoader;

/**
 * 用于载入场景数据
 * @author huliqing
 * @param <T>
 */
public class SceneDataLoader<T extends SceneData> implements DataLoader<T> {

    @Override
    public void load(Proto proto, T store) {        
        // 环境物体
        String[] envIds = proto.getAsArray("envs");
        if (envIds != null && envIds.length > 0) {
            List<EnvData> edStore = store.getEnvs();
            if (edStore == null) {
                edStore = new ArrayList<EnvData>(envIds.length);
                store.setEnvs(edStore);
            }
            for (String eid : envIds) {
                EnvData ed = DataFactory.createData(eid);
                edStore.add(ed);
            }
        }
    }
    
}
