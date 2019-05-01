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

package com.lusidity.framework.json;

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.text.StringX;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertyExtractor {
    private File file;
    public PropertyExtractor(File file){
        super();
        this.file = file;
    }

    public JsonData getProperties() throws ApplicationException {
        JsonData result = null;
        if(StringX.endsWithIgnoreCase(this.file.getAbsolutePath(), "csv")){
            result = this.handleCSV();
        }
        return result;
    }

    private JsonData handleCSV() throws ApplicationException {
        JsonData result = JsonData.createObject();
        JsonData items = JsonData.createArray();

        Properties properties = new Properties();
        try(FileInputStream fis = new FileInputStream(this.file)) {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(fis))){
                String line = br.readLine();
                if(!StringX.isBlank(line)){
                    items = this.parseCSVProperties(line);
                }
            }
            catch (Exception ex){
                throw new ApplicationException(ex);
            }
        }
        catch (Exception ex){
            throw new ApplicationException(ex);
        }

        result.put("properties", items);

        return result;
    }

    private JsonData parseCSVProperties(String line) {
        JsonData results = JsonData.createArray();
        String[] keys = StringX.split(line, ",");
        if(null!=keys && keys.length>0){
            for(String key: keys){
                results.put(key);
            }
        }
        return results;
    }
}
