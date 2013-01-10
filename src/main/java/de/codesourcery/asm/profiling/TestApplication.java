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
