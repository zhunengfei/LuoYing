/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.editor.edit.controls.ControlTile;
import name.huliqing.editor.edit.select.AdvanceWaterEntityControlTile;
import name.huliqing.editor.edit.select.DirectionalLightEntityControlTile;
import name.huliqing.editor.edit.select.EmptyEntityControlTile;
import name.huliqing.editor.edit.select.EntityControlTile;
import name.huliqing.editor.edit.select.SimpleModelEntityControlTile;

/**
 *
 * @author huliqing
 */
public class ControlTileManager {

    private static final Logger LOG = Logger.getLogger(ControlTileManager.class.getName());
    
    private final static Map<String, Class<? extends EntityControlTile>> MAPPING = new HashMap();
    
    static {
        MAPPING.put("entitySimpleModel", SimpleModelEntityControlTile.class);
        MAPPING.put("entitySimpleWater", SimpleModelEntityControlTile.class);
        MAPPING.put("entitySimpleTerrain", SimpleModelEntityControlTile.class);
        MAPPING.put("entityTree", SimpleModelEntityControlTile.class);
        MAPPING.put("entityGrass", SimpleModelEntityControlTile.class);
        MAPPING.put("entityDirectionalLight", DirectionalLightEntityControlTile.class);
        MAPPING.put("entityAdvanceWater", AdvanceWaterEntityControlTile.class);
        
        MAPPING.put("entitySkyBox", EmptyEntityControlTile.class);
        MAPPING.put("entityAmbientLight", EmptyEntityControlTile.class);
        MAPPING.put("entityDirectionalLightFilterShadow", EmptyEntityControlTile.class);
        MAPPING.put("entityChaseCamera", EmptyEntityControlTile.class);
        MAPPING.put("entityPhysics", EmptyEntityControlTile.class);
    }
    
    public final static <T extends EntityControlTile> T createEntityControlTile(String tagName) {
        Class<? extends ControlTile> clazz = MAPPING.get(tagName);
        if (clazz == null) {
            LOG.log(Level.WARNING, "Could not create select obj, unknow tagName={0}", tagName);
            return (T) new EmptyEntityControlTile();
        }
         
        ControlTile obj;
        try {
            obj = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            obj = new EmptyEntityControlTile();
            Logger.getLogger(ControlTileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (T) obj;
    }
}
