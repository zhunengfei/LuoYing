/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.game.state;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import java.util.List;
import name.huliqing.fighter.Common;
import name.huliqing.fighter.Factory;
import name.huliqing.fighter.data.GameData;
import name.huliqing.fighter.game.service.ActorService;
import name.huliqing.fighter.game.service.PlayService;
import name.huliqing.fighter.game.state.lan.Network;
import name.huliqing.fighter.game.view.ActorSelectView;
import name.huliqing.fighter.game.view.ClientsWin;
import name.huliqing.fighter.object.anim.Anim;
import name.huliqing.fighter.object.anim.Listener;
import name.huliqing.fighter.object.anim.ScaleAnim;
import name.huliqing.fighter.object.env.CameraChaseEnv;
import name.huliqing.fighter.object.game.Game;
import name.huliqing.fighter.object.game.Game.GameListener;
import name.huliqing.fighter.object.scene.SceneUtils;
import name.huliqing.fighter.ui.Icon;
import name.huliqing.fighter.ui.UI;
import name.huliqing.fighter.ui.state.UIState;

/**
 * 联网游戏的基类
 * @author huliqing
 */
public abstract class NetworkPlayState extends PlayState implements LanGame {
    private final PlayService playService = Factory.get(PlayService.class);
    private final ActorService actorService = Factory.get(ActorService.class);
    protected final Network network = Network.getInstance();

    // 客户端列表界面
    private ClientsWin clientsWin;
    private ScaleAnim clientsWinAnim;
    private Icon lanBtn;
    
    protected ActorSelectView actorPanel;
    
    public NetworkPlayState(Application app, GameData gameData) {
        super(app, gameData);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app); 
        // 初始化Network
        network.initialize(app);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        network.update(tpf);
    }
    
    @Override
    public void cleanup() {
        network.cleanup();
        super.cleanup(); 
    }

    @Override
    public void changeGameState(GameState newGameState) {
        super.changeGameState(newGameState);
        gameState.getGame().addListener(new GameListener() {
            @Override
            public void onGameStarted(Game game) {
                createLanUI();
            }
        });
    }

     /**
     * 显示角色选择面板
     * @param selectableActors 
     */
    public final void showSelectPanel(List<String> selectableActors) {
        if (actorPanel == null) {
            actorPanel = new ActorSelectView(Common.getSettings().getWidth(), Common.getSettings().getHeight(), app.getGuiNode());
            actorPanel.setSelectedListener(new ActorSelectView.SelectedListener() {
                @Override
                public void onSelected(String actorId, String actorName) {
                    actorPanel.removeFromParent();
                    actorPanel.getActorView().removeFromParent();
 
                    onSelectPlayer(actorId, actorName);
                }
            });
        }
        actorPanel.setModels(selectableActors);
        
        UIState.getInstance().addUI(actorPanel);
        gameState.addObject(actorPanel.getActorView(), false);
        
        // remove20160710
//        if (chaseCamera == null) {
//            chaseCamera = SceneTools.createChaseCam(app.getCamera(), app.getInputManager());
//            chaseCamera.setDefaultDistance(5f);
//        }
//        actorPanel.getActorView().addControl(chaseCamera);
        
        CameraChaseEnv cce = SceneUtils.findEnv(gameState.getGame().getScene(), CameraChaseEnv.class);
        if (cce != null) {
            cce.setChase(actorPanel.getActorView());
        }
    }
    
    /**
     * 当玩家在本地选择了一个角色进行游戏后进。
     * @param actorId
     * @param actorName
     */
    protected abstract void onSelectPlayer(String actorId, String actorName);
    
    @Override
    public boolean isServer() {
        return network.isServer();
    }
    
    /**
     * 刷新客户端界面列表
     */
    @Override
    public void onClientListUpdated() {
        if (clientsWin.isVisible()) {
            clientsWin.setClients(getClients());
        }
    }

    private void createLanUI() {
         // ---- 联网状态按钮,用于打开局域网客户端列表面板
        float fullWidth = Common.getSettings().getWidth();
        float fullHeight = Common.getSettings().getHeight();
        clientsWin = new ClientsWin(this, fullWidth * 0.75f, fullHeight * 0.8f);
        clientsWin.setToCorner(UI.Corner.CC);
        clientsWin.setCloseable(true);
        clientsWin.setDragEnabled(true);
        clientsWinAnim = new ScaleAnim();
        clientsWinAnim.setStartScale(0.5f);
        clientsWinAnim.setEndScale(1f);
        clientsWinAnim.setRestore(true);
        clientsWinAnim.setUseTime(0.4f);
        clientsWinAnim.setLocalScaleOffset(new Vector3f(clientsWin.getWidth() * 0.5f
                    , clientsWin.getHeight() * 0.5f
                    , 0));
        clientsWinAnim.setTarget(clientsWin);
        clientsWinAnim.addListener(new Listener() {
            @Override
            public void onDone(Anim anim) {
                playService.removeAnimation(anim);
            }
        });
                
        lanBtn = new Icon("Interface/icon/link.png");
        lanBtn.setUseAlpha(true);
        lanBtn.addClickListener(new name.huliqing.fighter.ui.UI.Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) return;
                displayLanPanel();
            }
        });
        // 把按钮添加到工具栏
        gameState.getMenuTool().addMenu(lanBtn, 0);
    }
    
    private void displayLanPanel() {
        if (clientsWin.getParent() == null) {
            addObject(clientsWin, true);
            clientsWin.setClients(getClients());
            playService.addAnimation(clientsWinAnim);
        } else {
            removeObject(clientsWin);
        }
    } 
    
}