/**
 * Copyright 2012 Tobias Gierke <tobias.gierke@code-sourcery.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codesourcery.asm.util;

/**
 * Used when deciding whether a certain class/method should be processed. 
 * 
 * <p>Note that just like ASM, I do not make a distinction between
 * methods and constructors.</p>
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public interface IJoinpointFilter
{
    /**
     * Matches all classes and methods/constructors.
     */
    public static IJoinpointFilter ALL = new IJoinpointFilter() {

        @Override
        public boolean matches(String clazz)
        {
            return true;
        }

        @Override
        public boolean matches(String clazz, String methodName)
        {
            return true;
        }
    };
    
    /**
     * Matches all classes and methods except constructors.
     */    
    public static IJoinpointFilter ALL_EXCEPT_CONSTRUCTORS = new IJoinpointFilter() {

        @Override
        public boolean matches(String clazz)
        {
            return true;
        }

        @Override
        public boolean matches(String clazz, String methodName)
        {
            return ! methodName.equals("<init>");
        }
    };    
    
    /**
     * Check whether a given class matches.
     * 
     * @param clazz fully-qualified class name
     * @return
     */
    public boolean matches(String clazz);
    
    /**
     * Check whether a given method matches.
     * 
     * @param clazz fully-qualified class name
     * @param methodName method name as read from a classfile. Constructor methods are always named '&lt;init&gt;'
     * @return
     */
    public boolean matches(String clazz,String methodName);
}
