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

import java.io.File;
import java.io.FileFilter;

public class FileFilterX implements FileFilter {

    private final String[] extensions;
    private boolean ignoreCase = true;

    /**
     * Create a FileFilter
     * @param extensions acceptable extensions
     * @param ignoreCase If true use case insensitivity.
     */
    public FileFilterX(boolean ignoreCase, String... extensions){
        super();
        this.extensions = extensions;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean accept(File pathname) {
        boolean result = false;
        String ext = StringX.getLast(pathname.getName(), ".");
        if(!StringX.isBlank(ext)){
            if(this.ignoreCase){
                ext = ext.toLowerCase();
            }
            for(String extension: this.extensions){
                result = this.ignoreCase? StringX.equalsIgnoreCase(ext, extension) :
                        StringX.equals(ext, extension);
                if(result){
                    break;
                }
            }
        }
        return result;
    }
}
