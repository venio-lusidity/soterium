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

package com.lusidity.framework.office;

import com.lusidity.framework.exceptions.ApplicationException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SuppressWarnings("BooleanParameter")
public class ExcelReader implements Closeable {

    private final File file;
    private POIFSFileSystem fs = null;
    private int rows = 0;
    private int cols = 0;

    public ExcelReader(File file) throws ApplicationException {
        super();
        this.file = file;
        this.load();
    }

    private void load() throws ApplicationException {
        try(FileInputStream fis = new FileInputStream(this.file)){
            this.fs = new POIFSFileSystem(fis);
        }
        catch (Exception ex){
            throw new ApplicationException(ex);
        }
    }

    /**
     * Read an xls file line by line.
     * @param lineHandler The handler to pass a newly read row to.
     * @param cellHandler Can be null, but is the handler to pass a newly read cell to.
     * @param sheetNumber What sheet number to read from. Null equals all sheets.
     * @param process If not process count the rows.
     */
    public void read(ExcelLineHandler lineHandler, ExcelCellHandler cellHandler, Integer sheetNumber, boolean process) {
        try(HSSFWorkbook wb = new HSSFWorkbook(this.fs)) {             ;
            if (null == sheetNumber) {
                int x = wb.getNumberOfSheets();
                for (int i = 0; i < x; i++) {
                    HSSFSheet sheet = wb.getSheetAt(i);
                    if (null != sheet) {
                        this.handleSheet(lineHandler, cellHandler, sheet, process);
                    }
                }
            } else {
                HSSFSheet sheet = wb.getSheetAt(sheetNumber);
                if (null != sheet) {
                    this.handleSheet(lineHandler, cellHandler, sheet, process);
                }
            }

        } catch (Exception ignored) {}
    }

    @SuppressWarnings({
        "OverlyComplexMethod",
        "OverlyNestedMethod"
    })
    private void handleSheet(ExcelLineHandler lineHandler, ExcelCellHandler cellHandler, HSSFSheet sheet, boolean process) {
        try {

            this.rows = sheet.getPhysicalNumberOfRows();
            if(process)
            {
                int tmp=0;

                // This trick ensures that we get the data properly even if it doesn't start from first few rows
                HSSFRow row;
                for (int i=0; (i<10) || (i<this.rows); i++)
                {
                    row=sheet.getRow(i);
                    if (row!=null)
                    {
                        tmp=sheet.getRow(i).getPhysicalNumberOfCells();
                        if (tmp>this.cols){ this.cols=tmp;}
                    }
                }

                for (int r=0; r<this.rows; r++)
                {
                    row=sheet.getRow(r);
                    if (row!=null)
                    {
                        lineHandler.handle(r, row, this.rows, this.cols);
                        if (null!=cellHandler)
                        {
                            for (int c=0; c<this.cols; c++)
                            {
                                HSSFCell cell=row.getCell(c);
                                if (cell!=null)
                                {
                                    cellHandler.handle(r, row, c, cell);
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception ignored) {}
    }

    public int getRows(){
        return this.rows;
    }

    @Override
    public void close() throws IOException {
        if(null!=this.fs){
            this.fs = null;
        }
    }
}
