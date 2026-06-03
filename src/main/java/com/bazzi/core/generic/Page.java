package com.bazzi.core.generic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@ApiModel(value = "Page")
public final class Page<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 556003819211216309L;

	@Builder.Default
	@ApiModelProperty(value = "当前页码")
	private Integer pageIdx = 1;// 当前页码

	@Builder.Default
	@ApiModelProperty(value = "每页大小")
	private Integer pageSize = 10;// 每页大小

	@ApiModelProperty(value = "数据集合")
	private List<T> records;// 数据

	@ApiModelProperty(value = "总记录数")
	private Integer totalRow;// 总记录数

	@ApiModelProperty(value = "总页数")
	private Integer totalPage;// 总页数

	@ApiModelProperty(value = "是否有上一页")
	private boolean hasPrev;// 是否有上一页

	@ApiModelProperty(value = "是否有下一页")
	private boolean hasNext;// 是否有下一页

	public static <T extends Serializable> Page<T> of(List<T> records,
													  Integer pageIdx, Integer pageSize, Integer totalRow) {
		int totalPage = totalRow % pageSize == 0 ? totalRow / pageSize : totalRow / pageSize + 1;
		return Page.<T>builder().pageIdx(pageIdx)
				.pageSize(pageSize).records(records)
				.totalRow(totalRow).totalPage(totalPage)
				.hasPrev(pageIdx > 1).hasNext(pageIdx < totalPage).build();
	}

}
