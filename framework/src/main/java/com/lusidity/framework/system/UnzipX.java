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

package com.lusidity.framework.system;

import com.lusidity.framework.text.StringX;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public
class UnzipX
{
	/**
	 * Size of the buffer to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;
// Methods
	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified by
	 * destDirectory (will be created if does not exists)
	 * @param zipFilePath
	 * @param destDirectory
	 * @throws IOException
	 */
	public static void unzip(String zipFilePath, String destDirectory) throws IOException
	{
		String targetDirectory =StringX.stripEnd(destDirectory, "\\");
		targetDirectory = StringX.stripEnd(targetDirectory, "/");

		File destDir = new File(targetDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			String filePath = String.format("%s%s%s", targetDirectory, File.separator, entry.getName());
			String parentPath = StringX.substring(filePath, 0, filePath.lastIndexOf(File.separator));
			File parent = new File(parentPath);

			if(!parent.exists()){
				parent.mkdirs();
			}

			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				UnzipX.extractFile(zipIn, filePath);
			}
			else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}
	/**
	 * Extracts a zip entry (file entry)
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[UnzipX.BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}
}
