package com.bazzi.core.generic;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public final class Page<T> implements Serializable {
    private static final long serialVersionUID = 5564838797824564651L;
    private Integer pageIdx = 1;// 当前页码
    private Integer pageSize = 10;// 每页大小
    private List<T> records;// 数据
    private Integer totalRow;// 总记录数
    private Integer totalPage;// 总页数
    private boolean hasPrev;// 是否有上一页
    private boolean hasNext;// 是否有下一页

//    public Page(List<T> records, Integer pageIdx, Integer pageSize, Integer totalRow) {
//        this.records = records;
//        this.pageIdx = pageIdx;
//        this.pageSize = pageSize;
//        this.totalRow = totalRow;
//        this.totalPage = totalRow % pageSize == 0 ? totalRow / pageSize : totalRow / pageSize + 1;
//        this.hasPrev = pageIdx > 1;
//        this.hasNext = pageIdx < totalPage;
//    }

    public static <T> Page<T> of(List<T> records, Integer pageIdx, Integer pageSize, Integer totalRow) {
        int totalPage = totalRow % pageSize == 0 ? totalRow / pageSize : totalRow / pageSize + 1;
        return Page.<T>builder().pageIdx(pageIdx)
                .pageSize(pageSize).records(records)
                .totalRow(totalRow).totalPage(totalPage)
                .hasPrev(pageIdx > 1).hasNext(pageIdx < totalPage).build();
    }

}
