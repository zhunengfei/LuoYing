///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package name.huliqing.fighter.loader.data;
//
//import java.util.ArrayList;
//import java.util.List;
//import name.huliqing.fighter.data.DropData;
//import name.huliqing.fighter.data.DropItem;
//import name.huliqing.fighter.data.Proto;
//import name.huliqing.fighter.utils.ConvertUtils;
//
///**
// *
// * @author huliqing
// */
//public class DropDataLoader implements DataLoader<DropData>{
//
//    @Override
//    public DropData loadData(Proto proto) {
//        DropData data = new DropData(proto.getId());
//        
//        // 基本掉落物品,format: id1|count, count可省略，默认为1.
//        List<DropItem> baseItems = null;
//        String[] baseDrop = proto.getAsArray("base");
//        if (baseDrop != null) {
//            baseItems = new ArrayList<DropItem>(baseDrop.length);
//            for (String drop : baseDrop) {
//                String[] dropArr = drop.split("\\|");
//                DropItem di = new DropItem(dropArr[0]);
//                if (dropArr.length >= 2) {
//                    di.setCount(ConvertUtils.toInteger(dropArr[1], 1));
//                }
//                baseItems.add(di);
//            }
//        }
//        
//        // 随机掉落物品,format: id1|count|factor
//        // factor和count可省略，默认factor=1,count=1
//        List<DropItem> randomItems = null;
//        String[] randomDrop = proto.getAsArray("random");
//        if (randomDrop != null) {
//            randomItems = new ArrayList<DropItem>(randomDrop.length);
//            for (String drop : randomDrop) {
//                String[] dropArr = drop.split("\\|");
//                DropItem di = new DropItem(dropArr[0]);
//                if (dropArr.length >= 2) {
//                    di.setCount(ConvertUtils.toInteger(dropArr[1], 1));
//                }
//                if (dropArr.length >= 3) {
//                    di.setFactor(ConvertUtils.toFloat(dropArr[2], 1));
//                }
//                randomItems.add(di);
//            }
//        }
//        
//        data.setBaseItems(baseItems);
//        data.setRandomItems(randomItems);
//        return data;
//    }
//    
//}