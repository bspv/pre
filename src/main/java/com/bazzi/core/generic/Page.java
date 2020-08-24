package com.bazzi.core.generic;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Page<T> implements Serializable {
	private static final long serialVersionUID = -5547536957621580709L;
	private Integer pageIdx = 1;// 当前页码
	private Integer pageSize = 10;// 每页大小
	private List<T> records;// 数据
	private Integer totalRow;// 总记录数
	private Integer totalPage;// 总页数
	private boolean hasPrev;// 是否有上一页
	private boolean hasNext;// 是否有下一页

	public Page() {
	}

	public Page(List<T> records, Integer pageIdx, Integer pageSize, Integer totalRow) {
		this.records = records;
		this.pageIdx = pageIdx;
		this.pageSize = pageSize;
		this.totalRow = totalRow;
		this.totalPage = totalRow % pageSize == 0 ? totalRow / pageSize : totalRow / pageSize + 1;
		this.hasPrev = pageIdx > 1;
		this.hasNext = pageIdx < totalPage;
	}

	public static <T> Page<T> of(List<T> records, Integer pageIdx, Integer pageSize, Integer totalRow) {
		Page<T> page = new Page<>();
		page.setPageIdx(pageIdx);
		page.setPageSize(pageSize);
		page.setRecords(records);
		page.setTotalRow(totalRow);
		int totalPage = totalRow % pageSize == 0 ? totalRow / pageSize : totalRow / pageSize + 1;
		page.setTotalPage(totalPage);
		page.setHasPrev(pageIdx > 1);
		page.setHasNext(pageIdx < totalPage);
		return page;
	}

}
