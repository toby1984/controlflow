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
