package backend;

import base.md.MdAttr;
import org.junit.Test;
import org.nutz.ssdb4j.impl.SimpleClient;
import org.nutz.ssdb4j.spi.Response;
import org.nutz.ssdb4j.spi.SSDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by Mr-yang on 16-2-23.
 */
public class TestSSDB {
    private static Logger logger = LoggerFactory.getLogger("TestSSDB");

    private SSDB ssdb = new SimpleClient("localhost", 8888, 10000);
    private int count = 100000;

    @Test
    public void testSet() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            ssdb.set(i, getMdAttr("file" + i, i, false));
        }
        long end = System.currentTimeMillis();
        logger.info(String.format("single thread set %sw mdattr, use time: %sms", count, (end - start)));
    }

    @Test
    public void testGet() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            ssdb.get(i);
        }
        long end = System.currentTimeMillis();
        logger.info(String.format("single thread get %sw mdattr, use time: %sms", count, (end - start)));
    }

    @Test
    public void testHSet(){
        long dCode = 10000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            ssdb.hset(dCode, "file" + i, getMdAttr("file" + i, i, false));
        }
        long end = System.currentTimeMillis();
        logger.info(String.format("single thread get %sw mdattr, use time: %sms", count, (end - start)));
    }
    @Test
    public void testHGet(){
        long dCode = 10000;
        Response response = ssdb.hgetall(dCode);
        Map<String, Object> mdAttrMap = response.map();
        int i=10;
        for (String key : mdAttrMap.keySet()){
            if (i-- == 0) break;
            MdAttr mdAttr = (MdAttr) mdAttrMap.get(key);
            logger.info(mdAttr.toString() + " " + mdAttrMap.size());
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
