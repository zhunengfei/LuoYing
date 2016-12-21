/*
 * LuoYing is a program used to make 3D RPG game.
 * Copyright (c) 2014-2016 Huliqing <31703299@qq.com>
 * 
 * This file is part of LuoYing.
 *
 * LuoYing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LuoYing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LuoYing.  If not, see <http://www.gnu.org/licenses/>.
 */
package name.huliqing.luoying.object.entity.impl;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import name.huliqing.luoying.LuoYing;
import name.huliqing.luoying.object.entity.ModelEntity;
import name.huliqing.luoying.object.entity.TerrainEntity;
import name.huliqing.luoying.object.scene.Scene;
import name.huliqing.luoying.object.scene.SceneListener;
import name.huliqing.luoying.object.scene.SceneListenerAdapter;

/**
 * 植被环境物体，如：花草等物体
 * @author huliqing
 */
public abstract class PlantEntity extends ModelEntity {
    
    private SceneListener sceneListener;

    @Override
    protected Spatial loadModel() {
        Spatial temp = LuoYing.getAssetManager().loadModel(data.getAsString("file"));
        return temp;
    }
    
    @Override
    public void onInitScene(Scene scene) {
        super.onInitScene(scene);
        if (scene.isInitialized()) {
            // 把植皮移到地形上面
            makePlantOnTerrain(scene);
        } else {
            sceneListener = new SceneListenerAdapter() {
                @Override
                public void onSceneLoaded(Scene scene) {
                    // 把植皮移到地形上面
                    makePlantOnTerrain(scene);
                    // 在处理完位置点之后就可以不再需要侦听了
                    scene.removeSceneListener(this);
                }
            };
            scene.addSceneListener(sceneListener);
        }
    }
    
    @Override
    public void cleanup() {
        if (sceneListener != null) {
            scene.removeSceneListener(sceneListener);
        }
        super.cleanup();
    }

    private void makePlantOnTerrain(Scene scene) {
        // 在场景载入完毕之后将植皮位置移到terrain节点的上面。
        List<TerrainEntity> sos = scene.getEntities(TerrainEntity.class, new ArrayList<TerrainEntity>());
        Vector3f location = null;
        for (TerrainEntity terrain : sos) {
            Vector3f terrainPoint = terrain.getHeight(spatial.getWorldTranslation().x, spatial.getWorldTranslation().z);
            if (terrainPoint != null) {
                if (location == null || terrainPoint.y > location.y) {
                    location = terrainPoint;
                }
            }
        }
        
        if (location != null) {
            location.addLocal(0, -0.1f, 0); // 下移一点
            RigidBodyControl rbc = spatial.getControl(RigidBodyControl.class);
            if (rbc != null) {
                rbc.setPhysicsLocation(location);
            } else {
                spatial.setLocalTranslation(location);
            }
        } 
    }

    
}
