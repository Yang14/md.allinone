import junit.framework.TestSuite;
import org.rocksdb.*;
import org.slf4j.LoggerFactory;

/**
 * Created by Mr-yang on 16-2-26.
 */
public class TestRocksCol extends TestSuite {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger("TestRocksCol");

    static {
        RocksDB.loadLibrary();
    }

    public static void main(String[] args) throws RocksDBException {
        String db_path = "/data/backend";
        System.out.println("RocksDBColumnFamilySample");
        RocksDB db = null;
        Options options = null;
        try {
            options = new Options().setCreateIfMissing(true);
            db = RocksDB.open(options, db_path);
            db.put("1:a".getBytes(), "aaa".getBytes());
            db.put("1:b".getBytes(), "aaa".getBytes());
            db.put("2:b".getBytes(), "aaa".getBytes());
            db.put("3:c".getBytes(), "aaa".getBytes());
            long count = 0;
            RocksIterator it = db.newIterator(new ReadOptions());

            for (it.seekToFirst();it.isValid();it.next()){
                logger.info("<" + new String(it.key()) + "," + new String(it.value()) + ">");
            }
            String startStr = "100008:";
            String endStr = "100009:";
            long start = System.currentTimeMillis();
            for (it.seek(startStr.getBytes());
                 it.isValid() && (new String(it.key()).compareTo(endStr) < 0);
                 it.next()) {
                count++;
                   // logger.info("<" + new String(it.key()) + "," + new String(it.value()) + ">");
            }
            long end = System.currentTimeMillis();
            logger.info(String.format("list all key use time: %s, %s count.", (end - start), count));
        } finally {
            if (db != null) {
                db.close();
                db = null;
            }
            if (options != null) {
                options.dispose();
            }
        }
    }

}
