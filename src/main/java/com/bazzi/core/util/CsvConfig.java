package com.bazzi.core.util;

import com.opencsv.ICSVParser;
import com.opencsv.ICSVWriter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
@Builder
public final class CsvConfig {
    @Builder.Default
    private Charset charset = StandardCharsets.UTF_8; // 文件编码
    @Builder.Default
    private boolean hasHeader = true; // 是否有表头
    @Builder.Default
    private char separator = ICSVParser.DEFAULT_SEPARATOR; // 行内字段分隔符
    @Builder.Default
    private int pageSize = 5000; // 分页大小，每次处理的行数
    @Builder.Default
    private int charBufferSize = 32 * 1024;
    // 缓冲区字符数（每个 char 占 2 字节，32K chars ≈ 64KB 堆）。
    // 主要用于减少向 InputStreamReader 的调用次数；上游 StreamDecoder
    // 默认仅 8KB 字节缓冲，过大值收益递减。Java 默认 8192 chars。
    @Builder.Default
    private char quoteChar = ICSVWriter.NO_QUOTE_CHARACTER; // 读写共用的引号字符，默认不加引号（即不做引号解析）；
    // 读取不规范、字段未加引号的脏数据时保持默认值，可避免落单的 " 把后续多行吞并为一条记录；
    // 若数据是规范 csv（含被引号保护的分隔符/换行），读写时都应设为 '"'
    @Builder.Default
    private char escapeChar = ICSVWriter.NO_ESCAPE_CHARACTER; // 读写共用的转义字符，默认不转义
    @Builder.Default
    private String lineEnd = ICSVWriter.DEFAULT_LINE_END; // 写入时的行结束符
    @Builder.Default
    private boolean append = false; // 写入文件时是否追加（true 追加；false 覆盖）
    @Builder.Default
    private String nullValue = ""; // 写入数组时，null 替换为该值
    @Builder.Default
    private boolean throwExceptions = false;
    // 读取为对象(readAsObject/batchReadAsObject)时遇到错误行（如字段数与表头不一致）的处理策略：
    // false（默认）=跳过该行并继续，容忍脏数据，符合数组读取路径的宽容行为；
    // true=立即抛异常中断整个读取。

}
