package index.dao.impl;

import base.md.MdIndex;
import com.alibaba.fastjson.JSON;
import index.dao.IndexDao;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Created by yang on 16-2-21.
 */
public class RedisDaoImpl implements IndexDao {

    private static Jedis jedis = new Jedis("192.168.0.13",6379);

    @Override
    public boolean insertMdIndex(String key, MdIndex mdIndex) {
        jedis.set(key, JSON.toJSONString(mdIndex));
        return true;
    }

    @Override
    public MdIndex findMdIndex(String key) {
        return JSON.parseObject(jedis.get(key),MdIndex.class);
    }

    @Override
    public boolean removeMdIndex(String key) {
        jedis.del(key);
        return true;
    }

    @Override
    public Map<String, MdIndex> getIndexMap() {
        return null;
    }
}
