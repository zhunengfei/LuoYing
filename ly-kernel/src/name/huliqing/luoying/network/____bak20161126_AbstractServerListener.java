///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package name.huliqing.luoying.network;
//
//import name.huliqing.luoying.mess.network.MessGameData;
//import name.huliqing.luoying.mess.network.MessClients;
//import name.huliqing.luoying.mess.network.MessClient;
//import name.huliqing.luoying.mess.network.MessGetServerState;
//import name.huliqing.luoying.mess.network.MessServerState;
//import name.huliqing.luoying.data.ConnData;
//import name.huliqing.luoying.network.GameServer.ServerListener;
//import name.huliqing.luoying.mess.network.MessGetClients;
//import name.huliqing.luoying.mess.network.MessGetGameData;
//import com.jme3.app.Application;
//import com.jme3.network.HostedConnection;
//import com.jme3.network.Message;
//import java.util.concurrent.Callable;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import name.huliqing.luoying.data.GameData;
//import name.huliqing.luoying.mess.network.MessRequestGameInit;
//
///**
// * 服务端监听器,用于监听来自客户端连接的消息。
// * @author huliqing
// * @param <T>
// */
//public abstract class AbstractServerListener<T> implements ServerListener<T> {
//
//    private static final Logger LOG = Logger.getLogger(AbstractServerListener.class.getName());
//
//    private final Application app;
//    
//    public AbstractServerListener(Application app) {
//        this.app = app;
//    }
//    
//    @Override
//    public final void clientAdded(final GameServer gameServer, final HostedConnection conn) {
//        app.enqueue(new Callable() {
//            @Override
//            public Object call() throws Exception {
//                onClientAdded(gameServer, conn);
//                return null;
//            }
//        });
//    }
//
//    @Override
//    public final void clientRemoved(final GameServer gameServer, final HostedConnection conn) {
//        app.enqueue(new Callable() {
//            @Override
//            public Object call() throws Exception {
//                onClientRemoved(gameServer, conn);
//                return null;
//            }
//        });
//    }
//
//    @Override
//    public final void serverMessage(final GameServer gameServer, final HostedConnection source, final Message m) {
//        app.enqueue(new Callable() {
//            @Override
//            public Object call() throws Exception {
//                clientMessage(gameServer, source, m);
//                return null;
//            }
//        });
//    }
//    
//    private void clientMessage(GameServer gameServer, HostedConnection source, final Message m) {
//        LOG.log(Level.INFO, "Receive from client, class={0}, message={1}", new Object[] {m.getClass().getName(), m.toString()});
//
//        if (m instanceof MessClient) {
//            onReceiveMessClient(gameServer, source, (MessClient) m);
//
//        } else if (m instanceof MessGetServerState) {
//            onReceiveMessGetServerState(gameServer, source, (MessGetServerState) m);
//
//        } else if (m instanceof MessGetGameData) {
//            onReceiveMessGetGameData(gameServer, source, (MessGetGameData) m);
//
//        } else if (m instanceof MessGetClients) {
//            onReceiveMessGetClients(gameServer, source, (MessGetClients) m);
//
//        } else if (m instanceof MessRequestGameInit) {
//            onReceiveMessRequestGameInit(gameServer, source, (MessRequestGameInit) m);
//
//        } else {
//            // other message
//            onReceiveMessage(gameServer, source, m);
//        }
//    }
//    
//    /**
//     * 当前客户端获得标识时,这一步发生在 {@link #onClientAdd }之后, 主要更
//     * 新客户端的标识，并刷新客户端列表。
//     * @param gameServer
//     * @param conn
//     * @param m 
//     */
//    protected void onReceiveMessClient(GameServer gameServer, HostedConnection conn, MessClient m) {
//        // 登记、更新客户端资料
//        ConnData cd = conn.getAttribute(ConnData.CONN_ATTRIBUTE_KEY);
//        cd.setClientId(m.getClientId());
//        cd.setClientName(m.getClientName());
//        // 更新客户端列表
//        onClientsUpdated(gameServer);
//    }
//    
//    /**
//     * 当接收到客户端发来的询问服务端的当前状态时。该方法主要向请求的客户端
//     * 返回当前服务端的状态。
//     * @param gameServer
//     * @param conn
//     * @param m 
//     */
//    protected void onReceiveMessGetServerState(GameServer gameServer, HostedConnection conn, MessGetServerState m) {
//        gameServer.send(conn, new MessServerState(gameServer.getServerState()));
//    }
//    
//    /**
//     * 当接收到客户端发送的请求游戏数据的消息时该方法被自动调用，这个方法主要实现向客户端返回游戏数据的消息：
//     * {@link  GameData}, 默认情况下服务端仅向客户端发送游戏的基本数据。
//     * 详情参考{@link #onSendGameData(GameServer, HostedConnection) }
//     * @param gameServer
//     * @param conn
//     * @param m 
//     * @see #onSendGameData(GameServer, HostedConnection) 
//     */
//    protected void onReceiveMessGetGameData(GameServer gameServer, HostedConnection conn, MessGetGameData m) {
//        onSendGameData(gameServer, conn);
//    }
//    
//    /**
//     * 当接收到客户端发送的请求所有的客户端列表时该方法被自动调用，这个方法主要实现向客户端返回当前连接的所有客户端
//     * 列表的基本信息. {@link 
//     * 信息。
//     * @param gameServer
//     * @param conn
//     * @param mess 
//     */
//    protected void onReceiveMessGetClients(GameServer gameServer, HostedConnection conn, MessGetClients mess) {
//        gameServer.broadcast(new MessClients(gameServer.getClients()));
//    }
//    
//    /**
//     * 服务端向客户端发送游戏数据，当服务端接收到客户端连接或者客户端向服务端请求游戏数据时，
//     * 该方法会被自动调用，主要实现向客户端发送当前的游戏数据GameData。<br>
//     * 注：默认情况下，服务端只向客户端发送游戏基本数据，不发送场景实体数据，也不发送游戏逻辑。
//     * 因为客户端不应该执行来自服务端的游戏逻辑，另一个，游戏场景实体数据的不确定性，当场景实体非常多时，
//     * 如果一次性发送可能导致问题，因此这些数据应该在客户端和服务端连接和初始化完成后再从服务端获取。
//     * @param gameServer
//     * @param coon 
//     */
//    protected void onSendGameData(GameServer gameServer, HostedConnection coon) {
//        // 注1：这里向客户端发送的并不包含游戏逻辑数据及场景实体数据，
//        // 这些数据是在客户端状态初始化后再从服务端获取并载入,当服务端和客户端状态就绪之后，服务端可以依次有序的向
//        // 客户端一个一个发送所有实体数据。
//        
//        // 注2：gameData必须克隆后，再清除逻辑和场景实体，否则会影响服务端的游戏数据。
//        GameData gameData = (GameData) gameServer.getGameData().clone(); 
//        gameData.getGameLogicDatas().clear();
//        gameData.getSceneData().setEntityDatas(null);
//        gameData.getGuiSceneData().setEntityDatas(null);
//        gameServer.send(coon, new MessGameData(gameData));
//    }
//    
//    /**
//     * 当一个新的客户端连接到服务端时该方法被自动调用，默认情况下，
//     * 该方法将立即向客户端发送游戏数据{@link GameData}作为响应.
//     * 注：默认情况下，服务端只向客户端发送游戏的基本数据,不包含场景数据及游戏逻辑。
//     * @param gameServer
//     * @param conn 
//     * @see #onSendGameData(GameServer, HostedConnection) 
//     */
//    protected void onClientAdded(GameServer gameServer, HostedConnection conn) {
//        // 初始化一个用于存放数据的容器,选择在这里初始化以便后续使用的时候不再需要判断null
//        ConnData cd = conn.getAttribute(ConnData.CONN_ATTRIBUTE_KEY);
//        if (cd == null) {
//            cd = new ConnData();
//            cd.setConnId(conn.getId());
//            cd.setAddress(conn.getAddress());
//            conn.setAttribute(ConnData.CONN_ATTRIBUTE_KEY, cd);
//        }
//        // 当客户端连接时向客户端发送游戏数据
//        onSendGameData(gameServer, conn);
//        // 更新客户端列表
//        onClientsUpdated(gameServer);
//    }
//    
//    /**
//     * 当一个客户端断开与服务端的连接时该方法被自动调用，默认情况下，该方法会向所有客户端广播更新客户端列表的信息。
//     * 子类可以覆盖该方法来作进一步处理该事件，例如：将客户端控制的游戏角色移除场景，向客户端发送广播通知消息。
//     * @param gameServer
//     * @param conn 
//     */
//    protected void onClientRemoved(GameServer gameServer, HostedConnection conn) {
//        // 更新客户端列表
//        onClientsUpdated(gameServer);
//    }
//    
//    /**
//     * 当客户端列表发生变化时该方法被自动调用,其中包含如：新客户端添加，断开，客户端信息发生变化时该方法都会被调用。
//     * 默认情况下，该方法会向所有客户端广播更新客户端列表的信息。
//     * @param gameServer
//     */
//    protected void onClientsUpdated(GameServer gameServer) {
//        gameServer.broadcast(new MessClients(gameServer.getClients()));
//    }
//    
//    protected abstract void onReceiveMessRequestGameInit(GameServer gameServer, HostedConnection conn, MessRequestGameInit mess);
//    
//    /**
//     * 在服务端处理接收到的来自客户端的消息
//     * @param gameServer
//     * @param source
//     * @param m 
//     */
//    protected abstract void onReceiveMessage(GameServer gameServer, HostedConnection source, Message m);
//}
