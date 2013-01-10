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

import de.codesourcery.asm.rewrite.ProfilingRewriter;

/**
 * Thread-local used to keep track of per-thread execution statistics.
 * 
 * <p>Right now this class only tracks the number of executed instructions.</p>
 * 
 * @author tobias.gierke@code-sourcery.de
 * @see ProfilingRewriter 
 */
public class ExecutionStatistics
{
    /**
     * Number of instructions executed on the current thread.
     * 
     * <p>For performance reasons this value is always initialized with
     * -{@link StatisticsManager#GRANULARITY} and then incremented. Whenever
     * it reaches a positive value, a call to {@link StatisticsManager#account()}
     * is triggered.</p>
     * 
     * @see ProfilingRewriter
     */
    public int executedInstructionCount=-StatisticsManager.GRANULARITY;
}
