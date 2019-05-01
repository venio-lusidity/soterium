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

package com.lusidity.framework.internet.http;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UtilsX
{
	// Methods
	public static String getContentType(File file) throws IOException {
		return Files.probeContentType(file.toPath());
	}

    public static String getContentType(Path path) throws IOException {
        return Files.probeContentType(path);
    }

    public static String getFileExtension(File file){
        return FilenameUtils.getExtension(file.getName());
    }

    public static String getParentDirectory() {
        return Paths.get("..").toAbsolutePath().normalize().toString();
    }
}
