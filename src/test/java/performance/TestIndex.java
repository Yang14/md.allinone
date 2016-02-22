package performance;

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

/**
 * Created by Mr-yang on 16-2-22.
 */
public class TestIndex {
    private static Logger logger = LoggerFactory.getLogger("TestIndex");

    private IndexDao indexDao = new RocksdbDaoImpl();
    private CommonModule commonModule = new CommonModuleImpl();


    /**
     * 测试创建100w索引数据需要的时间和空间
     */
    @Test
    public void testCreateOneMillionIndexUsedTimeAndSize() {
        long parentCode = 0L;
        String dirName = "dir";
        long start = System.currentTimeMillis();
        for (long i = 0; i < 1000000; ++i) {
            indexDao.insertMdIndex(buildKey(parentCode, dirName + i), genDirIndex(i, commonModule.genDCode()));
        }
        long end = System.currentTimeMillis();
        logger.info(String.format("time: %sms", (end - start)));
    }

    /**
     * 测试随机读取10w索引的时间
     */
    @Test
    public void testGetIndexRandom() {
        long parentCode = 0L;
        String dirName = "dir";
        Random rand = new Random();
        int dirPrefix;
        long start = System.currentTimeMillis();
        for (long i = 0; i < 100000; ++i) {
            dirPrefix = rand.nextInt(1000000);
            if (i < 3) {
                logger.info(indexDao.findMdIndex(buildKey(parentCode, dirName + dirPrefix)).toString());
            }
            indexDao.findMdIndex(buildKey(parentCode, dirName + dirPrefix));
        }
        long end = System.currentTimeMillis();
        logger.info(String.format("time: %sms", (end - start)));
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
