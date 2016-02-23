package backend;

import base.PortEnum;
import base.md.MdAttr;
import com.alibaba.fastjson.JSON;
import org.junit.Before;
import org.junit.Test;
import org.nutz.ssdb4j.SSDBs;
import org.nutz.ssdb4j.spi.Response;
import org.nutz.ssdb4j.spi.SSDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Mr-yang on 16-2-23.
 */
public class TestMultiSSDB {
    private static Logger logger = LoggerFactory.getLogger("TestMultiSSDB");

    private int threadCount = 8;
    private CountDownLatch latch = new CountDownLatch(threadCount);

    private Map<String, Long> createTimeMap = new ConcurrentHashMap<String, Long>();
    private Map<String, Long> findTimeMap = new ConcurrentHashMap<String, Long>();

    private final int createCount = 100000 / threadCount;

    private Map<Integer, Integer> dCodeMap;
    private static SSDB ssdb = SSDBs.pool("127.0.0.1", PortEnum.SSDB_PORT, 10000, null);

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
        logger.info(String.format("with net: %s thread create %s mdAttr, use time: %sms", threadCount, createCount, (end - start)));
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
        logger.info(String.format("%s thread find %s mdAttr, use time: %sms", threadCount, createCount, (end - start)));
        logger.info("each thread find index time spend is:" + findTimeMap);
    }

    @Test
    public void testHSet(){
        long dCode = Long.parseLong(Thread.currentThread().getName());
        long start = System.currentTimeMillis();
        for (int i = 0; i < createCount; i++) {
            ssdb.hset(dCode, "file" + i, JSON.toJSONString(getMdAttr("file" + i, i, false)));
        }
        long end = System.currentTimeMillis();
        logger.info(String.format("single thread get %sw mdattr, use time: %sms", createCount, (end - start)));
    }
    @Test
    public void testHGet(){
        long dCode = Long.parseLong(Thread.currentThread().getName());
        Response response = ssdb.hgetall(dCode);
        Map<String, String> mdAttrMap = response.mapString();
        int i=10;
        for (String key : mdAttrMap.keySet()){
            if (i-- == 0) break;
            logger.info(JSON.parseObject(mdAttrMap.get(key),MdAttr.class).toString());
        }
    }
    class CreateIndexWithNet implements Runnable {

        @Override
        public void run() {
                testHSet();;
                latch.countDown();
        }
    }

    class FindIndexWithNet implements Runnable {

        @Override
        public void run() {
                testHGet();
                latch.countDown();
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
