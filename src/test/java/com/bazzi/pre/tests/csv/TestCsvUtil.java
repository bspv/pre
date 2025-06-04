package com.bazzi.pre.tests.csv;

import com.bazzi.core.util.CsvConfig;
import com.bazzi.core.util.CsvUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestCsvUtil {
    private static final Logger logger = LoggerFactory.getLogger(TestCsvUtil.class);

    @Test
    public void testRead() {
//        List<String[]> csvData = CsvUtil.read("C:\\Users\\hhxd1\\Desktop\\ads_name-1.csv");
        List<String[]> csvData = CsvUtil.read(getFilePath("ads.csv"));
        logger.info("testRead, csvData = {}", csvData.size());
        logger.info("testRead, csvData.get(0).length = {}", Arrays.toString(csvData.get(0)));
    }

    @Test
    public void testReadAsObject() {
        List<ScenePo> csvData = CsvUtil.readAsObject(getFilePath("ads.csv"), ScenePo.class);
        logger.info("testReadAsObjectByName, csvData = {}", csvData.size());
        logger.info("testReadAsObjectByName, csvData.get(0) = {}", csvData.get(0));

        List<SceneNewPo> csvData1 = CsvUtil.readAsObject(getFilePath("ads.csv"), SceneNewPo.class);
        logger.info("testReadAsObjectByPosition, csvData1 = {}", csvData1.size());
        logger.info("testReadAsObjectByPosition, csvData1.get(0) = {}", csvData1.get(0));
    }

    @Test
    public void testBatchRead() {
        CsvUtil.batchRead("D:\\OTT\\信通院OTT\\jilin_cell_data_q4.csv", (lines) -> {
            logger.info("testBatchRead, lines.size:{}", lines.size());
            for (String[] line : lines) {
                logger.info("testBatchRead, line:{}", Arrays.toString(line));
            }
        });
    }

    @Test
    public void testBatchReadAsObject() {
        List<SceneNewPo> csvData = new ArrayList<>();
        CsvUtil.batchReadAsObject(getFilePath("ads.csv"), SceneNewPo.class, csvData::addAll);
        logger.info("testProcessBeanRead, csvData = {}", csvData.size());
        logger.info("testProcessBeanRead, csvData.get(0) = {}", csvData.get(0));

        List<ScenePo> csvData1 = new ArrayList<>();
        CsvUtil.batchReadAsObject(getFilePath("ads.csv"), ScenePo.class, csvData1::addAll);
        logger.info("testProcessBeanRead1, csvData = {}", csvData1.size());
        logger.info("testProcessBeanRead1, csvData.get(0) = {}", csvData1.get(0));
    }

    @Test
    public void testWrite() {
        List<String[]> csvData = CsvUtil.read(getFilePath("ads.csv"));
        CsvUtil.write(getFilePath("ads_write.csv"), csvData, new String[]{"province_id", "province", "city_id", "city", "scene_type", "firstscene", "secondscene", "scenename", "sceneid", "gpsrange"});
    }

    @Test
    public void testWriteByObject() {
        List<ScenePo> csvData = CsvUtil.readAsObject(getFilePath("ads.csv"), ScenePo.class);
        CsvUtil.writeByObject(getFilePath("ads_write_name.csv"), csvData);

        List<SceneNewPo> csvData1 = CsvUtil.readAsObject(getFilePath("ads.csv"), SceneNewPo.class);
        CsvUtil.writeByObject(getFilePath("ads_write_position.csv"), csvData1);
    }

    @Test
    public void testBatchWriteByObject() {
        long start = System.currentTimeMillis();
        List<SceneNewPo> csvData = CsvUtil.readAsObject(getFilePath("ads.csv"), SceneNewPo.class);
        long readEnd = System.currentTimeMillis();
        CsvConfig config = CsvConfig.builder().pageSize(3).build();
        CsvUtil.batchWriteByObject(getFilePath("ads_write_batch.csv"), (page, batchSize) -> {
            int offset = (page - 1) * batchSize;
            int end = Math.min(offset + batchSize, csvData.size());
            logger.info("testBatchWriteByObject, page = {}, batchSize = {}, offset = {}, end = {}", page, batchSize, offset, end);
            return offset > end ? Collections.emptyList() : csvData.subList(offset, end);
        }, SceneNewPo.class, null, config);
        long cur = System.currentTimeMillis();
        logger.info("testBatchWriteByObject, read_cost = {}ms, write_cost = {}ms", readEnd - start, cur - readEnd);
    }

    private static String getFilePath(String fileName) {
        return System.getProperty("user.dir") + File.separatorChar + "\\src\\test\\java\\com\\bazzi\\pre\\tests\\csv" + File.separatorChar + fileName;
    }
}
