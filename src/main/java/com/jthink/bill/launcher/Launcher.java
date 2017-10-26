package com.jthink.bill.launcher;

import com.google.common.collect.Lists;
import com.jthink.bill.constant.Constants;
import com.jthink.bill.dto.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * JThink@JThink
 *
 * @author JThink
 * @version 0.0.1
 * @desc 项目启动器
 * @date 2016-08-24 18:31:48
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.gravity.data.clear.bank.clear"})
@PropertySource("classpath:properties/bill.properties")
public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Launcher.class);
        Set<ApplicationListener<?>> listeners = builder.application().getListeners();
        for (Iterator<ApplicationListener<?>> it = listeners.iterator(); it.hasNext();) {
            ApplicationListener<?> listener = it.next();
            if (listener instanceof LoggingApplicationListener) {
                it.remove();
            }
        }
        builder.application().setListeners(listeners);
        ConfigurableApplicationContext context = builder.run(args);
        LOGGER.info("bank clear start successfully");

        LOGGER.info("开始进行账务计算...");
        if (args.length == 0) {
            LOGGER.info("参数不对");
            LOGGER.info("请输入参数: ");
            LOGGER.info("参数1: 需要进行账务计算的csv文件位置, 每行格式: 总重量,单价区间(如: 100,4-5)");
            LOGGER.info("参数2: 计算结果csv文件位置");
            LOGGER.info("参数3: 总价(最多小数点后一位)");
            return;
        }
        String srcPath = args[0];
        String rsPath = args[1];
        Double total = Double.parseDouble(args[2]);

        List<Interval> intervals = new ArrayList<>();
        List<Double> weights = new ArrayList<>();

        // 对价格区间和重量进行赋值
        File file = new File(srcPath);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            // 一次读入一行，直到读入null为文件结束
            while ((line = br.readLine()) != null) {
                String[] wi = line.split(Constants.COMMA);
                if (wi.length != 2) {
                    LOGGER.info("{} 这行格式错误", line);
                    return;
                }
                weights.add(Double.parseDouble(wi[0]));
                String[] interval = wi[1].split(Constants.MIDDLE_LINE);
                if (interval.length != 2) {
                    LOGGER.info("{} 这行格式错误", line);
                    return;
                }
                intervals.add(new Interval(Integer.parseInt(interval[0]) * 10, Integer.parseInt(interval[1]) * 10));
            }
        } catch (IOException e) {
            LOGGER.error("读文件报错, ", e);
        } catch (Exception e) {
            LOGGER.error("解密出错, ", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOGGER.error("读文件报错, ", e);
                }
            }
        }

        Double[] price = new Double[weights.size()];

        List<Double> rs = new ArrayList<>();

        calculate(0, weights, intervals, price, rs, total);

        br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            int cnt = 0;
            // 一次读入一行，直到读入null为文件结束
            while ((line = br.readLine()) != null) {
                StringBuilder sb = new StringBuilder();
                String[] wi = line.split(Constants.COMMA);
                Double w = Double.parseDouble(wi[0]);
                sb.append(w).append(Constants.COMMA);
                Double p = rs.get(cnt++);
                sb.append(p / w).append(Constants.COMMA).append(p);
                writeTofile(sb.toString(), rsPath);
            }
        } catch (IOException e) {
            LOGGER.error("读文件报错, ", e);
        } catch (Exception e) {
            LOGGER.error("解密出错, ", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOGGER.error("读文件报错, ", e);
                }
            }
        }

        LOGGER.info("进行账务计算结束...");
    }

    /**
     * 具体计算
     * @param index
     * @param weights
     * @param intervals
     * @param price
     * @param total
     */
    private static void calculate(int index, List<Double> weights, List<Interval> intervals, Double[] price, List<Double> rs, Double total) {
        int size = weights.size();
        if (size == index) {
            Double count = 0.0;
            for (Double item : price) {
                count += item;
            }
            if (count.equals(total)) {
                rs.addAll(Lists.newArrayList(price));
            }
        } else {
            Double weight = weights.get(index);
            Interval interval = intervals.get(index);

            for (int j = interval.getMin(); j <= interval.getMax(); ++j) {
                Double item = weight * j / 10.0;
                price[index] = item;
                calculate(index + 1, weights, intervals, price, rs, total);
            }
        }
    }

    /**
     * 写文件
     * @param line
     * @param destPath
     */
    private static void writeTofile(String line, String destPath) {
        BufferedWriter bw = null;
        try {
            File file = new File(destPath);
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    LOGGER.info("创建父文件夹失败");
                }
            }
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(line);
            bw.newLine();
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
