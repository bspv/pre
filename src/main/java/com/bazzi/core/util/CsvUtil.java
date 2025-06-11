package com.bazzi.core.util;

import com.opencsv.*;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.core.ResolvableType;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public final class CsvUtil {
    private static final char DEFAULT_QUOTE = ICSVParser.DEFAULT_QUOTE_CHARACTER;
    private static final char DEFAULT_ESCAPE = ICSVParser.DEFAULT_ESCAPE_CHARACTER;

    private CsvUtil() {
        throw new UnsupportedOperationException("不能创建CsvUtil对象");
    }

    /**
     * 采用默认配置，把csv文件读取成数组
     *
     * @param filePath 文件路径
     * @return 全量数据
     */
    public static List<String[]> read(String filePath) {
        return read(filePath, CsvConfig.builder().build());
    }

    /**
     * 把csv文件读取成数组
     *
     * @param filePath 文件路径
     * @param conf     配置
     * @return 全量数据
     */
    public static List<String[]> read(String filePath, CsvConfig conf) {
        return baseRead(() -> buildBufferReader(filePath, conf), conf);
    }

    /**
     * 采用默认配置，从文件流中读取成数组
     *
     * @param input 文件流
     * @return 全量数据
     */
    public static List<String[]> read(InputStream input) {
        return read(input, CsvConfig.builder().build());
    }

    /**
     * 从文件流中读取成数组
     *
     * @param input 文件流
     * @param conf  配置
     * @return 全量数据
     */
    public static List<String[]> read(InputStream input, CsvConfig conf) {
        return baseRead(() -> buildBufferReader(input, conf), conf);
    }

    /**
     * 读取成数组
     *
     * @param supplier BufferedReader
     * @param conf     配置
     * @return 全量数据
     */
    private static List<String[]> baseRead(BufferReaderSupplier supplier, CsvConfig conf) {
        try (CSVReader csvReader = buildCsvReader(supplier.get(), conf)) {
            return csvReader.readAll();
        } catch (Exception e) {
            handleException(e);
            return Collections.emptyList();
        }
    }

    /**
     * 采用默认配置，把csv文件读取成对象
     *
     * @param filePath 文件路径
     * @param clazz    类型
     * @return 全量数据
     */
    public static <T> List<T> readAsObject(String filePath, Class<T> clazz) {
        return readAsObject(filePath, clazz, CsvConfig.builder().build());
    }

    /**
     * 把csv文件读取成对象
     *
     * @param filePath 文件路径
     * @param clazz    类型
     * @param conf     配置
     * @return 全量数据
     */
    public static <T> List<T> readAsObject(String filePath, Class<T> clazz, CsvConfig conf) {
        return baseReadAsObject(() -> buildBufferReader(filePath, conf), clazz, conf);
    }

    /**
     * 采用默认配置，从文件流中读取成对象
     *
     * @param input 文件流
     * @param clazz 类型
     * @return 全量数据
     */
    public static <T> List<T> readAsObject(InputStream input, Class<T> clazz) {
        return readAsObject(input, clazz, CsvConfig.builder().build());
    }

    /**
     * 从文件流中读取成对象
     *
     * @param input 文件流
     * @param clazz 类型
     * @param conf  配置
     * @return 全量数据
     */
    public static <T> List<T> readAsObject(InputStream input, Class<T> clazz, CsvConfig conf) {
        return baseReadAsObject(() -> buildBufferReader(input, conf), clazz, conf);
    }

    /**
     * 读取成对象
     *
     * @param supplier BufferedReader
     * @param clazz    类型
     * @param conf     配置
     * @return 全量数据
     */
    private static <T> List<T> baseReadAsObject(BufferReaderSupplier supplier, Class<T> clazz, CsvConfig conf) {
        try (BufferedReader bufferedReader = supplier.get()) {
            CsvToBean<T> csvToBean = buildCsvToBean(bufferedReader, clazz, conf);
            return csvToBean.parse();
        } catch (IOException e) {
            handleException(e);
            return Collections.emptyList();
        }
    }

    /**
     * 采用默认配置，从csv文件中批量消费读取的数组
     *
     * @param filePath      文件路径
     * @param batchConsumer 批量消费者
     */
    public static void batchRead(String filePath, Consumer<List<String[]>> batchConsumer) {
        batchRead(filePath, batchConsumer, CsvConfig.builder().build());
    }

    /**
     * 从csv文件中批量消费读取的数组
     *
     * @param filePath      文件路径
     * @param batchConsumer 批量消费者
     * @param conf          配置
     */
    public static void batchRead(String filePath, Consumer<List<String[]>> batchConsumer, CsvConfig conf) {
        baseBatchRead(() -> buildBufferReader(filePath, conf), batchConsumer, conf);
    }

    /**
     * 采用默认配置，从文件流中批量消费读取的数组
     *
     * @param input         文件流
     * @param batchConsumer 批量消费者
     */
    public static void batchRead(InputStream input, Consumer<List<String[]>> batchConsumer) {
        batchRead(input, batchConsumer, CsvConfig.builder().build());
    }

    /**
     * 从文件流中批量消费读取的数组
     *
     * @param input         文件流
     * @param batchConsumer 批量消费者
     * @param conf          配置
     */
    public static void batchRead(InputStream input, Consumer<List<String[]>> batchConsumer, CsvConfig conf) {
        baseBatchRead(() -> buildBufferReader(input, conf), batchConsumer, conf);
    }

    /**
     * 批量读取成数组
     *
     * @param supplier      BufferedReader
     * @param batchConsumer 批量消费者
     * @param conf          配置
     */
    private static void baseBatchRead(BufferReaderSupplier supplier, Consumer<List<String[]>> batchConsumer, CsvConfig conf) {
        try (CSVReader csvReader = buildCsvReader(supplier.get(), conf)) {
            List<String[]> batchList = new ArrayList<>(conf.getPageSize());
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                batchList.add(nextLine);
                if (batchList.size() >= conf.getPageSize()) {
                    batchConsumer.accept(batchList);

                    batchList = new ArrayList<>(conf.getPageSize());
                }
            }

            if (!batchList.isEmpty()) {
                batchConsumer.accept(batchList);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * 采用默认配置，从csv文件中批量消费读取的对象
     *
     * @param filePath      文件路径
     * @param clazz         类型
     * @param batchConsumer 批量消费者
     */
    public static <T> void batchReadAsObject(String filePath, Class<T> clazz, Consumer<List<T>> batchConsumer) {
        batchReadAsObject(filePath, clazz, batchConsumer, CsvConfig.builder().build());
    }

    /**
     * 从csv文件中批量消费读取的对象
     *
     * @param filePath      文件路径
     * @param clazz         类型
     * @param batchConsumer 批量消费者
     * @param conf          配置
     */
    public static <T> void batchReadAsObject(String filePath, Class<T> clazz, Consumer<List<T>> batchConsumer, CsvConfig conf) {
        baseBatchReadAsObject(() -> buildBufferReader(filePath, conf), clazz, batchConsumer, conf);
    }

    /**
     * 采用默认配置，从文件流中批量消费读取的对象
     *
     * @param input         文件流
     * @param clazz         类型
     * @param batchConsumer 批量消费者
     */
    public static <T> void batchReadAsObject(InputStream input, Class<T> clazz, Consumer<List<T>> batchConsumer) {
        batchReadAsObject(input, clazz, batchConsumer, CsvConfig.builder().build());
    }

    /**
     * 从文件流中批量消费读取的对象
     *
     * @param input         文件流
     * @param clazz         类型
     * @param batchConsumer 批量消费者
     * @param conf          配置
     */
    public static <T> void batchReadAsObject(InputStream input, Class<T> clazz, Consumer<List<T>> batchConsumer, CsvConfig conf) {
        baseBatchReadAsObject(() -> buildBufferReader(input, conf), clazz, batchConsumer, conf);
    }

    /**
     * 批量读取成对象
     *
     * @param supplier      BufferedReader
     * @param clazz         类型
     * @param batchConsumer 批量消费者
     * @param conf          配置
     */
    private static <T> void baseBatchReadAsObject(BufferReaderSupplier supplier, Class<T> clazz, Consumer<List<T>> batchConsumer, CsvConfig conf) {
        try (BufferedReader bufferedReader = supplier.get()) {
            CsvToBean<T> csvToBean = buildCsvToBean(bufferedReader, clazz, conf);

            List<T> batchList = new ArrayList<>(conf.getPageSize());
            for (T obj : csvToBean) {
                batchList.add(obj);
                if (batchList.size() >= conf.getPageSize()) {
                    batchConsumer.accept(batchList);

                    batchList = new ArrayList<>(conf.getPageSize());
                }
            }

            if (!batchList.isEmpty()) {
                batchConsumer.accept(batchList);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * 采用默认配置，把数据写入csv文件
     *
     * @param filePath 文件路径
     * @param data     数据
     * @param header   列头
     */
    public static void write(String filePath, List<String[]> data, String[] header) {
        write(filePath, data, header, CsvConfig.builder().build());
    }

    /**
     * 把数据写入csv文件
     *
     * @param filePath 文件路径
     * @param data     数据
     * @param header   列头
     * @param conf     配置
     */
    public static void write(String filePath, List<String[]> data, String[] header, CsvConfig conf) {
        batchWrite(filePath, buildDataSupplier(data), header, conf);
    }

    /**
     * 采用默认配置，从数据提供者批量获取数据后写入csv文件
     *
     * @param filePath     文件路径
     * @param dataSupplier 数据提供者
     * @param header       列头
     */
    public static void batchWrite(String filePath, DataSupplier<String[]> dataSupplier, String[] header) {
        batchWrite(filePath, dataSupplier, header, CsvConfig.builder().build());
    }

    /**
     * 从数据提供者批量获取数据后写入csv文件
     *
     * @param filePath     文件路径
     * @param dataSupplier 数据提供者
     * @param header       列头
     * @param conf         配置
     */
    public static void batchWrite(String filePath, DataSupplier<String[]> dataSupplier, String[] header, CsvConfig conf) {
        baseBatchWrite(() -> buildBufferWriter(filePath, conf), dataSupplier, header, conf);
    }

    /**
     * 采用默认配置，把数据写入文件流
     *
     * @param output 文件流
     * @param data   数据
     * @param header 列头
     */
    public static void write(OutputStream output, List<String[]> data, String[] header) {
        write(output, data, header, CsvConfig.builder().build());
    }

    /**
     * 把数据写入文件流
     *
     * @param output 文件流
     * @param data   数据
     * @param header 列头
     * @param conf   配置
     */
    public static void write(OutputStream output, List<String[]> data, String[] header, CsvConfig conf) {
        batchWrite(output, buildDataSupplier(data), header, conf);
    }

    /**
     * 采用默认配置，从数据提供者批量获取数据后写入文件流
     *
     * @param output       文件流
     * @param dataSupplier 数据提供者
     * @param header       列头
     */
    public static void batchWrite(OutputStream output, DataSupplier<String[]> dataSupplier, String[] header) {
        batchWrite(output, dataSupplier, header, CsvConfig.builder().build());
    }

    /**
     * 从数据提供者批量获取数据后写入文件流
     *
     * @param output       文件流
     * @param dataSupplier 数据提供者
     * @param header       列头
     * @param conf         配置
     */
    public static void batchWrite(OutputStream output, DataSupplier<String[]> dataSupplier, String[] header, CsvConfig conf) {
        baseBatchWrite(() -> buildBufferWriter(output, conf), dataSupplier, header, conf);
    }

    /**
     * 按数组进行批量写入
     *
     * @param supplier     BufferedWriter
     * @param dataSupplier 数据提供者
     * @param header       列头数组
     * @param conf         配置
     */
    private static void baseBatchWrite(BufferedWriterSupplier supplier, DataSupplier<String[]> dataSupplier, String[] header, CsvConfig conf) {
        try {
            ICSVWriter csvWriter = buildCsvWriter(supplier.get(), conf.getSeparator());

            if (conf.isHasHeader() && header != null && header.length > 0) {
                csvWriter.writeNext(header);
            }

            int pageIdx = 1;
            List<String[]> batch;
            while (!(batch = dataSupplier.getPage(pageIdx, conf.getPageSize())).isEmpty()) {
                csvWriter.writeAll(batch);
                csvWriter.flush();
                pageIdx++;
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * 采用默认配置，把对象数据写入csv文件
     *
     * @param filePath 文件路径
     * @param data     数据
     */
    public static <T> void writeByObject(String filePath, List<T> data) {
        writeByObject(filePath, data, null, CsvConfig.builder().build());
    }

    /**
     * 把对象数据写入csv文件
     *
     * @param filePath 文件路径
     * @param data     数据
     * @param header   列头
     * @param conf     配置
     */
    public static <T> void writeByObject(String filePath, List<T> data, String[] header, CsvConfig conf) {
        if (data == null || data.isEmpty())
            return;
        batchWriteByObject(filePath, buildDataSupplier(data), getGenerics(data.get(0)), header, conf);
    }

    /**
     * 采用默认配置，从数据提供者批量获取对象后写入csv文件
     *
     * @param filePath     文件路径
     * @param dataSupplier 数据提供者
     * @param clazz        类型
     */
    public static <T> void batchWriteByObject(String filePath, DataSupplier<T> dataSupplier, Class<T> clazz) {
        batchWriteByObject(filePath, dataSupplier, clazz, null, CsvConfig.builder().build());
    }

    /**
     * 从数据提供者批量获取对象后写入csv文件
     *
     * @param filePath     文件路径
     * @param dataSupplier 数据提供者
     * @param clazz        类型
     * @param header       列头
     * @param conf         配置
     */
    public static <T> void batchWriteByObject(String filePath, DataSupplier<T> dataSupplier, Class<T> clazz, String[] header, CsvConfig conf) {
        baseBatchWriteByObject(() -> buildBufferWriter(filePath, conf), dataSupplier, clazz, header, conf);
    }

    /**
     * 采用默认配置，把对象数据写入文件流
     *
     * @param output 文件流
     * @param data   数据
     */
    public static <T> void writeByObject(OutputStream output, List<T> data) {
        writeByObject(output, data, null, CsvConfig.builder().build());
    }

    /**
     * 把对象数据写入文件流
     *
     * @param output 文件流
     * @param data   数据
     * @param header 列头
     * @param conf   配置
     */
    public static <T> void writeByObject(OutputStream output, List<T> data, String[] header, CsvConfig conf) {
        if (data == null || data.isEmpty())
            return;
        batchWriteByObject(output, buildDataSupplier(data), getGenerics(data.get(0)), header, conf);
    }

    /**
     * 采用默认配置，从数据提供者批量获取对象后写入文件流
     *
     * @param output       文件流
     * @param dataSupplier 数据提供者
     * @param clazz        类型
     */
    public static <T> void batchWriteByObject(OutputStream output, DataSupplier<T> dataSupplier, Class<T> clazz) {
        batchWriteByObject(output, dataSupplier, clazz, null, CsvConfig.builder().build());
    }

    /**
     * 从数据提供者批量获取对象后写入文件流
     *
     * @param output       文件流
     * @param dataSupplier 数据提供者
     * @param clazz        类型
     * @param header       列头
     * @param conf         配置
     */
    public static <T> void batchWriteByObject(OutputStream output, DataSupplier<T> dataSupplier, Class<T> clazz, String[] header, CsvConfig conf) {
        baseBatchWriteByObject(() -> buildBufferWriter(output, conf), dataSupplier, clazz, header, conf);
    }

    /**
     * 按对象进行批量写入
     *
     * @param supplier     BufferedWriter
     * @param dataSupplier 数据提供者
     * @param clazz        类型
     * @param header       列头数组
     * @param conf         配置
     */
    private static <T> void baseBatchWriteByObject(BufferedWriterSupplier supplier, DataSupplier<T> dataSupplier, Class<T> clazz, String[] header, CsvConfig conf) {
        try {
            MappingStrategy<T> mappingStrategy = buildMappingStrategy(clazz);
            boolean isPositionMapping = mappingStrategy.getClass().isAssignableFrom(ColumnPositionMappingStrategy.class);

            ICSVWriter csvWriter = buildCsvWriter(supplier.get(), conf.getSeparator());
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
                csvWriter.flush();
                pageIdx++;
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * 创建CSVWriter
     *
     * @param bufferedWriter 写入器
     * @param separator      分隔符
     * @return CSVWriter
     */
    private static ICSVWriter buildCsvWriter(BufferedWriter bufferedWriter, char separator) {
        return new CSVWriterBuilder(bufferedWriter)
                .withSeparator(separator)
                .withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
                .withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER)
                .withLineEnd(ICSVWriter.DEFAULT_LINE_END)
                .build();
    }

    /**
     * 获取类中带CsvBindByName注解的字段，采用column值组成列头数组
     *
     * @param clazz 类型
     * @return 属性名CsvBindByName的column值组成的列头数组
     */
    private static <T> Comparator<String> buildComparator(Class<T> clazz) {
        List<String> fields = FieldUtils.getFieldsListWithAnnotation(clazz, CsvBindByName.class).stream()
                .map(f -> Objects.requireNonNull(f.getAnnotation(CsvBindByName.class)).column().toUpperCase())
                .collect(Collectors.toList());
        return new FixedOrderComparator<>(fields);
    }

    /**
     * 从类中获取带CsvBindByPosition注解的字段，按照position值排序
     *
     * @param clazz 类
     * @return 属性名组成的列头数组
     */
    private static <T> String[] getHeaderByPosition(Class<T> clazz) {
        return FieldUtils.getFieldsListWithAnnotation(clazz, CsvBindByPosition.class).stream()
                .sorted(Comparator.comparingInt(f -> Objects.requireNonNull(f.getAnnotation(CsvBindByPosition.class)).position()))
                .map(Field::getName)
                .toArray(String[]::new);
    }

    /**
     * 获取类型
     *
     * @param obj 对象
     * @return 获取类型
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> getGenerics(Object obj) {
        return (Class<T>) ResolvableType.forInstance(obj).getRawClass();
    }

    /**
     * 构建CsvReader
     *
     * @param bufferedReader 读取器
     * @param conf           配置
     * @return CSVReader
     */
    private static CSVReader buildCsvReader(BufferedReader bufferedReader, CsvConfig conf) {
        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator(conf.getSeparator())
                .withQuoteChar(DEFAULT_QUOTE)
                .withEscapeChar(DEFAULT_ESCAPE)
                .build();

        return new CSVReaderBuilder(bufferedReader)
                .withCSVParser(csvParser)
                .withSkipLines(conf.isHasHeader() ? 1 : 0)
                .build();
    }

    /**
     * 构建CsvToBean
     *
     * @param bufferedReader 读取器
     * @param clazz          类型
     * @param conf           配置
     * @return CsvToBean
     */
    private static <T> CsvToBean<T> buildCsvToBean(BufferedReader bufferedReader, Class<T> clazz, CsvConfig conf) {
        MappingStrategy<T> mappingStrategy = buildMappingStrategy(clazz);
        boolean isPositionMapping = mappingStrategy.getClass().isAssignableFrom(ColumnPositionMappingStrategy.class);
        int skipLines = isPositionMapping && conf.isHasHeader() ? 1 : 0;
        return new CsvToBeanBuilder<T>(bufferedReader)
                .withType(clazz)
                .withMappingStrategy(mappingStrategy)
                .withSeparator(conf.getSeparator())
                .withQuoteChar(DEFAULT_QUOTE)
                .withEscapeChar(DEFAULT_ESCAPE)
                .withIgnoreLeadingWhiteSpace(true)
                .withSkipLines(skipLines)
                .build();
    }

    /**
     * 构建映射策略
     *
     * @param clazz 类型
     * @return 返回映射策略
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
     * 根据文件路径、编码、字符缓存区大小，构建BufferedReader
     *
     * @param filePath 文件路径
     * @param conf     配置
     * @return BufferedReader
     * @throws IOException 异常
     */
    private static BufferedReader buildBufferReader(String filePath, CsvConfig conf) throws IOException {
        return buildBufferReader(Files.newInputStream(Paths.get(filePath)), conf);
    }

    /**
     * 根据输入流、编码、字符缓存区大小，构建BufferedReader
     *
     * @param input 输入流
     * @param conf  配置
     * @return BufferedReader
     */
    private static BufferedReader buildBufferReader(InputStream input, CsvConfig conf) {
        return new BufferedReader(new InputStreamReader(input, conf.getCharset()), conf.getCharBufferSize());
    }

    /**
     * 根据文件路径、编码、字符缓存区大小，构建BufferedWriter
     *
     * @param filePath 文件路径
     * @param conf     配置
     * @return BufferedWriter
     * @throws IOException 异常
     */
    private static BufferedWriter buildBufferWriter(String filePath, CsvConfig conf) throws IOException {
        return buildBufferWriter(Files.newOutputStream(Paths.get(filePath)), conf);
    }

    /**
     * 根据输出流、编码、字符缓存区大小，构建BufferedWriter
     *
     * @param output 输出流
     * @param conf   配置
     * @return BufferedWriter
     */
    private static BufferedWriter buildBufferWriter(OutputStream output, CsvConfig conf) {
        return new BufferedWriter(new OutputStreamWriter(output, conf.getCharset()), conf.getCharBufferSize());
    }

    /**
     * 统一的异常处理逻辑
     *
     * @param e 异常
     */
    private static void handleException(Exception e) {
        if (e instanceof CsvRequiredFieldEmptyException
                || e.getCause() instanceof CsvRequiredFieldEmptyException) {
            log.error("CSV 必填字段缺失: {}", e.getMessage());
        } else if (e instanceof CsvValidationException
                || e.getCause() instanceof CsvValidationException) {
            log.error("CSV 格式验证失败: {}", e.getMessage());
        } else if (e instanceof IOException) {
            log.error("CSV 文件读取/写入失败: {}", e.getMessage());
        } else {
            //其他异常，记录堆栈信息
            log.error(e.getMessage(), e);
        }

        throw new IllegalArgumentException(e);
    }

    /**
     * 构建BufferReader提供者
     */
    @FunctionalInterface
    private interface BufferReaderSupplier {
        BufferedReader get() throws IOException;
    }

    /**
     * 构建BufferWriter提供者
     */
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
     *
     * @param data 全量数据
     * @return 数据提供者
     */
    private static <T> DataSupplier<T> buildDataSupplier(List<T> data) {
        return (pageIdx, pageSize) -> {
            int offset = (pageIdx - 1) * pageSize;

            // 检查偏移量是否超出范围
            if (offset >= data.size() || offset < 0)
                return Collections.emptyList();

            return data.subList(offset, Math.min(offset + pageSize, data.size()));
        };
    }

}
