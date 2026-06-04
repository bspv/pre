package com.bazzi.tests.util;

import com.bazzi.core.util.CsvConfig;
import com.bazzi.core.util.CsvUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 {@link CsvUtil} 的读写能力：
 * 1. 读取为数组 / 对象（按列名 {@code @CsvBindByName}、按位置 {@code @CsvBindByPosition} 两种映射）
 * 2. 批量分页读取（数组 / 对象），校验分批回调次数与总量
 * 3. 写入 / 批量写入（数组 / 对象）后回读，做往返一致性校验
 * 4. {@link CsvConfig} 关键配置：是否含表头、分隔符、null 占位符
 * 5. 入参非法时的异常行为（路径为空、类型为空、文件不存在）
 * <p>
 * 测试数据使用源码目录下的 ads.csv（含表头 + 10 行数据，共 10 列）；
 * 所有写入均落在 JUnit 提供的临时目录，避免污染源码树。
 */
class TestCsvUtil {

    /**
     * 测试输入数据所在目录（源码树中与本测试类同级）。
     */
    private static final Path DATA_DIR = Paths.get(
            System.getProperty("user.dir"),
            "src", "test", "java", "com", "bazzi", "tests", "util");

    /**
     * 测试输入文件名（含表头 + 10 行数据，共 10 列）。
     */
    private static final String ADS_CSV = "ads.csv";

    /**
     * ads.csv 的期望数据行数（不含表头）。
     */
    private static final int EXPECTED_ROWS = 10;

    /**
     * ads.csv 的期望列数。
     */
    private static final int EXPECTED_COLUMNS = 10;

    /**
     * 表头，供数组方式写入使用。
     */
    private static final String[] HEADER = {
            "province_id", "province", "city_id", "city", "scene_type",
            "firstscene", "secondscene", "scenename", "sceneid", "gpsrange"};

    @TempDir
    Path tempDir;

    private static String inputPath(String fileName) {
        return DATA_DIR.resolve(fileName).toString();
    }

    private String outputPath(String fileName) {
        return tempDir.resolve(fileName).toString();
    }

    // -------------------- 1. 读取为数组 --------------------

    @Test
    @DisplayName("read：按数组读取应跳过表头并保留全部数据行")
    void testRead() {
        List<String[]> rows = CsvUtil.read(inputPath(ADS_CSV));

        assertEquals(EXPECTED_ROWS, rows.size(), "默认含表头，应返回去表头后的数据行数");
        assertEquals(EXPECTED_COLUMNS, rows.get(0).length, "每行列数应与表头一致");
        assertEquals("121", rows.get(0)[0]);
        assertEquals("浙江", rows.get(0)[1]);
        assertEquals("宁波", rows.get(0)[3]);
    }

    // -------------------- 2. 读取为对象 --------------------

    @Test
    @DisplayName("readAsObject：按列名映射应正确填充字段")
    void testReadAsObjectByName() {
        List<ScenePo> data = CsvUtil.readAsObject(inputPath(ADS_CSV), ScenePo.class);

        assertEquals(EXPECTED_ROWS, data.size());
        ScenePo first = data.get(0);
        assertEquals("121", first.getProvinceId());
        assertEquals("浙江", first.getProvince());
        assertEquals("宁波", first.getCity());
    }

    @Test
    @DisplayName("readAsObject：按位置映射应正确填充字段并完成类型转换")
    void testReadAsObjectByPosition() {
        List<SceneNewPo> data = CsvUtil.readAsObject(inputPath(ADS_CSV), SceneNewPo.class);

        assertEquals(EXPECTED_ROWS, data.size());
        SceneNewPo first = data.get(0);
        assertEquals(121, first.getProvinceId(), "position=0 应转换为 Integer");
        assertEquals("浙江", first.getProvince());
        assertEquals("宁波", first.getCity());
    }

    // -------------------- 3. 批量分页读取 --------------------

    @Test
    @DisplayName("batchRead：按 pageSize 分批回调，合计行数应与全量一致")
    void testBatchRead() {
        CsvConfig config = CsvConfig.builder().pageSize(3).build();
        AtomicInteger batchCount = new AtomicInteger();
        List<String[]> collected = new ArrayList<>();

        CsvUtil.batchRead(inputPath(ADS_CSV), lines -> {
            batchCount.incrementAndGet();
            assertFalse(lines.isEmpty(), "回调批次不应为空");
            collected.addAll(lines);
        }, config);

        assertEquals(EXPECTED_ROWS, collected.size(), "分批读取应覆盖全部数据行");
        // 10 行、每批 3 行 => 向上取整 4 批
        assertEquals(4, batchCount.get(), "10 行按每批 3 行应回调 4 次");
    }

    @Test
    @DisplayName("batchReadAsObject：分批读取对象合计应与全量一致")
    void testBatchReadAsObject() {
        List<SceneNewPo> collected = new ArrayList<>();
        CsvUtil.batchReadAsObject(inputPath(ADS_CSV), SceneNewPo.class, collected::addAll);

        assertEquals(EXPECTED_ROWS, collected.size());
        assertEquals(121, collected.get(0).getProvinceId());
    }

    // -------------------- 4. 写入数组并回读（往返一致） --------------------

    @Test
    @DisplayName("write：写入数组后回读应保持数据一致")
    void testWriteRoundTrip() {
        List<String[]> source = CsvUtil.read(inputPath(ADS_CSV));
        String target = outputPath("ads_write.csv");

        CsvUtil.write(target, source, HEADER);
        List<String[]> reloaded = CsvUtil.read(target);

        assertEquals(source.size(), reloaded.size());
        assertArrayEquals(source.get(0), reloaded.get(0), "首行内容应与写入前一致");
        assertArrayEquals(source.get(source.size() - 1), reloaded.get(reloaded.size() - 1), "末行内容应一致");
    }

    // -------------------- 5. 写入对象并回读（两种映射） --------------------

    @Test
    @DisplayName("writeByObject：按列名映射对象写入后回读应一致")
    void testWriteByObjectName() {
        List<ScenePo> source = CsvUtil.readAsObject(inputPath(ADS_CSV), ScenePo.class);
        String target = outputPath("ads_write_name.csv");

        CsvUtil.writeByObject(target, source);
        List<ScenePo> reloaded = CsvUtil.readAsObject(target, ScenePo.class);

        assertEquals(source.size(), reloaded.size());
        assertEquals(source.get(0).getProvinceId(), reloaded.get(0).getProvinceId());
        assertEquals(source.get(0).getSceneName(), reloaded.get(0).getSceneName());
    }

    @Test
    @DisplayName("writeByObject：按位置映射对象写入后回读应一致")
    void testWriteByObjectPosition() {
        List<SceneNewPo> source = CsvUtil.readAsObject(inputPath(ADS_CSV), SceneNewPo.class);
        String target = outputPath("ads_write_position.csv");

        CsvUtil.writeByObject(target, source);
        List<SceneNewPo> reloaded = CsvUtil.readAsObject(target, SceneNewPo.class);

        assertEquals(source.size(), reloaded.size());
        assertEquals(source.get(0).getProvinceId(), reloaded.get(0).getProvinceId());
        assertEquals(source.get(0).getSceneName(), reloaded.get(0).getSceneName());
    }

    // -------------------- 6. 批量写入对象（数据提供者分页） --------------------

    @Test
    @DisplayName("batchWriteByObject：分页提供数据写入后回读应一致")
    void testBatchWriteByObject() {
        List<SceneNewPo> source = CsvUtil.readAsObject(inputPath(ADS_CSV), SceneNewPo.class);
        String target = outputPath("ads_write_batch.csv");
        CsvConfig config = CsvConfig.builder().pageSize(3).build();

        CsvUtil.batchWriteByObject(target, (page, batchSize) -> {
            int offset = (page - 1) * batchSize;
            if (offset >= source.size()) {
                return Collections.emptyList();
            }
            int end = Math.min(offset + batchSize, source.size());
            return source.subList(offset, end);
        }, SceneNewPo.class, null, config);

        List<SceneNewPo> reloaded = CsvUtil.readAsObject(target, SceneNewPo.class);
        assertEquals(source.size(), reloaded.size());
        assertEquals(source.get(0).getProvinceId(), reloaded.get(0).getProvinceId());
    }

    // -------------------- 7. CsvConfig 关键配置 --------------------

    @Test
    @DisplayName("config：hasHeader=false 时不跳过首行")
    void testReadWithoutHeader() {
        List<String[]> rows = CsvUtil.read(inputPath(ADS_CSV),
                CsvConfig.builder().hasHeader(false).build());

        assertEquals(EXPECTED_ROWS + 1, rows.size(), "不跳过表头时应多出表头一行");
        assertEquals("province_id", rows.get(0)[0], "首行应为表头内容");
    }

    @Test
    @DisplayName("config：nullValue 应把数组中的 null 写成指定占位符")
    void testWriteNullValueReplacement() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"a", null, "c"});
        String target = outputPath("null_value.csv");

        CsvConfig config = CsvConfig.builder().hasHeader(false).nullValue("N/A").build();
        CsvUtil.write(target, data, null, config);

        List<String[]> reloaded = CsvUtil.read(target, CsvConfig.builder().hasHeader(false).build());
        assertEquals(1, reloaded.size());
        assertArrayEquals(new String[]{"a", "N/A", "c"}, reloaded.get(0));
    }

    @Test
    @DisplayName("config：自定义分隔符写入后用同样分隔符应能正确回读")
    void testCustomSeparatorRoundTrip() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"x", "y", "z"});
        String target = outputPath("semicolon.csv");

        CsvConfig config = CsvConfig.builder().hasHeader(false).separator(';').build();
        CsvUtil.write(target, data, null, config);
        List<String[]> reloaded = CsvUtil.read(target, config);

        assertEquals(1, reloaded.size());
        assertArrayEquals(new String[]{"x", "y", "z"}, reloaded.get(0));
    }

    // -------------------- 8. 非法入参的异常行为 --------------------

    @Test
    @DisplayName("非法入参：路径为空应抛出 IllegalArgumentException")
    void testReadBlankPath() {
        assertThrows(IllegalArgumentException.class, () -> CsvUtil.read("  "));
    }

    @Test
    @DisplayName("非法入参：clazz 为 null 应抛出 NullPointerException")
    void testReadAsObjectNullClazz() {
        String path = inputPath(ADS_CSV);
        assertThrows(NullPointerException.class,
                () -> CsvUtil.readAsObject(path, null));
    }

    @Test
    @DisplayName("非法入参：读取不存在的文件应抛出 CsvException")
    void testReadMissingFile() {
        String missing = outputPath("not_exists.csv");
        assertThrows(CsvUtil.CsvException.class, () -> CsvUtil.read(missing));
    }

}
