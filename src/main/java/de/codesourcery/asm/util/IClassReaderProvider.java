package de.codesourcery.asm.util;

import java.io.IOException;

import org.objectweb.asm.ClassReader;

/**
 * Strategy interface for <code>ClassReader</code> factories.
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public interface IClassReaderProvider
{
    /**
     * Create a classreader.
     * 
     * @return
     * @throws IOException
     */
    public ClassReader getClassReader() throws IOException;
    
    /**
     * Returns the fully-qualified name of the class that
     * {@link #getClassReader()} will read.
     * 
     * @return
     */
    public String getClassName();
}
