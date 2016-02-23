package index;

import base.md.MdIndex;
import index.common.CommonModule;
import index.common.CommonModuleImpl;
import index.dao.IndexDao;
import index.dao.impl.RocksdbDaoImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Mr-yang on 16-2-22.
 */
public class TestMultiIndex {
    private static Logger logger = LoggerFactory.getLogger("TestIndex");

    private IndexDao indexDao = new RocksdbDaoImpl();
    private CommonModule commonModule = new CommonModuleImpl();

    private int threadCount = 1;
    private CountDownLatch latch = new CountDownLatch(threadCount);


    class CreateIndex implements Runnable {

        @Override
        public void run() {
            testCreateOneMillionIndexUsedTimeAndSize();
            latch.countDown();
        }
    }

    class FindIndex implements Runnable {

        @Override
        public void run() {
            testGetIndexRandom();
            latch.countDown();
        }
    }

    /**
     * 测试多个线程并发创建100w索引数据需要的时间
     */
    @Test
    public void testMultiCreate() throws InterruptedException {
        long count = 1000000;
        long start = System.currentTimeMillis();
        for (long i = 0; i < threadCount; ++i) {
            new Thread(new CreateIndex(), "thread" + i).start();
        }
        latch.await();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s thread create %sw index, use time: %sms", threadCount, count, (end - start)));
    }

    /**
     * 测试创建100w索引数据需要的时间和空间
     */
    @Test
    public void testCreateOneMillionIndexUsedTimeAndSize() {
        long parentCode = 0L;
        String dirName = Thread.currentThread().getName();
        long count = 1000000;
        for (long i = 0; i < count; ++i) {
            indexDao.insertMdIndex(buildKey(parentCode, dirName + i), genDirIndex(i, commonModule.genDCode()));
        }
    }

    /**
     * 测试多个线程并发创建100w索引数据需要的时间
     */
    @Test
    public void testMultiFind() throws InterruptedException {
        long count = 100000;
        long start = System.currentTimeMillis();
        for (long i = 0; i < threadCount; ++i) {
            new Thread(new FindIndex(), "thread" + i).start();
        }
        latch.await();
        long end = System.currentTimeMillis();
        logger.info(String.format("%s thread find %sw index, use time: %sms", threadCount, count, (end - start)));
    }

    /**
     * 测试随机读取10w索引的时间
     */
    @Test
    public void testGetIndexRandom() {
        long parentCode = 0L;
        String dirName = Thread.currentThread().getName();
        Random rand = new Random();
        int dirPrefix;
        for (long i = 0; i < 100000; ++i) {
            dirPrefix = rand.nextInt(1000000);
            indexDao.findMdIndex(buildKey(parentCode, dirName + dirPrefix));
        }
    }

    private MdIndex genDirIndex(long fCode, long dCode) {
        List<Long> dCodes = new ArrayList<Long>();
        dCodes.add(dCode);
        MdIndex mdIndex = new MdIndex();
        mdIndex.setfCode(fCode);
        mdIndex.setdCodeList(dCodes);
        return mdIndex;
    }

    private String buildKey(long dCode, String dirName) {
        return dCode + ":" + dirName;
    }
}
