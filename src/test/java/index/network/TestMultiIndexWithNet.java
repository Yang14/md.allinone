package index.network;

import base.rmiapi.index.IndexOpsService;
import client.service.impl.RmiTool;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Mr-yang on 16-2-22.
 * 使用多线程模拟客户端创建和查询索引
 */
public class TestMultiIndexWithNet {
    private static Logger logger = LoggerFactory.getLogger("TestMultiIndexWithNet");
    private int threadCount = 4;
    private CountDownLatch startGate = new CountDownLatch(1);
    private CountDownLatch latch = new CountDownLatch(threadCount);

    private IndexOpsService indexOps = RmiTool.getIndexOpsService();

    private String[] dirArray = new String[]{"/", "/d1", "/d1/d2", "/d1/d2/d3/d4", "/d1/d2/d3", "/d1/d2/d3/d4/d5"};

    private Map<String, Long> createTimeMap = new ConcurrentHashMap<String, Long>();
    private Map<String, Long> findTimeMap = new ConcurrentHashMap<String, Long>();

    class CreateIndexWithNet implements Runnable {

        @Override
        public void run() {
            try {
                startGate.await();
                testCreateOneMillionIndexUsedTimeAndSize();
                latch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    class FindIndexWithNet implements Runnable {

        @Override
        public void run() {
            try {
                testGetIndexRandom();
                latch.countDown();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 测试多个线程并发创建100w索引数据需要的时间
     * 通过rmi接口
     */
    @Test
    public void testMultiCreate() throws InterruptedException, RemoteException {
        buildDirTreeBeforeTest();
        long count = 10000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; ++i) {
            new Thread(new CreateIndexWithNet(), dirArray[i]).start();
        }
        latch.await();
        long end = System.currentTimeMillis();
        logger.info(String.format("with net: %s thread create %sw index, use time: %sms", threadCount, count, (end - start)));
        logger.info("each thread create index time spend is:" + createTimeMap);
    }

    /**
     * 测试创建1w索引数据需要的时间和空间
     */
    @Test
    public void testCreateOneMillionIndexUsedTimeAndSize() throws RemoteException {
        String parentPath = Thread.currentThread().getName();
        String dirName = "dir";
        long count = 10000;
        long start = System.currentTimeMillis();
        for (long i = 0; i < count; ++i) {
            indexOps.createDirIndex(parentPath, dirName + i);
        }
        long end = System.currentTimeMillis();
        createTimeMap.put(parentPath, end - start);
    }

    private void buildDirTreeBeforeTest() throws RemoteException {
        indexOps.createDirIndex("/", "d1");
        indexOps.createDirIndex("/d1", "d2");
        indexOps.createDirIndex("/d1/d2", "d3");
        indexOps.createDirIndex("/d1/d2/d3", "d4");
        indexOps.createDirIndex("/d1/d2/d3/d4", "d5");
        indexOps.createDirIndex("/d1/d2/d3/d4/d5", "d6");
        startGate.countDown();
    }

    /**
     * 测试多个线程并发创建100w索引数据需要的时间
     * 通过rmi接口
     */
    @Test
    public void testMultiFind() throws InterruptedException {
        long count = 10000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; ++i) {
            new Thread(new FindIndexWithNet(), dirArray[i]).start();
        }
        latch.await();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s thread find %sw index, use time: %sms", threadCount, count, (end - start)));
        logger.info("each thread find index time spend is:" + createTimeMap);
    }

    /**
     * 测试随机读取10w索引的时间
     */
    @Test
    public void testGetIndexRandom() throws RemoteException {
        String parentPath = Thread.currentThread().getName();
        Random rand = new Random();
        String dirPath = parentPath + "/dir";
        long start = System.currentTimeMillis();
        for (long i = 0; i < 10; ++i) {
            dirPath += rand.nextInt(10000);
            logger.info(dirPath + " " + indexOps.getMdPosList(dirPath).toString());
        }
        long end = System.currentTimeMillis();
        findTimeMap.put(parentPath, end - start);
    }

}
