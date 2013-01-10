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
package de.codesourcery.asm;

/**
 * Just a sample class used in testing. 
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public class TestClass
{
    public TestClass() {
        System.out.println("Testclass constructed.");
    }
    
    public void testMethod(boolean a) 
    {
        if ( a ) {
            System.out.println("testMethod() called.");
        } else {
            System.out.println("what??");
        }
    }
}