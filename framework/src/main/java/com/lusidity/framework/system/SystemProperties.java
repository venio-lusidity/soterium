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

import java.util.HashMap;
import java.util.LinkedHashMap;

public class SystemProperties {
    
    private static final HashMap<String, String> PROPERTIES = SystemProperties.getPROPERTIES();

    private SystemProperties() {
        super();
    }

    public enum Keys{
        java_version,
        java_vendor,
        java_vendor_url,
        java_home,
        java_vm_specification_version,
        java_vm_specification_vendor,
        java_vm_specification_name,
        java_vm_version,
        java_vm_vendor,
        java_vm_name,
        java_specification_version,
        java_specification_vendor,
        java_specification_name,
        java_class_version,
        java_class_path,
        java_library_path,
        java_io_tmpdir,
        java_compiler,
        java_ext_dirs,
        os_name,
        os_arch,
        os_version,
        file_separator,
        path_separator,
        line_separator,
        user_name,
        user_home,
        user_dir
    }

    private static HashMap<String, String> getPROPERTIES() {
        HashMap<String, String> results = new HashMap<String, String>();
        results.put("java.version","JRE version");
        results.put("java.vendor","JRE vendor");
        results.put("java.vendor.url","Java vendor URL");
        results.put("java.home","Java installation directory");
        results.put("java.vm.specification.version","JVM specification version");
        results.put("java.vm.specification.vendor","JVM specification vendor");
        results.put("java.vm.specification.name","JVM specification name");
        results.put("java.vm.version","JVM implementation version");
        results.put("java.vm.vendor","JVM implementation vendor");
        results.put("java.vm.name","JVM implementation name");
        results.put("java.specification.version","JRE specification version");
        results.put("java.specification.vendor","JRE specification vendor");
        results.put("java.specification.name","JRE specification name");
        results.put("java.class.version","Java class format version number");
        results.put("java.class.path","Java class path");
        results.put("java.library.path","List of paths to search when loading libraries");
        results.put("java.io.tmpdir","Default temp file path");
        results.put("java.compiler","Name of JIT compiler to use");
        results.put("java.ext.dirs","Path of extension directory or directories");
        results.put("os.name","Operating system name");
        results.put("os.arch","Operating system architecture");
        results.put("os.version","Operating system version");
        results.put("file.separator","File separator ('/' on UNIX)");
        results.put("path.separator","Path separator (':' on UNIX)");
        results.put("line.separator","Line separator ('\\\n' on UNIX)");
        results.put("user.name","User's account name");
        results.put("user.home","User's home directory");
        results.put("user.dir","User's current working directory");

        return results;
    }
    
    public static String getLabel(Keys key)
    {
        return SystemProperties.PROPERTIES.get(SystemProperties.getKey(key));
    }
    
    @SuppressWarnings("AccessOfSystemProperties")
    public static String getProperty(Keys key)
    {
        return System.getProperty(SystemProperties.getKey(key));        
    }

    public static String getKey(Keys key)
    {
        return key.toString().replace("_", ".");
    }
    
    @SuppressWarnings({"CollectionDeclaredAsConcreteClass", "OverlyLongMethod"})
    public static LinkedHashMap<String, Object> toLinkedHashMap(boolean all)
    {
        LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>();

        results.put("java.version", SystemProperties.create(Keys.java_version));
        results.put("java.vendor", SystemProperties.create(Keys.java_vendor));
        results.put("java.vendor.url", SystemProperties.create(Keys.java_vendor_url));
        results.put("java.home", SystemProperties.create(Keys.java_home));
        results.put("java.vm.specification.version", SystemProperties.create(Keys.java_vm_specification_version));
        results.put("java.vm.specification.vendor", SystemProperties.create(Keys.java_vm_specification_vendor));
        results.put("java.vm.specification.name", SystemProperties.create(Keys.java_vm_specification_name));
        results.put("java.vm.version", SystemProperties.create(Keys.java_vm_version));
        results.put("java.vm.vendor", SystemProperties.create(Keys.java_vm_vendor));
        results.put("java.vm.name", SystemProperties.create(Keys.java_vm_name));
        results.put("java.specification.version", SystemProperties.create(Keys.java_specification_version));
        results.put("java.specification.vendor", SystemProperties.create(Keys.java_specification_vendor));
        results.put("java.specification.name", SystemProperties.create(Keys.java_specification_name));
        results.put("java.class.version", SystemProperties.create(Keys.java_class_version));

        if(all)
        {
            results.put("java.class.path", SystemProperties.create(Keys.java_class_path));
            results.put("java.library.path", SystemProperties.create(Keys.java_library_path));
            results.put("java.io.tmpdir", SystemProperties.create(Keys.java_io_tmpdir));
            results.put("java.compiler", SystemProperties.create(Keys.java_compiler));
            results.put("java.ext.dirs", SystemProperties.create(Keys.java_ext_dirs));
            results.put("os.name", SystemProperties.create(Keys.os_name));
            results.put("os.arch", SystemProperties.create(Keys.os_arch));
            results.put("os.version", SystemProperties.create(Keys.os_version));
            results.put("file.separator", SystemProperties.create(Keys.file_separator));
            results.put("path.separator", SystemProperties.create(Keys.path_separator));
            results.put("line.separator", SystemProperties.create(Keys.line_separator));
            results.put("user.name", SystemProperties.create(Keys.user_name));
            results.put("user.home", SystemProperties.create(Keys.user_home));
            results.put("user.dir", SystemProperties.create(Keys.user_dir));
        }

        return results;
    }

    private static LinkedHashMap<String, Object> create(Keys key)
    {
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("value", SystemProperties.getProperty(key));
        result.put("label", SystemProperties.getLabel(key));
        return result;
    }
}
