package com.dxxt.im.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class Zookeeper {

    private CuratorFramework zkClient;

    //线程安全的计数器
    private final AtomicInteger nextServerCyclicCounter = new AtomicInteger(0);

    //key="/dxxt/imserver/im0000000001"  value="192.168.1.23:8080"
    //key="/dxxt/imserver/im0000000002"  value="192.168.1.24:8080"
    private final ConcurrentMap<String, String> imServerNode = new ConcurrentHashMap<>();

    //zk路径监听器-当path下面子节点发生改变后会回调次监听器
    private PathChildrenCacheListener listener = (client, event) -> {
        try {
            if (event.getType() == event.getType().CONNECTION_RECONNECTED) {
                return;
            }
            log.debug("zookeeper event for basePath=[{}] event=[{}]", AppConfig.zookeeperPath, event);

            //节点上线事件通知
            if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                String fullPath = event.getData().getPath();
                String data = new String(event.getData().getData(), "utf-8");
                //key="/dxxt/imserver/im0000000001"  value="192.168.1.23:8080"
                imServerNode.put(fullPath, data);
            }

            //节点下线事件通知
            if (event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                String fullPath = event.getData().getPath();
                //key="/dxxt/imserver/im0000000001"
                imServerNode.remove(fullPath);
            }

        } catch (UnsupportedEncodingException e) {
            log.error("error", e);
        }
    };

    /**
     * 轮询返回可用Server节点
     * 此方法线程安全
     */
    public Object chooseImServerNode() {

        Object node = null;
        int count = 0;

        //循环10次
        while (node == null && count++ < 10) {

            Object[] keys = imServerNode.keySet().toArray();
            int nodeCount = keys.length;

            if ((nodeCount == 0)) {
                log.warn("No up imServerNode available");
                return null;
            }

            //轮询获取数组下标,此方法线程安全
            int nextNodeIndex = incrementAndGetModulo(nodeCount);

            //这里有可能返回空(因为在此刻有可能zk当中有事件发生，导致imServerNode删除了某个节点)
            node = imServerNode.get(keys[nextNodeIndex]);

            //如果node为空,说明刚好有节点掉线，我们收到事件删除了imServerNode当中的某个节点
            if (node == null) {
                //让出一会cpu
                Thread.yield();
                //继续循环，循环10次，不可能每次都赶上下线
                continue;
            }

            log.debug("Choose ImServerNode is [{}]", node);

            //返回可用节点
            return node;
        }

        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from zookeeper");
        }

        return null;
    }

    /**
     * 根据一个模数轮训下标值
     *
     * @param modulo 模数(是数组的长度)
     * @return 假如模数是5，那么返回值为 0,1,2,3,4,0,1,2,3,4 ...以此类推
     */
    private int incrementAndGetModulo(int modulo) {
        for (; ;) {
            int current = nextServerCyclicCounter.get();
            //拿出当前nextServerCyclicCounter的值+1 对 modulo 取模
            //计算出的值next肯定小于modulo
            int next = (current + 1) % modulo;
            //乐观锁设值 如果nextServerCyclicCounter当前值还等于current,则设值成功,否则死循环解决并发问
            if (nextServerCyclicCounter.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    @PostConstruct
    public void started() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, Integer.MAX_VALUE);
        zkClient = CuratorFrameworkFactory.newClient(AppConfig.zookeeperUrl, retryPolicy);
        zkClient.start();

        //对zookeeper当中的某个path进行监听
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, AppConfig.zookeeperPath, true);
        pathChildrenCache.getListenable().addListener(listener);
        pathChildrenCache.start();

        log.info("zookeeper client listen on path: {}", AppConfig.zookeeperPath);
    }

    @PreDestroy
    public void stop() {
        zkClient.close();
        log.info("zookeeper client stopped");
    }

}
