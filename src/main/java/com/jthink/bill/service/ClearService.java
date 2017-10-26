package com.jthink.bill.service;

import com.jthink.bill.constant.Constants;
import com.jthink.bill.dto.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * JThink@JThink
 *
 * @author JThink
 * @version 0.0.1
 * @desc 具体清洗业务
 * @date 2016-08-24 18:31:48
 */
@Service
public class ClearService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClearService.class);

    /**
     * 写文件
     * @param transList
     * @param destPath
     */
    private void writeTofile(List<Interval> transList, String destPath) {
        BufferedWriter bw = null;
        try {
            File file = new File(destPath);
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    LOGGER.info("创建父文件夹失败");
                }
            }
            bw = new BufferedWriter(new FileWriter(file, true));
            StringBuilder sb = new StringBuilder();
            for (Interval trans : transList) {
                sb.append(trans.toString()).append(Constants.LINE);
            }
            bw.write(sb.toString());
        } catch (IOException e) {
            LOGGER.error("写文件报错, ", e);
        } finally {
            if (bw != null) {
                try {
                    bw.flush();
                    bw.close();
                } catch (IOException e) {
                    LOGGER.error("写文件报错, ", e);
                }
            }
        }
    }

}
