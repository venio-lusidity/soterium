/*
 * Copyright 2018 lusidity inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.lusidity.office;

import com.lusidity.domains.system.io.FileInfo;
import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.json.JsonData;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("OverlyComplexClass")
public class ExcelX
{
	private XSSFCellStyle alternateStyle = null;
	private XSSFCellStyle genericStyle = null;

	// Constructors
	public ExcelX(){
		super();
	}

	// Methods
	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyNestedMethod",
		"OverlyLongMethod"
	})
	public void convertExcelToXml(String excelPath)
		throws ApplicationException
	{
		//FileInfo resultFile = null;
		String name="DPAS";
		int type=0;

		if (StringX.endsWithIgnoreCase(excelPath, "xsl"))
		{
			type=1;
		}
		else if (StringX.endsWithIgnoreCase(excelPath, "xlsx"))
		{
			type=2;
		}
		try
		{
			ArrayList<ArrayList<String>> data=this.getExcelData(type, excelPath);
			//Divide into smaller chunks to create multiple smaller xml files
			int factor=this.getFactor(data.size());

			int arrayCount=0;
			int rowCount=0;
			for (int start=0; start<data.size(); start+=factor)
			{
				arrayCount++;
				String fileName=name+"_"+arrayCount+".xml";
				int end=Math.min(start+factor, data.size());
				List<ArrayList<String>> subData=data.subList(start, end);
				// Initializing the XML document
				DocumentBuilderFactory factory=DocumentBuilderFactory
					.newInstance();
				DocumentBuilder builder=factory.newDocumentBuilder();
				Document document=builder.newDocument();
				Element productElement=null;
				Element rootElement=document.createElement("DpasData");
				document.appendChild(rootElement);

				for (ArrayList<String> row : subData)
				{
					int index=0;
					rowCount++;
					if (arrayCount==1)
					{
						if (rowCount!=1)
						{
							productElement=document.createElement("InventoryItem");
							rootElement.appendChild(productElement);
						}
						index=0;
						for (String s : row)
						{
							if (rowCount!=1)
							{
								String rawString=StringX.removeNonAlphaNumericIgnoreCase(data.get(0).get(index));
								String headerString=StringX.replace(rawString, " ", "_");
								Element headerElement=document
									.createElement(headerString);
								productElement.appendChild(headerElement);
								headerElement.appendChild(document.createTextNode(s));
							}
							index++;
						}
					}
					else
					{
						productElement=document.createElement("InventoryItem");
						rootElement.appendChild(productElement);
						for (String s : row)
						{

							String rawString=StringX.removeNonAlphaNumericIgnoreCase(data.get(0).get(index));
							String headerString=StringX.replace(rawString, " ", "_");
							Element headerElement=document
								.createElement(headerString);
							productElement.appendChild(headerElement);
							headerElement.appendChild(document.createTextNode(s));

							index++;
						}
					}


				}
				TransformerFactory tFactory=TransformerFactory.newInstance();
				Transformer transformer=tFactory.newTransformer();
				// Add indentation to WriteConsoleJob
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");

				DOMSource source=new DOMSource(document);
				File sourceFile=new File(excelPath);
				String absPath=FilenameUtils.getFullPath(sourceFile.getAbsolutePath());
				StreamResult result=new StreamResult(new File(absPath+fileName));
				transformer.transform(source, result);
			}

		}
		catch (Exception e)
		{
			throw new ApplicationException(e);
		}

	}

	@SuppressWarnings({
		"UnusedAssignment",
		"OverlyComplexMethod"
		,
		"OverlyLongMethod"
		,
		"OverlyNestedMethod"
	})
	private ArrayList<ArrayList<String>> getExcelData(int type, String excelPath)
		throws ApplicationException
	{
		ArrayList<ArrayList<String>> ExcelData=new ArrayList<>();
		int sheetCount=0;
		int rowCount=0;
		int rowCellCount=0;
		Workbook workBook;

		switch (type)
		{
			case 1:
				try
				{
					NPOIFSFileSystem fs=new NPOIFSFileSystem(new File(excelPath));
					workBook=new HSSFWorkbook(fs.getRoot(), true);
					sheetCount=workBook.getNumberOfSheets();
					for (int i=0; i<sheetCount; i++)
					{
						Sheet sheet=workBook.getSheetAt(i);
						Iterator<?> rows=sheet.rowIterator();
						while (rows.hasNext())
						{
							rowCount++;
							Row row=(Row) rows.next();
							Iterator<?> cells=row.cellIterator();
							ArrayList<String> rowData=new ArrayList<>();
							String contents=" ";
							if (rowCount==1)
							{
								while (cells.hasNext())
								{
									rowCellCount++;
									Cell cell=(Cell) cells.next();
									switch (cell.getCellType())
									{
										case Cell.CELL_TYPE_STRING:
											contents=cell.getRichStringCellValue().getString();
											break;
										case Cell.CELL_TYPE_NUMERIC:
											if (DateUtil.isCellDateFormatted(cell))
											{
												contents=cell.getDateCellValue().toString();
											}
											else
											{
												contents=String.valueOf(cell.getNumericCellValue());
											}
											break;
										case Cell.CELL_TYPE_BOOLEAN:
											contents=String.valueOf(cell.getBooleanCellValue());
											break;
										case Cell.CELL_TYPE_FORMULA:
											contents=cell.getCellFormula();
											break;
										case Cell.CELL_TYPE_BLANK:
											contents="";
											break;
										default:
											break;
									}

									rowData.add(contents);
								}
							}
							else
							{
								for (int c=0; c<rowCellCount; c++)
								{
									Cell dataCell=row.getCell(c);
									if (dataCell!=null)
									{
										switch (dataCell.getCellType())
										{
											case Cell.CELL_TYPE_STRING:
												contents=dataCell.getRichStringCellValue().getString();
												break;
											case Cell.CELL_TYPE_NUMERIC:
												if (DateUtil.isCellDateFormatted(dataCell))
												{
													contents=dataCell.getDateCellValue().toString();
												}
												else
												{
													contents=String.valueOf(dataCell.getNumericCellValue());
												}
												break;
											case Cell.CELL_TYPE_BOOLEAN:
												contents=String.valueOf(dataCell.getBooleanCellValue());
												break;
											case Cell.CELL_TYPE_FORMULA:
												contents=dataCell.getCellFormula();
												break;
											case Cell.CELL_TYPE_BLANK:
												contents="";
												break;
											default:
												break;
										}
									}
									else
									{
										contents=" ";
									}
									rowData.add(contents);
								}
							}

							ExcelData.add(rowData);
						}
						fs.close();
					}
					break;
				}
				catch (Exception e)
				{
					throw new ApplicationException(e);
				}

			case 2:
				try
				{
					OPCPackage pkg=OPCPackage.open(new File(excelPath));
					workBook=new XSSFWorkbook(pkg);
					sheetCount=workBook.getNumberOfSheets();
					for (int i=0; i<sheetCount; i++)
					{
						Sheet sheet=workBook.getSheetAt(i);
						Iterator<?> rows=sheet.rowIterator();
						while (rows.hasNext())
						{
							rowCount++;
							Row row=(Row) rows.next();
							Iterator<?> cells=row.cellIterator();
							ArrayList<String> rowData=new ArrayList<>();
							String contents=" ";
							if (rowCount==1)
							{
								while (cells.hasNext())
								{
									rowCellCount++;
									Cell cell=(Cell) cells.next();
									switch (cell.getCellType())
									{
										case Cell.CELL_TYPE_STRING:
											contents=cell.getRichStringCellValue().getString();
											break;
										case Cell.CELL_TYPE_NUMERIC:
											if (DateUtil.isCellDateFormatted(cell))
											{
												contents=cell.getDateCellValue().toString();
											}
											else
											{
												contents=String.valueOf(cell.getNumericCellValue());
											}
											break;
										case Cell.CELL_TYPE_BOOLEAN:
											contents=String.valueOf(cell.getBooleanCellValue());
											break;
										case Cell.CELL_TYPE_FORMULA:
											contents=cell.getCellFormula();
											break;
										default:
											break;
									}

									rowData.add(contents);
								}
							}
							else
							{
								for (int c=0; c<rowCellCount; c++)
								{
									Cell dataCell=row.getCell(c);
									if (dataCell!=null)
									{
										switch (dataCell.getCellType())
										{
											case Cell.CELL_TYPE_STRING:
												contents=dataCell.getRichStringCellValue().getString();
												break;
											case Cell.CELL_TYPE_NUMERIC:
												if (DateUtil.isCellDateFormatted(dataCell))
												{
													contents=dataCell.getDateCellValue().toString();
												}
												else
												{
													contents=String.valueOf(dataCell.getNumericCellValue());
												}
												break;
											case Cell.CELL_TYPE_BOOLEAN:
												contents=String.valueOf(dataCell.getBooleanCellValue());
												break;
											case Cell.CELL_TYPE_FORMULA:
												contents=dataCell.getCellFormula();
												break;
											case Cell.CELL_TYPE_BLANK:
												contents="";
												break;
											default:
												break;
										}
									}
									else
									{
										contents=" ";
									}
									rowData.add(contents);
								}
							}

							ExcelData.add(rowData);
						}
					}
					pkg.close();
					break;

				}
				catch (Exception e)
				{
					throw new ApplicationException(e);
				}
			case 0:
				break;

		}
		return ExcelData;
	}

	private int getFactor(int size)
	{
		int f=0;
		if (size<100)
		{
			f=size;
		}
		else if ((size>100) && (size<1000))
		{
			f=100;
		}
		else if ((size>1000) && (size<10000))
		{
			f=500;
		}
		else if (size>10000)
		{
			f=1000;
		}

		return f;
	}

	public FileInfo fromJsonData(String fileName, File outputFolder, JsonData schema, JsonData items, String className, boolean styleIt)
		throws IOException, ApplicationException
	{
		FileInfo result=null;

		if (!outputFolder.exists())
		{
			throw new IOException(String.format("The directory, %s, does not exist.", outputFolder.getAbsolutePath()));
		}

		if ((null==schema) || !schema.isJSONObject() || schema.isEmpty())
		{
			throw new InvalidParameterException("The schema object cannot be null or empty.");
		}

		if (items.isJSONArray())
		{
			try
			{
				String fName=StringX.stripEnd(fileName, ".xls");
				fName=StringX.stripEnd(fName, ".xlsx");
				File file=new File(outputFolder, String.format("%s.xlsx", fName));
				Workbook workbook=new XSSFWorkbook();
				Sheet sheet=workbook.createSheet(String.format("%s-%s", "Total", items.length()));

				Row rowHead=sheet.createRow(3);
				int c =this.createHeaderRow(workbook, sheet, rowHead, schema, null, 0);
				this.createBannerRow(workbook, sheet, fileName, 0, c);
				this.createDateRow(workbook, sheet, fileName, 1, c);
				this.createTitleRow(workbook, sheet, fileName, 2, c);
				if(items.isEmpty() || (items.length()<1)){
					this.createEmptyDataRow(sheet, 4, className);
				}
				else{
					this.createValueRows(workbook, sheet, 4, items, schema, styleIt);
				}
				//adjust column width to fit the content
				int maxCells= sheet.getRow(0).getLastCellNum();
				for (int cellCounter=0
					; cellCounter<maxCells
					; cellCounter++)
				{ // Loop through columns and autosize
					sheet.autoSizeColumn(cellCounter);
				}
				try (FileOutputStream out=new FileOutputStream(file))
				{
					workbook.write(out);
					result=new FileInfo();
					result.fetchTitle().setValue(fileName);
					result.fetchExtension().setValue(".xlsx");
					result.fetchPath().setValue(file.getAbsolutePath());
				}
				catch (Exception ex)
				{
					ReportHandler.getInstance().severe(ex);
				}

			}
			catch (Exception ex)
			{
				ReportHandler.getInstance().severe(ex);
			}
		}
		else
		{
			throw new ApplicationException("The JsonData object must be an array of Json objects.");
		}

		return result;
	}

	@SuppressWarnings({
		"OverlyComplexMethod",
		"OverlyNestedMethod"
	})
	private int createHeaderRow(Workbook workbook, Sheet sheet, Row rowHead, JsonData schema, String prefix, int on)
		throws ApplicationException
	{
		int result=on;
		if ((null!=schema) && schema.isJSONObject())
		{
			for (String key : schema.keys())
			{
				try
				{
					JsonData item=schema.getFromPath(key);
					if (item.hasKey("schema"))
					{
						String p=item.getString("s_label");
						result=this.createHeaderRow(workbook, sheet, rowHead, item.getFromPath("schema"), p, result);
					}
					else if (item.hasKey("s_label"))
					{
						String v=item.getString("s_label");
						if (!StringX.isBlank(prefix))
						{
							v=String.format("%s \n %s", prefix, v);
							float height=2*sheet.getDefaultRowHeightInPoints();
							if (rowHead.getHeightInPoints()<height)
							{
								rowHead.setHeightInPoints((height));
							}
						}
						Integer idx=item.getInteger("idx");
						if (null==idx)
						{
							idx=result;
							item.put("idx", result);
						}
						Cell cell=rowHead.createCell(idx);
						XSSFCellStyle cs=this.getHeaderStyle(workbook);
						cell.setCellStyle(cs);
						cell.setCellValue(v);
						result++;
					}
					else if (!item.keys().isEmpty())
					{
						result=this.createHeaderRow(workbook, sheet, rowHead, item, prefix, result);
					}
					else
					{
						throw new ApplicationException("Invalid schema object @ %s.", key);
					}
				}
				catch(Exception ex){
					if(null!=ReportHandler.getInstance()){
						ReportHandler.getInstance().severe(ex);
					}
				}
			}
		}
		return result;
	}
	private void createTitleRow(Workbook workbook, Sheet sheet, String fileName, int rowNumber, int cells)
	{
		try
		{
			XSSFCellStyle cs=this.getTitleStyle(workbook);
			Row row=sheet.createRow(rowNumber);
			String title=StringX.substring(fileName, 0, fileName.lastIndexOf('-'));
			Cell firstCell=row.createCell(0);
			firstCell.setCellStyle(cs);
			firstCell.setCellValue("Report For: " + StringX.replace(title, "_", " "));
			for (int cellCounter=0
				; cellCounter<cells
				; cellCounter++)
			{ // Loop through cells
				if (row.getCell(cellCounter)==null)
				{
					row.createCell(cellCounter).setCellStyle(cs);
				}
			}
			sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber,0, cells-1));
		}
		catch (Exception ex)
		{
			if (null!=ReportHandler.getInstance())
			{
				ReportHandler.getInstance().severe(ex);
			}
		}
	}
	private void createBannerRow(Workbook workbook, Sheet sheet, String fileName, int rowNumber, int cells)
	{
		try
		{
			XSSFCellStyle cs=this.getBannerStyle(workbook);
			Row row=sheet.createRow(rowNumber);
			Cell firstCell=row.createCell(0);
			firstCell.setCellStyle(cs);
			firstCell.setCellValue("UNCLASSIFIED//FOUO");
			for (int cellCounter=0
				; cellCounter< cells
				; cellCounter++)
			{ // Loop through cells
				if (row.getCell(cellCounter)==null)
				{
					row.createCell(cellCounter).setCellStyle(cs);
				}
			}
			sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber,0, cells-1));
		}
		catch (Exception ex)
		{
			if (null!=ReportHandler.getInstance())
			{
				ReportHandler.getInstance().severe(ex);
			}
		}
	}
	private void createDateRow(Workbook workbook, Sheet sheet, String fileName, int rowNumber, int cells)
	{
		try
		{
			XSSFCellStyle cs=this.getTitleStyle(workbook);
			XSSFFont font=cs.getFont();
			font.setFontHeight(14);
			Row row=sheet.createRow(rowNumber);
			DateTime dt = DateTime.now();
			DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy kk:mm");
			String formatted = dt.toString(dtf);
			Cell firstCell=row.createCell(0);
			firstCell.setCellStyle(cs);
			firstCell.setCellValue("Created: " +formatted);
			for (int cellCounter=0
				; cellCounter< cells
				; cellCounter++)
			{ // Loop through cells
				if (row.getCell(cellCounter)==null)
				{
					row.createCell(cellCounter).setCellStyle(cs);
				}
			}
			sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber,0, cells-1));
		}
		catch (Exception ex)
		{
			if (null!=ReportHandler.getInstance())
			{
				ReportHandler.getInstance().severe(ex);
			}
		}
	}
	private XSSFCellStyle getAlternateStyle(Workbook workbook)
	{
		if(null == this.alternateStyle)
		{
			this.alternateStyle=(XSSFCellStyle) workbook.createCellStyle();
			//rgb value for light-gray f5f5f5
			XSSFColor myColor=new XSSFColor(new java.awt.Color(245, 245, 245));
			this.alternateStyle.setFillForegroundColor(myColor);
			this.alternateStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			this.alternateStyle.setBorderBottom(BorderStyle.THIN);
			this.alternateStyle.setBorderTop(BorderStyle.THIN);
			this.alternateStyle.setBorderLeft(BorderStyle.THIN);
			this.alternateStyle.setBorderLeft(BorderStyle.THIN);
			this.alternateStyle.setWrapText(true);
		}
		return this.alternateStyle;
	}
	private XSSFCellStyle getGenericStyle(Workbook workbook)
	{
		if(null == this.genericStyle)
		{
			this.genericStyle =(XSSFCellStyle) workbook.createCellStyle();
			this.genericStyle.setBorderBottom(BorderStyle.THIN);
			this.genericStyle.setBorderTop(BorderStyle.THIN);
			this.genericStyle.setBorderLeft(BorderStyle.THIN);
			this.genericStyle.setBorderLeft(BorderStyle.THIN);
			this.genericStyle.setWrapText(true);
		}
		return this.genericStyle;
	}
	private XSSFCellStyle getHeaderStyle(Workbook workbook)
	{
		XSSFCellStyle style=(XSSFCellStyle) workbook.createCellStyle();
		style.setWrapText(true);
		//light-blue e6ffff
		XSSFColor myColor=new XSSFColor(new java.awt.Color(230, 242, 255));
		style.setFillForegroundColor(myColor);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		XSSFFont font=(XSSFFont) workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setWrapText(true);
		return style;
	}
	private XSSFCellStyle getTitleStyle(Workbook workbook)
	{
		XSSFCellStyle style=(XSSFCellStyle) workbook.createCellStyle();
		style.setWrapText(true);
		//light-blue e6ffff
		XSSFColor myColor=new XSSFColor(new java.awt.Color(230, 242, 255));
		style.setFillForegroundColor(myColor);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		XSSFFont font=(XSSFFont) workbook.createFont();
		font.setBold(true);
		font.setFontHeight(18);
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setWrapText(true);
		return style;
	}
	private XSSFCellStyle getBannerStyle(Workbook workbook)
	{
		XSSFCellStyle style=(XSSFCellStyle) workbook.createCellStyle();
		style.setWrapText(true);
		//green
		XSSFColor myColor=new XSSFColor(new java.awt.Color(0, 153, 0));
		style.setFillForegroundColor(myColor);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		XSSFFont font=(XSSFFont) workbook.createFont();
		font.setBold(true);
		font.setFontHeight(16);
		font.setColor(IndexedColors.WHITE.getIndex());
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setWrapText(true);
		return style;
	}
	private int createValueRows(Workbook workbook, Sheet sheet, int rowNumber, JsonData items, JsonData schema, boolean styleIt)
		throws ApplicationException
	{
		int result=rowNumber;
		int count=0;
		for (Object o : items)
		{
			count++;
			if (o instanceof JSONObject)
			{
				boolean shade=(count%2)!=0;
				JsonData item=new JsonData(o);
				Row row=sheet.createRow(result);
				XSSFCellStyle style=null;
				if (shade)
				{
					style=this.getAlternateStyle(workbook);
				}
				else
				{
					style=this.getGenericStyle(workbook);
				}

				for (String key : schema.keys())
				{
					this.createValue(schema, item, row, key, styleIt ? style : null);
				}

				// if the style is being applied before hand why is it applied again here?
				Row firstRow=sheet.getRow(0);
				int maxCells=firstRow.getLastCellNum();
				for (int cellCounter=0; cellCounter<maxCells; cellCounter++)
				{
					// Loop through cells
					if (row.getCell(cellCounter)==null)
					{
						Cell rowCell=row.createCell(cellCounter);
						if(styleIt)
						{
							rowCell.setCellStyle(style);
						}
					}
				}
				result++;
			}
		}
		return result;
	}

	private void createEmptyDataRow(Sheet sheet, int rowNumber, String message)
		throws ApplicationException
	{
		String richText = "No data returned for this " + message;
		Row emptyRow=sheet.createRow(rowNumber);
		Row firstRow=sheet.getRow(0);
		Cell cell = emptyRow.createCell(0);
		if(StringX.isBlank(message)) {
			richText = "No data returned for this vertex type";
		}
		cell.setCellValue(new XSSFRichTextString(richText));
		int maxCells=firstRow.getLastCellNum();
		for (int cellCounter=0
			; cellCounter<maxCells
			; cellCounter++)
		{ // Loop through cells
			if (emptyRow.getCell(cellCounter)==null)
			{
				emptyRow.createCell(cellCounter);
			}
		}
		sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber,0, maxCells));
	}
	private void createValue(JsonData schema, JsonData item, Row row, String key, CellStyle style)
	{
		try
		{
			Object value=item.getObjectFromPath(key);
			JsonData itemSchema=schema.getFromPath(key);

			if((value instanceof JSONArray) || (value instanceof JSONObject)){
				value = JsonData.create(value);
			}

			if ((value instanceof JsonData) && itemSchema.hasKey("schema"))
			{
				JsonData data=(JsonData) value;
				JsonData values=JsonData.createArray();
				JsonData subSchema=itemSchema.getFromPath("schema");
				if (data.isJSONArray())
				{
					values=data;
				}
				else
				{
					values.put(data);
				}
				this.handleSchemaCells(subSchema, values, row, style);
			}
			else
			{
				if (null!=value)
				{
					int idx=itemSchema.getInteger("idx");
					Cell cell=row.createCell(idx);
					cell.setCellValue(value.toString());
					if (null!=style)
					{
						cell.setCellStyle(style);
					}
				}
			}
		}
		catch (Exception ex){
			ReportHandler.getInstance().warning(ex);
		}
	}

	@SuppressWarnings("OverlyNestedMethod")
	private void handleSchemaCells(JsonData subSchema, JsonData items, Row row, CellStyle style)
	{
		for (Object o : items)
		{
			if (o instanceof JSONObject)
			{
				JsonData item=JsonData.create(o);
				for (String key : subSchema.keys())
				{
					Object value=item.getObjectFromPath(key);
					JsonData itemSchema=subSchema.getFromPath(key);

					if (null!=value)
					{
						int idx=itemSchema.getInteger("idx");
						Cell cell = row.getCell(idx);
						if(null != cell){
							StringBuffer sb = new StringBuffer();
							if(!StringX.isBlank(cell.getStringCellValue())){
								String currentValue = cell.getStringCellValue();
								sb.append(currentValue).append("\n");
							}
							String v = value.toString();
							sb.append(v);
							cell.setCellValue(sb.toString());

						}
						else{
							cell=row.createCell(idx);
							cell.setCellValue(value.toString());
						}
						if (null!=style)
						{
							cell.setCellStyle(style);
						}
					}
				}
			}
		}
	}
}
