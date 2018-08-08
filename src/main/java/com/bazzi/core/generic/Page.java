package com.bazzi.core.generic;

import java.io.Serializable;
import java.util.List;

public class Page implements Serializable {
	private static final long serialVersionUID = 6095275634490272398L;
	private Integer current = 1;// 当前页码
	private Integer pageSize = 10;// 每页大小
	private List<?> records;// 数据
	private Integer totalRow;// 总记录数
	private Integer totalPage;// 总页数
	private Integer rowIdx;// 从第几条记录开始

	public Integer getCurrent() {
		return current;
	}

	public void setCurrent(Integer current) {
		this.current = current;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public List<?> getRecords() {
		return records;
	}

	public void setRecords(List<?> records) {
		this.records = records;
	}

	public Integer getTotalRow() {
		return totalRow;
	}

	public void setTotalRow(Integer totalRow) {
		this.totalRow = totalRow;
		this.totalPage = totalRow % pageSize == 0 ? totalRow / pageSize : totalRow / pageSize + 1;
	}

	public Integer getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

	public Integer getRowIdx() {
		rowIdx = pageSize * (current - 1);
		return rowIdx;
	}

	public void setRowIdx(Integer rowIdx) {
		this.rowIdx = rowIdx;
	}

	public boolean hasPrev() {
		return current > 1;
	}

	public boolean hasNext() {
		return current < totalPage;
	}

}
