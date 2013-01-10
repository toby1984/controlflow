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
package de.codesourcery.asm.profiling;

import de.codesourcery.asm.TestClass;

/**
 * Sample application.
 * 
 * <p>Just a sample to demonstrate that a class was actually instrumented. 
 * </p>
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public class TestApplication
{
    public static void main(String[] args)
    {
        new TestClass().testMethod(true);
        System.out.println("Instructions executed: "+StatisticsManager.getExecutedInstructionsCount());
    }
}
