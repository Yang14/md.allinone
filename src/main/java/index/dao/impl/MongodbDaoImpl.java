package index.dao.impl;

import base.md.MdIndex;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import index.dao.IndexDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by Mr-yang on 16-2-24.
 */
public class MongodbDaoImpl implements IndexDao {
    private static Logger logger = LoggerFactory.getLogger("MongodbDaoImpl");

    private static MongoDatabase mongoDb= new MongoClient().getDatabase("indexDb");

    @Override
    public boolean insertMdIndex(String key, MdIndex mdIndex) {
        MongoCollection coll = mongoDb.getCollection("mdIndex");
        System.out.println("Collection mycol selected successfully");
        BasicDBObject doc = new BasicDBObject("key", "MongoDB").
                append("description", "database").
                append("likes", 100).
                append("url", "http://www.w3cschool.cc/mongodb/").
                append("by", "w3cschool.cc");
        coll.insertOne(doc);
        return true;
    }

    @Override
    public MdIndex findMdIndex(String key) {
        return null;
    }

    @Override
    public boolean removeMdIndex(String key) {
        return false;
    }

    @Override
    public Map<String, MdIndex> getIndexMap() {
        return null;
    }
}
