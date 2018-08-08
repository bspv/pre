package com.bazzi.core.util;

import com.bazzi.core.annotation.CellAttribute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.ServletOutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public final class ExportExcel {
	private static final int ROW_ACCESS_WINDOW_SIZE = 2000;

	private static final int XLS_ROW = 65536;
	private static final int XLS_COL = 256;
	private static final int XLSX_ROW = 1048576;
	private static final int XLSX_COL = 16384;

	private static final ThreadLocal<CellStyle> headerLocal = new ThreadLocal<>();
	private static final ThreadLocal<CellStyle> bodyLocal = new ThreadLocal<>();
	private static final ThreadLocal<CellStyle> totalLocal = new ThreadLocal<>();

	/**
	 * 生成Excel，并写入response的OutputStream里
	 *
	 * @param outputStream ServletOutputStream
	 * @param title        标题，如果为空则不生成标题
	 * @param body         内容
	 * @param total        总计，如果为空则不生成总计
	 * @param typeClazz    对象class
	 * @param isXlsx       true代表生成XLSX，否则生成XLS
	 */
	public static <T> void generateExcel(ServletOutputStream outputStream, String title,
										 List<T> body, List<String> total, Class<T> typeClazz, boolean isXlsx) {
		if (outputStream == null || body == null || typeClazz == null)
			throw new IllegalArgumentException("Property outputStream && body && typeClazz is required");
		int rowTotal = (title == null || "".equals(title) ? 0 : 1) + 1 + body.size()
				+ (total == null ? 0 : total.size());//总行数
		// 判断总行数是不是超过了最大的行数限制
		if ((!isXlsx && rowTotal > XLS_ROW) || rowTotal > XLSX_ROW)
			throw new IllegalArgumentException(
					"TotalRow(" + rowTotal + " > " + (isXlsx ? XLSX_ROW : XLS_ROW) + ") ,create Excel Fail");

		List<Integer> widthList = new ArrayList<>();
		List<String> headerList = new ArrayList<>();
		List<String> fieldList = new ArrayList<>();
		//获取每列宽度，表头，每列取那个属性值
		int colTotal = fillList(widthList, headerList, fieldList, typeClazz);//总列数
		// 判断总列数是不是超过了最大的列数限制
		if ((!isXlsx && colTotal > XLS_COL) || colTotal > XLSX_COL)
			throw new IllegalArgumentException(
					"TotalCol(" + colTotal + " > " + (isXlsx ? XLSX_COL : XLS_COL) + ") ,create Excel Fail");

		Workbook wb = null;
		try {
			wb = isXlsx ? new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE) : new HSSFWorkbook();
			Sheet sheet = wb.createSheet();
			for (int i = 0; i < widthList.size(); i++) {
				sheet.setColumnWidth(i, widthList.get(i) * 256);
			}
			int current = createTitle(wb, sheet, title, colTotal);// 创建标题
			current = createHeader(wb, sheet, headerList, current);// 创建列头
			for (int rowNum = 0; rowNum < body.size(); rowNum++) {
				setRowValue(wb, sheet, sheet.createRow(rowNum + current), body.get(rowNum), fieldList);
			}
			createTotal(wb, sheet, total, current + body.size(), colTotal);// 创建统计
			wb.write(outputStream);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			headerLocal.set(null);
			bodyLocal.set(null);
			totalLocal.set(null);
			if (wb != null)
				try {
					wb.close();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
		}
	}

	/**
	 * 从clazz获取注解，找到宽度、列名，以及对应的属性名
	 *
	 * @param widthList  宽度
	 * @param headerList 表头列名
	 * @param fieldList  属性名
	 * @param typeClazz  导出Excel的对象
	 * @return 列数
	 */
	private static <T> int fillList(List<Integer> widthList, List<String> headerList, List<String> fieldList, Class<T> typeClazz) {
		Map<Integer, CellInfo> map = new TreeMap<>();
		Field[] fields = typeClazz.getDeclaredFields();
		int count = 0;
		for (Field field : fields) {
			if (field.isAnnotationPresent(CellAttribute.class)) {
				count++;
				CellAttribute cellAttr = field.getAnnotation(CellAttribute.class);
				map.put(cellAttr.order(), new CellInfo(cellAttr.width(), cellAttr.name(), field.getName()));
			}
		}
		if (map.size() != count)
			throw new IllegalArgumentException("order repeat,create excel fail");
		for (Map.Entry<Integer, CellInfo> styleEntry : map.entrySet()) {
			CellInfo excelStyle = styleEntry.getValue();
			widthList.add(excelStyle.getWidth());
			headerList.add(excelStyle.getHeaderName());
			fieldList.add(excelStyle.getFieldName());
		}
		return count;
	}

	/**
	 * 创建标题，如果title字段为空，则不设置标题
	 *
	 * @param wb       工作簿
	 * @param sheet    表格
	 * @param title    标题
	 * @param colTotal 总列数
	 * @return 有标题返回1，否则返回0
	 */
	private static int createTitle(Workbook wb, Sheet sheet, String title, int colTotal) {
		if (title != null && !"".equals(title)) {
			mergedRegion(sheet, 0, 0, 0, colTotal - 1);
			Row row = sheet.createRow(0);
			row.setHeightInPoints(30);
			Cell cell = row.createCell(0);
			cell.setCellStyle(titleStyle(wb));
			cell.setCellValue(title);
			return 1;
		}
		return 0;
	}

	/**
	 * 创建表头
	 *
	 * @param wb      工作簿
	 * @param sheet   表格
	 * @param header  列头
	 * @param current 当前第几行
	 * @return 返回current+1
	 */
	private static int createHeader(Workbook wb, Sheet sheet, List<String> header, int current) {
		Row row = sheet.createRow(current);
		row.setHeightInPoints(30);
		for (int i = 0; i < header.size(); i++) {
			Cell cell = row.createCell(i);
			cell.setCellStyle(headerStyle(wb));
			cell.setCellValue(header.get(i));
		}
		return current + 1;
	}

	/**
	 * 设置表体中一行记录
	 *
	 * @param wb    工作簿
	 * @param sheet 表格
	 * @param row   行
	 * @param t     行数据
	 */
	private static <T> void setRowValue(Workbook wb, Sheet sheet, Row row, T t, List<String> fieldList) {
		for (int i = 0; i < fieldList.size(); i++) {
			Cell cell = row.createCell(i);
			cell.setCellStyle(bodyStyle(wb));

			Object val = BeanUtil.getValueByField(fieldList.get(i), t);
			convertValueToCell(val, cell);
		}
	}

	/**
	 * 设置一个单元格的值
	 *
	 * @param value 值
	 * @param cell  单元格
	 */
	private static void convertValueToCell(Object value, Cell cell) {
		if (value == null || "".equals(value)) {
			cell.setCellValue("");
		} else if (value instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			cell.setCellValue(sdf.format((Date) value));
		} else if (value instanceof Double) {
			Matcher matcher = Pattern.compile("^//d+(//.//d+)?$").matcher(value.toString());
			if (matcher.matches()) {
				// 是数字当作double处理
				cell.setCellValue(Double.parseDouble(value.toString()));
			} else {
				cell.setCellValue(value.toString());
			}
		} else {
			cell.setCellValue(value.toString());
		}
	}

	/**
	 * 创建总计数据，total为空则不创建
	 *
	 * @param wb       工作簿
	 * @param sheet    表格
	 * @param total    总计内容
	 * @param rowTotal 当前已有的行数
	 * @param colTotal 列数
	 */
	private static void createTotal(Workbook wb, Sheet sheet, List<String> total, int rowTotal, int colTotal) {
		if (total != null && total.size() > 0)
			for (int i = 0; i < total.size(); i++) {
				int index = rowTotal + i;
				Row row = sheet.createRow(index);
				mergedRegion(sheet, index, index, 0, colTotal - 1);
				Cell c = row.createCell(0);
				c.setCellStyle(totalStyle(wb));
				c.setCellValue(total.get(i));
			}
	}

	/**
	 * 合并单元格
	 *
	 * @param sheet    表格
	 * @param firstRow 起行
	 * @param lastRow  止行
	 * @param firstCol 起列
	 * @param lastCol  止列
	 */
	private static void mergedRegion(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
		sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
	}

	/**
	 * 获取标题的单元格样式
	 *
	 * @param wb 工作簿
	 * @return 单元格样式
	 */
	private static CellStyle titleStyle(Workbook wb) {
		return baseStyle(wb, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, false, true, 12, "宋体");
	}

	/**
	 * 获取表头的单元格样式
	 *
	 * @param wb 工作簿
	 * @return 单元格样式
	 */
	private static CellStyle headerStyle(Workbook wb) {
		CellStyle headerStyle = headerLocal.get();
		if (headerStyle == null) {
			headerStyle = baseStyle(wb, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false, false, 12, "宋体");
			headerLocal.set(headerStyle);
		}
		return headerStyle;
	}

	/**
	 * 获取表体的单元格样式
	 *
	 * @param wb 工作簿
	 * @return 单元格样式
	 */
	private static CellStyle bodyStyle(Workbook wb) {
		CellStyle bodyStyle = bodyLocal.get();
		if (bodyStyle == null) {
			bodyStyle = baseStyle(wb, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, false, false, 12, "宋体");
			bodyLocal.set(bodyStyle);
		}
		return bodyStyle;
	}

	/**
	 * 获取总计的单元格样式
	 *
	 * @param wb 工作簿
	 * @return 单元格样式
	 */
	private static CellStyle totalStyle(Workbook wb) {
		CellStyle totalStyle = totalLocal.get();
		if (totalStyle == null) {
			totalStyle = baseStyle(wb, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER, false, false, 12, "宋体");
			totalLocal.set(totalStyle);
		}
		return totalStyle;
	}

	/**
	 * 设置单元格格式
	 *
	 * @param wb         工作簿
	 * @param hAlign     水平对齐方式
	 * @param vAlign     垂直对齐方式
	 * @param wrapFlag   是否自动折行，true折行，false不折行
	 * @param boldFlag   是否加粗，true加粗，false不加粗
	 * @param fontHeight 字体大小
	 * @param fontName   字体名称
	 * @return 单元格样式
	 */
	private static CellStyle baseStyle(Workbook wb, HorizontalAlignment hAlign, VerticalAlignment vAlign,
									   boolean wrapFlag, boolean boldFlag, int fontHeight, String fontName) {
		CellStyle style = wb.createCellStyle();
		// 设置填充字体的样式
		style.setFillPattern(FillPatternType.NO_FILL);
		// 设置单元格居中对齐
		style.setAlignment(hAlign);// 水平
		style.setVerticalAlignment(vAlign);// 垂直
		style.setWrapText(wrapFlag);// 自动折行，true折行，false不折行
		// 设置单元格字体样式
		Font font = wb.createFont();
		font.setBold(boldFlag);// 字体加粗，true加粗，false不加粗
		font.setFontName(fontName);// 字体的名称
		// 将fontHeight乘以20以后再转换为short类型，缺省的字体大小是11
		short fontHeightShort = (short) ((fontHeight > (Short.MAX_VALUE / 20) ? 11 : fontHeight) * 20);
		font.setFontHeight(fontHeightShort);// 字体的大小
		style.setFont(font);// 将字体填充到表格中去

		return style;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private static class CellInfo {
		private int width;
		private String headerName;
		private String fieldName;
	}
}
