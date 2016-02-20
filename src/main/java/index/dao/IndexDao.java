package index.dao;

import base.md.MdIndex;

import java.util.Map;

/**
 * Created by Mr-yang on 16-2-18.
 */
public interface IndexDao {

    public boolean insertMdIndex(String key, MdIndex mdIndex);

    public MdIndex findMdIndex(String key);

    public boolean removeMdIndex(String key);

    public Map<String, MdIndex> getIndexMap();
}
