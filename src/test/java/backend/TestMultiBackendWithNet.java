package backend;

import base.md.MdAttr;
import base.md.MdPos;
import base.rmiapi.backend.BackendOpsService;
import client.service.impl.RmiTool;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Mr-yang on 16-2-22.
 * 使用多线程模拟客户端创建和查询索引
 */
public class TestMultiBackendWithNet {
    private static Logger logger = LoggerFactory.getLogger("TestMultiBackendWithNet");
    private int threadCount = 1;
    private CountDownLatch latch = new CountDownLatch(threadCount);

    private Map<String, Long> createTimeMap = new ConcurrentHashMap<String, Long>();
    private Map<String, Long> findTimeMap = new ConcurrentHashMap<String, Long>();

    private final int createCount = 1000000 / threadCount;

    private Map<Integer, Integer> dCodeMap;

    @Before
    public void setUp() {
        dCodeMap = new HashMap<Integer, Integer>();
        for (int i = 0; i < threadCount; i++) {
            dCodeMap.put(i, 100 + i);
        }
    }

    /**
     * 测试多个线程并发创建100w索引数据需要的时间
     * 通过rmi接口
     */
    @Test
    public void testMultiCreate() throws InterruptedException, RemoteException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; ++i) {
            new Thread(new CreateIndexWithNet(), dCodeMap.get(i) + "").start();
        }
        latch.await();
        long end = System.currentTimeMillis();
        logger.info(String.format("with net: %s thread create %s index, use time: %sms", threadCount, createCount, (end - start)));
        // logger.info("each thread create index time spend is:" + createTimeMap);
    }

    /**
     * 测试多个线程并发创建100w索引数据需要的时间
     * 通过rmi接口
     */
    @Test
    public void testMultiFind() throws InterruptedException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; ++i) {
            new Thread(new FindIndexWithNet(), dCodeMap.get(i) + "").start();
        }
        latch.await();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s thread find %sw index, use time: %sms", threadCount, createCount, (end - start)));
        logger.info("each thread find index time spend is:" + findTimeMap);
    }


    /**
     * 测试创建1w索引数据需要的时间和空间
     */
    @Test
    public void testCreateOneMillionIndexUsedTimeAndSize() throws RemoteException {
        long dCode = Long.parseLong(Thread.currentThread().getName());
        String fileName = "file";
        long start = System.currentTimeMillis();
        for (int i = 0; i < createCount; ++i) {
            final BackendOpsService backendOpsService = RmiTool.getBackendOpsService(new MdPos("localhost", 9999, dCode));
            backendOpsService.insertMd(dCode, fileName + i, getMdAttr(fileName + i, i, false));

        }
        long end = System.currentTimeMillis();
        createTimeMap.put(dCode + "", end - start);
    }

    /**
     * 测试随机读取10w索引的时间
     */
    @Test
    public void testGetIndexRandom() throws RemoteException {
        long dCode = Long.parseLong(Thread.currentThread().getName());
        String fileName = "file";
        long start = System.currentTimeMillis();
        for (long i = 0; i < createCount; ++i) {
            final BackendOpsService backendOpsService = RmiTool.getBackendOpsService(new MdPos("localhost", 9999, dCode));
            backendOpsService.findFileMd(dCode, fileName + i);
        }
        long end = System.currentTimeMillis();
        findTimeMap.put(dCode+"", end - start);
    }

    class CreateIndexWithNet implements Runnable {

        @Override
        public void run() {
            try {
                testCreateOneMillionIndexUsedTimeAndSize();
                latch.countDown();
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

    private MdAttr getMdAttr(String name, int size, boolean isDir) {
        MdAttr mdAttr = new MdAttr();
        mdAttr.setName(name);
        mdAttr.setSize(size);
        mdAttr.setType(isDir);
        mdAttr.setCreateTime(System.currentTimeMillis());
        return mdAttr;
    }

}
