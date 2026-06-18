package com.bazzi.core.util;

import com.opencsv.*;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * CSV 工具类。
 * <p>
 * 线程安全说明：本类所有方法均为无状态静态方法，可被多线程并发调用；
 * 但传入的 {@link InputStream}/{@link OutputStream}/{@link CsvConfig} 实例不应在多线程间共享。
 */
@Slf4j
public final class CsvUtil {
    private static final int MIN_CHAR_BUFFER_SIZE = 8 * 1024;

    private static final String MSG_FILE_PATH_REQUIRED = "文件路径不能为空";
    private static final String MSG_CONF_REQUIRED = "CSV 配置不能为空";
    private static final String MSG_CLAZZ_REQUIRED = "数据类型不能为空";
    private static final String MSG_CONSUMER_REQUIRED = "数据消费者不能为空";
    private static final String MSG_DATA_SUPPLIER_REQUIRED = "数据提供者不能为空";

    private CsvUtil() {
        throw new UnsupportedOperationException("不能创建CsvUtil对象");
    }

    /**
     * 采用默认配置，把csv文件读取成数组
     */
    public static List<String[]> read(String filePath) {
        return read(filePath, CsvConfig.builder().build());
    }

    /**
     * 把csv文件读取成数组
     */
    public static List<String[]> read(String filePath, CsvConfig conf) {
        return baseRead(() -> buildBufferReader(filePath, conf), conf);
    }

    /**
     * 采用默认配置，从文件流中读取成数组
     */
    public static List<String[]> read(InputStream input) {
        return read(input, CsvConfig.builder().build());
    }

    /**
     * 从文件流中读取成数组
     */
    public static List<String[]> read(InputStream input, CsvConfig conf) {
        return baseRead(() -> buildBufferReader(input, conf), conf);
    }

    private static List<String[]> baseRead(BufferReaderSupplier supplier, CsvConfig conf) {
        try (CSVReader csvReader = buildCsvReader(supplier.get(), conf)) {
            return csvReader.readAll();
        } catch (Exception e) {
            throw wrapException(e, "读取CSV失败");
        }
    }

    /**
     * 采用默认配置，把csv文件读取成对象
     */
    public static <T> List<T> readAsObject(String filePath, Class<T> clazz) {
        return readAsObject(filePath, clazz, CsvConfig.builder().build());
    }

    /**
     * 把csv文件读取成对象
     */
    public static <T> List<T> readAsObject(String filePath, Class<T> clazz, CsvConfig conf) {
        Objects.requireNonNull(clazz, MSG_CLAZZ_REQUIRED);
        return baseReadAsObject(() -> buildBufferReader(filePath, conf), clazz, conf);
    }

    /**
     * 采用默认配置，从文件流中读取成对象
     */
    public static <T> List<T> readAsObject(InputStream input, Class<T> clazz) {
        return readAsObject(input, clazz, CsvConfig.builder().build());
    }

    /**
     * 从文件流中读取成对象
     */
    public static <T> List<T> readAsObject(InputStream input, Class<T> clazz, CsvConfig conf) {
        Objects.requireNonNull(clazz, MSG_CLAZZ_REQUIRED);
        return baseReadAsObject(() -> buildBufferReader(input, conf), clazz, conf);
    }

    private static <T> List<T> baseReadAsObject(BufferReaderSupplier supplier, Class<T> clazz, CsvConfig conf) {
        try (BufferedReader bufferedReader = supplier.get()) {
            CsvToBean<T> csvToBean = buildCsvToBean(bufferedReader, clazz, conf);
            List<T> list = csvToBean.parse();
            logSkippedRows(conf, csvToBean);
            return list;
        } catch (Exception e) {
            throw wrapException(e, "读取CSV为对象失败");
        }
    }

    /**
     * 采用默认配置，从csv文件中批量消费读取的数组
     */
    public static void batchRead(String filePath, Consumer<List<String[]>> consumer) {
        batchRead(filePath, consumer, CsvConfig.builder().build());
    }

    /**
     * 从csv文件中批量消费读取的数组
     */
    public static void batchRead(String filePath, Consumer<List<String[]>> consumer, CsvConfig conf) {
        Objects.requireNonNull(consumer, MSG_CONSUMER_REQUIRED);
        baseBatchRead(() -> buildBufferReader(filePath, conf), consumer, conf);
    }

    /**
     * 采用默认配置，从文件流中批量消费读取的数组
     */
    public static void batchRead(InputStream input, Consumer<List<String[]>> consumer) {
        batchRead(input, consumer, CsvConfig.builder().build());
    }

    /**
     * 从文件流中批量消费读取的数组
     */
    public static void batchRead(InputStream input, Consumer<List<String[]>> consumer, CsvConfig conf) {
        Objects.requireNonNull(consumer, MSG_CONSUMER_REQUIRED);
        baseBatchRead(() -> buildBufferReader(input, conf), consumer, conf);
    }

    private static void baseBatchRead(BufferReaderSupplier supplier, Consumer<List<String[]>> consumer, CsvConfig conf) {
        try (CSVReader csvReader = buildCsvReader(supplier.get(), conf)) {
            List<String[]> batchList = new ArrayList<>(conf.getPageSize());
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                batchList.add(nextLine);
                if (batchList.size() >= conf.getPageSize()) {
                    consumer.accept(batchList);
                    batchList = new ArrayList<>(conf.getPageSize());
                }
            }
            if (!batchList.isEmpty()) {
                consumer.accept(batchList);
            }
        } catch (Exception e) {
            throw wrapException(e, "批量读取CSV失败");
        }
    }

    /**
     * 采用默认配置，从csv文件中批量消费读取的对象
     */
    public static <T> void batchReadAsObject(String filePath, Class<T> clazz, Consumer<List<T>> consumer) {
        batchReadAsObject(filePath, clazz, consumer, CsvConfig.builder().build());
    }

    /**
     * 从csv文件中批量消费读取的对象
     */
    public static <T> void batchReadAsObject(String filePath, Class<T> clazz, Consumer<List<T>> consumer, CsvConfig conf) {
        Objects.requireNonNull(clazz, MSG_CLAZZ_REQUIRED);
        Objects.requireNonNull(consumer, MSG_CONSUMER_REQUIRED);
        baseBatchReadAsObject(() -> buildBufferReader(filePath, conf), clazz, consumer, conf);
    }

    /**
     * 采用默认配置，从文件流中批量消费读取的对象
     */
    public static <T> void batchReadAsObject(InputStream input, Class<T> clazz, Consumer<List<T>> consumer) {
        batchReadAsObject(input, clazz, consumer, CsvConfig.builder().build());
    }

    /**
     * 从文件流中批量消费读取的对象
     */
    public static <T> void batchReadAsObject(InputStream input, Class<T> clazz, Consumer<List<T>> consumer, CsvConfig conf) {
        Objects.requireNonNull(clazz, MSG_CLAZZ_REQUIRED);
        Objects.requireNonNull(consumer, MSG_CONSUMER_REQUIRED);
        baseBatchReadAsObject(() -> buildBufferReader(input, conf), clazz, consumer, conf);
    }

    private static <T> void baseBatchReadAsObject(BufferReaderSupplier supplier, Class<T> clazz, Consumer<List<T>> consumer, CsvConfig conf) {
        try (BufferedReader bufferedReader = supplier.get()) {
            CsvToBean<T> csvToBean = buildCsvToBean(bufferedReader, clazz, conf);
            List<T> batchList = new ArrayList<>(conf.getPageSize());
            for (T obj : csvToBean) {
                batchList.add(obj);
                if (batchList.size() >= conf.getPageSize()) {
                    consumer.accept(batchList);
                    batchList = new ArrayList<>(conf.getPageSize());
                }
            }
            if (!batchList.isEmpty()) {
                consumer.accept(batchList);
            }
            logSkippedRows(conf, csvToBean);
        } catch (Exception e) {
            throw wrapException(e, "批量读取CSV为对象失败");
        }
    }

    /**
     * 采用默认配置，把数据写入csv文件
     */
    public static void write(String filePath, List<String[]> data, String[] header) {
        write(filePath, data, header, CsvConfig.builder().build());
    }

    /**
     * 把数据写入csv文件
     */
    public static void write(String filePath, List<String[]> data, String[] header, CsvConfig conf) {
        batchWrite(filePath, buildDataSupplier(data), header, conf);
    }

    /**
     * 采用默认配置，从数据提供者批量获取数据后写入csv文件
     */
    public static void batchWrite(String filePath, DataSupplier<String[]> dataSupplier, String[] header) {
        batchWrite(filePath, dataSupplier, header, CsvConfig.builder().build());
    }

    /**
     * 从数据提供者批量获取数据后写入csv文件
     */
    public static void batchWrite(String filePath, DataSupplier<String[]> dataSupplier, String[] header, CsvConfig conf) {
        Objects.requireNonNull(dataSupplier, MSG_DATA_SUPPLIER_REQUIRED);
        baseBatchWrite(() -> buildBufferWriter(filePath, conf), dataSupplier, header, conf);
    }

    /**
     * 采用默认配置，把数据写入文件流
     */
    public static void write(OutputStream output, List<String[]> data, String[] header) {
        write(output, data, header, CsvConfig.builder().build());
    }

    /**
     * 把数据写入文件流
     */
    public static void write(OutputStream output, List<String[]> data, String[] header, CsvConfig conf) {
        batchWrite(output, buildDataSupplier(data), header, conf);
    }

    /**
     * 采用默认配置，从数据提供者批量获取数据后写入文件流
     */
    public static void batchWrite(OutputStream output, DataSupplier<String[]> dataSupplier, String[] header) {
        batchWrite(output, dataSupplier, header, CsvConfig.builder().build());
    }

    /**
     * 从数据提供者批量获取数据后写入文件流
     */
    public static void batchWrite(OutputStream output, DataSupplier<String[]> dataSupplier, String[] header, CsvConfig conf) {
        Objects.requireNonNull(dataSupplier, MSG_DATA_SUPPLIER_REQUIRED);
        baseBatchWrite(() -> buildBufferWriter(output, conf), dataSupplier, header, conf);
    }

    /**
     * 按数组进行批量写入
     */
    private static void baseBatchWrite(BufferedWriterSupplier supplier, DataSupplier<String[]> dataSupplier, String[] header, CsvConfig conf) {
        try (BufferedWriter bw = supplier.get();
             ICSVWriter csvWriter = buildCsvWriter(bw, conf)) {

            if (conf.isHasHeader() && header != null && header.length > 0) {
                csvWriter.writeNext(header);
            }

            int pageIdx = 1;
            List<String[]> batch;
            while (!(batch = dataSupplier.getPage(pageIdx, conf.getPageSize())).isEmpty()) {
                if (conf.getNullValue() != null) {
                    sanitizeNulls(batch, conf.getNullValue());
                }
                csvWriter.writeAll(batch);
                pageIdx++;
            }
            csvWriter.flush();
        } catch (Exception e) {
            throw wrapException(e, "写入CSV失败");
        }
    }

    /**
     * 采用默认配置，把对象数据写入csv文件
     */
    public static <T> void writeByObject(String filePath, List<T> data) {
        writeByObject(filePath, data, null, CsvConfig.builder().build());
    }

    /**
     * 把对象数据写入csv文件
     */
    public static <T> void writeByObject(String filePath, List<T> data, String[] header, CsvConfig conf) {
        if (data == null || data.isEmpty())
            return;
        Class<T> clazz = getGenerics(data.get(0));
        if (clazz == null) {
            throw new IllegalArgumentException("无法解析数据元素类型");
        }
        batchWriteByObject(filePath, buildDataSupplier(data), clazz, header, conf);
    }

    /**
     * 采用默认配置，从数据提供者批量获取对象后写入csv文件
     */
    public static <T> void batchWriteByObject(String filePath, DataSupplier<T> dataSupplier, Class<T> clazz) {
        batchWriteByObject(filePath, dataSupplier, clazz, null, CsvConfig.builder().build());
    }

    /**
     * 从数据提供者批量获取对象后写入csv文件
     */
    public static <T> void batchWriteByObject(String filePath, DataSupplier<T> dataSupplier, Class<T> clazz, String[] header, CsvConfig conf) {
        Objects.requireNonNull(dataSupplier, MSG_DATA_SUPPLIER_REQUIRED);
        Objects.requireNonNull(clazz, MSG_CLAZZ_REQUIRED);
        baseBatchWriteByObject(() -> buildBufferWriter(filePath, conf), dataSupplier, clazz, header, conf);
    }

    /**
     * 采用默认配置，把对象数据写入文件流
     */
    public static <T> void writeByObject(OutputStream output, List<T> data) {
        writeByObject(output, data, null, CsvConfig.builder().build());
    }

    /**
     * 把对象数据写入文件流
     */
    public static <T> void writeByObject(OutputStream output, List<T> data, String[] header, CsvConfig conf) {
        if (data == null || data.isEmpty())
            return;
        Class<T> clazz = getGenerics(data.get(0));
        if (clazz == null) {
            throw new IllegalArgumentException("无法解析数据元素类型");
        }
        batchWriteByObject(output, buildDataSupplier(data), clazz, header, conf);
    }

    /**
     * 采用默认配置，从数据提供者批量获取对象后写入文件流
     */
    public static <T> void batchWriteByObject(OutputStream output, DataSupplier<T> dataSupplier, Class<T> clazz) {
        batchWriteByObject(output, dataSupplier, clazz, null, CsvConfig.builder().build());
    }

    /**
     * 从数据提供者批量获取对象后写入文件流
     */
    public static <T> void batchWriteByObject(OutputStream output, DataSupplier<T> dataSupplier, Class<T> clazz, String[] header, CsvConfig conf) {
        Objects.requireNonNull(dataSupplier, MSG_DATA_SUPPLIER_REQUIRED);
        Objects.requireNonNull(clazz, MSG_CLAZZ_REQUIRED);
        baseBatchWriteByObject(() -> buildBufferWriter(output, conf), dataSupplier, clazz, header, conf);
    }

    /**
     * 按对象进行批量写入
     */
    private static <T> void baseBatchWriteByObject(BufferedWriterSupplier supplier, DataSupplier<T> dataSupplier, Class<T> clazz, String[] header, CsvConfig conf) {
        try (BufferedWriter bw = supplier.get();
             ICSVWriter csvWriter = buildCsvWriter(bw, conf)) {

            MappingStrategy<T> mappingStrategy = buildMappingStrategy(clazz);
            boolean isPositionMapping = mappingStrategy instanceof ColumnPositionMappingStrategy;

            if (isPositionMapping && conf.isHasHeader()) {
                csvWriter.writeNext(header != null && header.length > 0 ? header : getHeaderByPosition(clazz));
            }

            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(csvWriter)
                    .withMappingStrategy(mappingStrategy)
                    .build();

            int pageIdx = 1;
            List<T> batch;
            while (!(batch = dataSupplier.getPage(pageIdx, conf.getPageSize())).isEmpty()) {
                beanToCsv.write(batch);
                pageIdx++;
            }
            csvWriter.flush();
        } catch (Exception e) {
            throw wrapException(e, "按对象写入CSV失败");
        }
    }

    /**
     * 创建CSVWriter
     */
    private static ICSVWriter buildCsvWriter(BufferedWriter bufferedWriter, CsvConfig conf) {
        return new CSVWriterBuilder(bufferedWriter)
                .withSeparator(conf.getSeparator())
                .withQuoteChar(conf.getQuoteChar())
                .withEscapeChar(conf.getEscapeChar())
                .withLineEnd(conf.getLineEnd())
                .build();
    }

    /**
     * 获取类中带CsvBindByName注解的字段，按 column 值组成列头比较器
     */
    private static <T> Comparator<String> buildComparator(Class<T> clazz) {
        List<String> fields = FieldUtils.getFieldsListWithAnnotation(clazz, CsvBindByName.class).stream()
                .map(f -> Objects.requireNonNull(f.getAnnotation(CsvBindByName.class)).column().toUpperCase())
                .collect(Collectors.toList());
        return new FixedOrderComparator<>(fields);
    }

    /**
     * 从类中获取带CsvBindByPosition注解的字段，按照position值排序
     */
    private static <T> String[] getHeaderByPosition(Class<T> clazz) {
        return FieldUtils.getFieldsListWithAnnotation(clazz, CsvBindByPosition.class).stream()
                .sorted(Comparator.comparingInt(f -> Objects.requireNonNull(f.getAnnotation(CsvBindByPosition.class)).position()))
                .map(Field::getName)
                .toArray(String[]::new);
    }

    /**
     * 获取对象的运行时类型
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> getGenerics(Object obj) {
        if (obj == null) {
            return null;
        }
        return (Class<T>) ResolvableType.forInstance(obj).getRawClass();
    }

    /**
     * 构建CsvReader
     */
    private static CSVReader buildCsvReader(BufferedReader bufferedReader, CsvConfig conf) {
        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(conf.getSeparator())
                .withQuoteChar(conf.getQuoteChar())
                .withEscapeChar(conf.getEscapeChar())
                .build();

        return new CSVReaderBuilder(bufferedReader)
                .withCSVParser(csvParser)
                .withSkipLines(conf.isHasHeader() ? 1 : 0)
                .build();
    }

    /**
     * 构建CsvToBean
     */
    private static <T> CsvToBean<T> buildCsvToBean(BufferedReader bufferedReader, Class<T> clazz, CsvConfig conf) {
        MappingStrategy<T> mappingStrategy = buildMappingStrategy(clazz);
        boolean isPositionMapping = mappingStrategy instanceof ColumnPositionMappingStrategy;
        int skipLines = isPositionMapping && conf.isHasHeader() ? 1 : 0;
        return new CsvToBeanBuilder<T>(bufferedReader)
                .withType(clazz)
                .withMappingStrategy(mappingStrategy)
                .withSeparator(conf.getSeparator())
                .withQuoteChar(conf.getQuoteChar())
                .withEscapeChar(conf.getEscapeChar())
                .withIgnoreLeadingWhiteSpace(true)
                .withThrowExceptions(conf.isThrowExceptions())
                .withSkipLines(skipLines)
                .build();
    }

    /**
     * 构建映射策略
     */
    private static <T> MappingStrategy<T> buildMappingStrategy(Class<T> clazz) {
        List<Field> nameFieldsList = FieldUtils.getFieldsListWithAnnotation(clazz, CsvBindByName.class);
        List<Field> posFieldsList = FieldUtils.getFieldsListWithAnnotation(clazz, CsvBindByPosition.class);
        MappingStrategy<T> strategy;
        if (!nameFieldsList.isEmpty() && posFieldsList.isEmpty()) {
            HeaderColumnNameMappingStrategy<T> mappingStrategy = new HeaderColumnNameMappingStrategy<>();
            mappingStrategy.setColumnOrderOnWrite(buildComparator(clazz));
            strategy = mappingStrategy;
        } else if (!posFieldsList.isEmpty() && nameFieldsList.isEmpty()) {
            strategy = new ColumnPositionMappingStrategy<>();
        } else {
            throw new IllegalArgumentException("请单独使用CsvBindByName或CsvBindByPosition注解");
        }
        strategy.setType(clazz);
        return strategy;
    }

    /**
     * 当配置为不抛异常(throwExceptions=false)时，统计并告警被跳过的错误行数量。
     */
    private static <T> void logSkippedRows(CsvConfig conf, CsvToBean<T> csvToBean) {
        if (conf.isThrowExceptions()) {
            return;
        }
        int skipped = csvToBean.getCapturedExceptions().size();
        if (skipped > 0) {
            log.warn("读取CSV为对象：跳过 {} 行解析异常的数据（如字段数与表头不一致）", skipped);
        }
    }

    private static BufferedReader buildBufferReader(String filePath, CsvConfig conf) throws IOException {
        Assert.hasText(filePath, MSG_FILE_PATH_REQUIRED);
        Objects.requireNonNull(conf, MSG_CONF_REQUIRED);
        return buildBufferReader(Files.newInputStream(Paths.get(filePath)), conf);
    }

    private static BufferedReader buildBufferReader(InputStream input, CsvConfig conf) {
        Objects.requireNonNull(input, "输入流不能为空");
        Objects.requireNonNull(conf, MSG_CONF_REQUIRED);
        int size = Math.max(conf.getCharBufferSize(), MIN_CHAR_BUFFER_SIZE);
        return new BufferedReader(new InputStreamReader(input, conf.getCharset()), size);
    }

    private static BufferedWriter buildBufferWriter(String filePath, CsvConfig conf) throws IOException {
        Assert.hasText(filePath, MSG_FILE_PATH_REQUIRED);
        Objects.requireNonNull(conf, MSG_CONF_REQUIRED);
        StandardOpenOption mode = conf.isAppend() ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;
        OutputStream out = Files.newOutputStream(Paths.get(filePath),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, mode);
        return buildBufferWriter(out, conf);
    }

    private static BufferedWriter buildBufferWriter(OutputStream output, CsvConfig conf) {
        Objects.requireNonNull(output, "输出流不能为空");
        Objects.requireNonNull(conf, MSG_CONF_REQUIRED);
        int size = Math.max(conf.getCharBufferSize(), MIN_CHAR_BUFFER_SIZE);
        return new BufferedWriter(new OutputStreamWriter(output, conf.getCharset()), size);
    }

    /**
     * 把数组中的 null 替换为指定占位符，避免输出 "null" 字符串
     */
    private static void sanitizeNulls(List<String[]> batch, String nullValue) {
        for (String[] row : batch) {
            if (row == null) continue;
            for (int i = 0; i < row.length; i++) {
                if (row[i] == null) {
                    row[i] = nullValue;
                }
            }
        }
    }


    /**
     * 统一异常包装：保留原异常作为 cause，按类型记录差异化日志，
     * 抛出统一的 {@link CsvException}，便于调用方区分处理。
     * 参数校验异常（{@link IllegalArgumentException}/{@link NullPointerException}）原样重抛，不做包装。
     */
    private static CsvException wrapException(Exception e, String message) {
        if (e instanceof IllegalArgumentException) {
            throw (IllegalArgumentException) e;
        }
        if (e instanceof NullPointerException) {
            throw (NullPointerException) e;
        }
        Throwable root = e.getCause() != null ? e.getCause() : e;
        if (e instanceof CsvException) {
            return (CsvException) e;
        }
        if (e instanceof CsvRequiredFieldEmptyException || root instanceof CsvRequiredFieldEmptyException) {
            log.error("CSV 必填字段缺失: {}", e.getMessage());
        } else if (e instanceof CsvValidationException || root instanceof CsvValidationException) {
            log.error("CSV 格式验证失败: {}", e.getMessage());
        } else if (e instanceof IOException || root instanceof IOException) {
            log.error("CSV IO 失败: {}", e.getMessage());
        } else {
            log.error("CSV 处理异常: {}", e.getMessage(), e);
        }
        return new CsvException(message + ": " + e.getMessage(), e);
    }

    /**
     * CSV 操作专用异常
     */
    public static class CsvException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CsvException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @FunctionalInterface
    private interface BufferReaderSupplier {
        BufferedReader get() throws IOException;
    }

    @FunctionalInterface
    private interface BufferedWriterSupplier {
        BufferedWriter get() throws IOException;
    }

    /**
     * 用于批量提供数据，分页返回，避免一次性加载全量数据
     */
    @FunctionalInterface
    public interface DataSupplier<T> {
        List<T> getPage(int pageIdx, int pageSize);
    }

    /**
     * 构建数据提供者
     */
    private static <T> DataSupplier<T> buildDataSupplier(List<T> data) {
        return (pageIdx, pageSize) -> {
            if (data == null || data.isEmpty()) {
                return Collections.emptyList();
            }
            int offset = (pageIdx - 1) * pageSize;
            if (offset < 0 || offset >= data.size()) {
                return Collections.emptyList();
            }
            return data.subList(offset, Math.min(offset + pageSize, data.size()));
        };
    }

}
