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
