package index.common;

import base.md.MdPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Mr-yang on 16-1-11.
 */
public class CommonModuleImpl implements CommonModule {

    private static long fileCode = 100000;

    @Override
    public long genFCode() {
        return fileCode++;
    }

    @Override
    public long genDCode() {
        return Long.valueOf(new Random().nextInt() & 0x0FFFFFFFF);
    }

    @Override
    public boolean isDCodeFit(long dCode) {
        return true;
    }

    @Override
    public MdPos buildMdPos(long dCode) {
        MdPos md = new MdPos();
        if (dCode % 3 == 0) {
            md.setIp("node-03");
        } else if (dCode % 2 == 0) {
            md.setIp("node-02");
        } else {
            md.setIp("node-01");
        }
        md.setIp("127.0.0.1");
        md.setdCode(dCode);
        md.setPort(9999);
        return md;
    }

    @Override
    public MdPos createMdPos() {
        return buildMdPos(genDCode());
    }

    @Override
    public List<MdPos> buildMdPosList(List<Long> dCodeList) {
        if (dCodeList == null) {
            return null;
        }
        List<MdPos> mdPoses = new ArrayList<MdPos>();
        for (long code : dCodeList) {
            mdPoses.add(buildMdPos(code));
        }
        return mdPoses;
    }
}
