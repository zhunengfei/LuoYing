/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.object.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.constants.ResConstants;
import name.huliqing.ly.data.ChatData;
import name.huliqing.luoying.data.ObjectData;
import name.huliqing.luoying.layer.service.ItemService;
import name.huliqing.luoying.layer.service.SkinService;
import name.huliqing.ly.view.Footer;
import name.huliqing.ly.view.transfer.SimpleTransferPanel;
import name.huliqing.ly.view.transfer.TabTransferPanel;
import name.huliqing.ly.view.transfer.TransferPanel;
import name.huliqing.luoying.manager.ResourceManager;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.item.Item;
import name.huliqing.luoying.object.skin.Skin;
import name.huliqing.luoying.ui.Button;
import name.huliqing.luoying.ui.FrameLayout;
import name.huliqing.luoying.ui.LinearLayout;
import name.huliqing.luoying.ui.UI;
import name.huliqing.luoying.ui.UIFactory;
import name.huliqing.luoying.ui.Window;
import name.huliqing.luoying.xml.DataFactory;
import name.huliqing.luoying.xml.DataProcessor;
import name.huliqing.ly.layer.network.ChatNetwork;
import name.huliqing.ly.layer.network.GameNetwork;
import name.huliqing.ly.layer.service.GameService;

/**
 * 出售物品到商店类角色
 * @author huliqing
 * @param <T>
 */
public class SendChat<T extends ChatData> extends Chat<T> {
    private final ItemService itemService = Factory.get(ItemService.class);
    private final SkinService skinService = Factory.get(SkinService.class);
    private final GameService gameService = Factory.get(GameService.class);
    private final GameNetwork gameNetwork = Factory.get(GameNetwork.class);
    private final ChatNetwork chatNetwork = Factory.get(ChatNetwork.class);

    // ---- inner
    private Window win;
    private LinearLayout bodyPanel;
    private TransferPanel<DataProcessor<ObjectData>> sourcePanel;  // 玩家物品面板,显示角色已经有的物品
    private TransferPanel<DataProcessor<ObjectData>> distPanel;    // 等发送物品面板
    private CenterPanel centerPanel;    // 分隔线
    private FooterPanel footerPanel;    // 显示确认按钮
    
    // 发送者角色，一般为Player.
    private Entity sender;
    
    @Override
    protected UI createChatUI(float width, float height) {
        float titleHeight = UIFactory.getUIConfig().getTitleHeight();
        float footerHeight = height * 0.15f;
        float bodyHeight = height - titleHeight - footerHeight;
        float ppWidth = width * 0.49f;
        float cpWidth = width * 0.02f;
        float spWidth = width * 0.49f;
        
        bodyPanel = new LinearLayout(width, bodyHeight);
        sourcePanel = new TabTransferPanel(ppWidth, bodyHeight);
        centerPanel = new CenterPanel(cpWidth, bodyHeight);
        distPanel = new SimpleTransferPanel(spWidth, bodyHeight);
        
        // 双向传输
        sourcePanel.setTransfer(distPanel);
        distPanel.setTransfer(sourcePanel);
        
        bodyPanel.setLayout(LinearLayout.Layout.horizontal);
        bodyPanel.addView(sourcePanel);
        bodyPanel.addView(centerPanel);
        bodyPanel.addView(distPanel);
        
        footerPanel = new FooterPanel(width, footerHeight);
        
        win = new Window("", width, height);
        win.setCloseable(true);
        win.setDragEnabled(true);
        win.addCloseListener(new Window.CloseListener() {
            @Override
            public void onClosed(Window win) {
                scene.removeEntity(SendChat.this);
            }
        });
        
        win.addView(bodyPanel);
        win.addView(footerPanel);
        win.setToCorner(UI.Corner.CC);
        return win;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        
        // 记住卖者
        sender = gameService.getPlayer();
        
        // 初始化, 数据要复制一份出来，不要去影响角色的包裹中的数据
        List<DataProcessor> tempDatas = new ArrayList<DataProcessor>();
        List<Item> items = itemService.getItems(sender);
        if (items != null) {
            tempDatas.addAll(items);
        }
        List<Skin> skins = skinService.getSkins(sender);
        if (skins != null) {
            tempDatas.addAll(skins);
        }
        // 移除不能出售的物品
        Iterator<DataProcessor> it = tempDatas.iterator();
        while (it.hasNext()) {
            DataProcessor dp = it.next();
            if (dp instanceof Item) {
                Item item = (Item) dp;
                if (!item.getData().isSellable()) {
                    it.remove();
                    continue;
                }
            }
            if (dp instanceof Skin) {
                Skin skin = (Skin) dp;
                if (skin.isAttached() || skin.isBaseSkin() || skin.isSkinning()) {
                    it.remove();
                    continue;
                }
            }
        }
        
        List<DataProcessor<ObjectData>> transforDatas = new ArrayList<DataProcessor<ObjectData>>();
        for (DataProcessor dp : tempDatas) {
            ObjectData dataCopy = DataFactory.createData(dp.getData().getId());
            dataCopy.setTotal(((ObjectData)dp.getData()).getTotal());
            transforDatas.add(Loader.load(dataCopy));
        }
        sourcePanel.setDatas(transforDatas);
        
        // 清空dist面板
        distPanel.setDatas(Collections.EMPTY_LIST);
        
        win.setTitle(getChatName() + "-" + actor.getData().getName());
    }
    
    // 给目标发送物品
    private void send() {
        List<DataProcessor<ObjectData>> datas = distPanel.getDatas();
        if (datas.isEmpty()) {
            scene.removeEntity(this);
            return;
        }
        String[] objects = new String[datas.size()];
        int[] counts = new int[datas.size()];
        DataProcessor<ObjectData> pd;
        for (int i = 0; i < datas.size(); i++) {
            pd = datas.get(i);
            objects[i] = pd.getData().getId();
            counts[i] = pd.getData().getTotal();
        }
        chatNetwork.chatSend(sender, actor, objects, counts);
        scene.removeEntity(this);
    }
    
    // -------------------------------------------------------------Center Panel
    private class CenterPanel extends FrameLayout {
        public CenterPanel(float width, float height) {
            super(width, height);
            setBackground(UIFactory.getUIConfig().getBackground(), false);
            setBackgroundColor(UIFactory.getUIConfig().getFooterBgColor(), false);
        }
    }
    
    // -------------------------------------------------------------Footer Panel
    private class FooterPanel extends Footer {

        private Button submit;
        
        public FooterPanel(float width, float height) {
            super(width, height);
            submit = new Button(ResourceManager.get(ResConstants.COMMON_SEND));
            submit.addClickListener(new Listener() {
                @Override
                public void onClick(UI view, boolean isPressed) {
                    if (isPressed) return;
                    send();
                }
            });
            setPadding(20, 0, 20, 0);
            addView(submit);
        }

        @Override
        protected void updateViewChildren() {
            super.updateViewChildren();
            submit.setWidth(UIFactory.getUIConfig().getButtonWidth());
            submit.setHeight(UIFactory.getUIConfig().getButtonHeight());
        }

        @Override
        protected void updateViewLayout() {
            super.updateViewLayout(); 
            submit.setToCorner(Corner.RC);
        }
        
    }
    
}
