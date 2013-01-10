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