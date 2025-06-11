package com.bazzi.core.util;

import com.opencsv.ICSVParser;
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
    private int charBufferSize = 64 * 1024; // 缓冲区大小，缺省按照SSD硬盘配置，Java默认缓冲区大小为 8192 字符（约16KB，每个Java char占 2 字节）

}