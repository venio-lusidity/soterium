/*
 * Copyright (c) 2008-2012, Venio, Inc.
 * All Rights Reserved Worldwide.
 *
 * This computer software is protected by copyright law and international treaties.
 * It may not be duplicated, reproduced, distributed, compiled, executed,
 * reverse-engineered, or used in any other way, in whole or in part, without the
 * express written consent of Venio, Inc.
 *
 * Portions of this computer software also embody trade secrets, patents, and other
 * protected intellectual property of Venio, Inc. and third parties and are subject to
 * applicable laws, regulations, treaties, agreements, and other legal mechanisms.
 */

package com.lusidity.framework.system;

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.reports.ReportHandler;
import com.lusidity.framework.text.StringX;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * File and filesystem utilities.
 */
public class FileX {
    /**
     * Private default constructor. This is a utility class and should not be instantiated.
     */
    private FileX() {
        super();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteRecursively(File file) {
        if ((null!=file) && file.isDirectory()) {
            try
            {
                FileUtils.cleanDirectory(file);

                @SuppressWarnings("ConstantConditions")
                int fileLength = (null!=file.listFiles()) ? file.listFiles().length : 0;

                if(fileLength>0)
                {
                    //  Recursively delete contents of a directory
                    String[] files=file.list();
                    if(null!=files)
                    {
                        for (String temp : files)
                        {
                            File fileDelete=new File(file, temp);
                            FileX.deleteRecursively(fileDelete);
                        }
                    }
                }

                fileLength = (null!=file.listFiles()) ? file.listFiles().length : 0;

                //  Directory is now empty and can be deleted
                //noinspection ConstantConditions
                if (fileLength==0)
                {
                    file.delete();
                }
            }
            catch (Exception ignored){}
        } else {
            if(null!=file)
            {
                // Delete a file
                file.delete();
            }
        }
    }

    public static File getFile(String pathAndFilename)
    {
        return new File(pathAndFilename);
    }

    public static void write(File file, String content)
    {
	    if(!file.getParentFile().exists()){
		    file.getParentFile().mkdirs();
	    }
        try {
	        FileUtils.writeStringToFile(file, content);
        }
        catch (Exception ex){
        	if(null!=ReportHandler.getInstance()){
        		ReportHandler.getInstance().warning(ex);
	        }
        }
    }

    public static synchronized void appendLine(File file, String content)
    {
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        if(!file.exists()){
             FileX.write(file, content);
        }
        else
        {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(file, true)))
            {
                writer.newLine();
                writer.write(content);
            }
            catch (Exception ex)
            {
                if (null!=ReportHandler.getInstance())
                {
                    ReportHandler.getInstance().warning(ex);
                }
            }
        }
    }

    public static void readLines(File file, LineHandler lineHandler) throws ApplicationException
    {
        try (FileInputStream fis=new FileInputStream(file))
        {
            try (BufferedReader br=new BufferedReader(new InputStreamReader(fis, "UTF-8")))
            {
                String line;
                while ((line=br.readLine())!=null)
                {
                    boolean exit=lineHandler.handle(line);
                    lineHandler.incrementLinesRead();
                    if (exit)
                    {
                        break;
                    }
                }
            }
            catch (Exception ex)
            {
                //noinspection ThrowCaughtLocally
                throw new ApplicationException(ex);
            }
        }
        catch (Exception ex)
        {
            throw new ApplicationException(ex);
        }
    }

    public static int lineCount(File file) {
        int result = 0;
        try(FileInputStream fis = new FileInputStream(file)) {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))){
                while (br.readLine()!=null){
                    result++;
                }
            }
            catch (Exception ignored){}
        }
        catch (Exception ignored){}

        return result;
    }

    public static FileFilter filter(boolean ignoreCase, String... extensions) {
        return new FileFilterX(ignoreCase, extensions);
    }

    /**
     * Strips all empty lines from the file that it is reading.
     * @param file The file to read.
     * @return The contents of this file ignores empty lines in the result of the string value.
     * In other words it all gets mashed together.
     * @throws ApplicationException
     */
    public static String getString(File file) throws ApplicationException {
        StringBuffer sb = new StringBuffer();
        try(FileInputStream fis = new FileInputStream(file)) {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))){
                String line;
                while ((line= br.readLine())!=null){
                    sb.append(line);
                }
            }
            catch (Exception ex){
                throw new ApplicationException(ex);
            }
        }
        catch (Exception ex){
            throw new ApplicationException(ex);
        }
        return (sb.length()==0) ? "" : sb.toString();
    }

    public static File getNewestFile(String filePath, String ext) {
        File result = null;
        File dir = new File(filePath);
        FileFilter fileFilter = new WildcardFileFilter("*."+ext);
        File[] files = dir.listFiles(fileFilter);

        if ((null!=files) && (files.length>0)) {
            /** The newest file comes first **/
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            result = files[0];
        }

        return result;
    }

    public static File getNewestFile(String filePath) {
        File result = null;
        File dir = new File(filePath);
        File[] files = dir.listFiles();
        if (files.length > 0) {
            /** The newest file comes first **/
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            result = files[0];
        }
        return result;
    }

    public static String getWebUrl(File file, String baseUrl, String... relativePaths)
    {
        String result = null;
        if(file.exists() && !StringX.isBlank(baseUrl)){
            StringBuffer sb = new StringBuffer();
            sb.append(StringX.stripEnd(baseUrl, "/"));
            if(null!=relativePaths){
                for(String path: relativePaths){
                    if(!StringX.isBlank(path)){
                        sb.append("/").append(StringX.stripEnd(StringX.stripStart(path, "/"), "/"));
                    }
                }
            }
            sb.append("/").append(file.getName());
            result = sb.toString();
        }
        return result;
    }

    public static String getRelativeWebUrl(File file, String... relativePaths)
    {
        String result = null;
        if(file.exists()){
            StringBuffer sb = new StringBuffer();
            if(null!=relativePaths){
                for(String path: relativePaths){
                    if(!StringX.isBlank(path)){
                        sb.append("/").append(StringX.stripEnd(StringX.stripStart(path, "/"), "/"));
                    }
                }
            }
            sb.append("/").append(file.getName());
            result = sb.toString();
        }
        return result;
    }


    /**
     * Retrieves a text file preserving any format.
     * @param file The text file to read.
     * @return The text document content as a string.
     * @throws IOException
     */
    public static String getAsPlainText(File file)
        throws IOException
    {
        byte[] bytes =Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        return new String(bytes, "UTF-8");
    }
}
