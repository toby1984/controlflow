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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.ArrayUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Various ASM-related utility methods.
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public class ASMUtil
{
    public interface ILogger {
        public void logVerbose(String msg);
    }
    
    /**
     * Check whether an instruction is a conditional branch operation.
     *  
     * @param node
     * @return
     */
    public static boolean isConditionalJump(AbstractInsnNode node) {
        if ( node.getType() == AbstractInsnNode.JUMP_INSN ) 
        {
            switch( node.getOpcode() ) 
            {
                case Opcodes.IFEQ:
                case Opcodes.IFNE:
                case Opcodes.IFLT:
                case Opcodes.IFGE:
                case Opcodes.IFGT:
                case Opcodes.IFLE:
                case Opcodes.IF_ICMPEQ:
                case Opcodes.IF_ICMPNE:
                case Opcodes.IF_ICMPLT:
                case Opcodes.IF_ICMPGE:
                case Opcodes.IF_ICMPGT:
                case Opcodes.IF_ICMPLE:
                case Opcodes.IF_ACMPEQ:
                case Opcodes.IF_ACMPNE:
                case Opcodes.IFNULL:
                case Opcodes.IFNONNULL:
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Create an ASM <code>ClassReader</code> for a given class , searching an optional classpath.
     * 
     * <p>If a classpath is specified, it is searched before the system class path.</p>
     * 
     * @param classToAnalyze
     * @param classPathEntries optional classpath that may contain directories or ZIP/JAR archives, may be <code>null</code>.
     * @return
     * @throws IOException
     */
    public static ClassReader createClassReader(String classToAnalyze, File[] classPathEntries) throws IOException 
    {
        return createClassReader(classToAnalyze,classPathEntries,new ILogger() { @Override public void logVerbose(String msg) {} });
    }
    
    /**
     * Create an ASM <code>ClassReader</code> for a given class , searching an optional classpath.
     * 
     * <p>If a classpath is specified, it is searched before the system class path.</p>
     * 
     * @param classToAnalyze
     * @param classPathEntries optional classpath that may contain directories or ZIP/JAR archives, may be <code>null</code>.
     * @param logger Logger used to output debug messages
     * @return
     * @throws IOException
     */    
    public static ClassReader createClassReader(String classToAnalyze, File[] classPathEntries,ILogger logger) throws IOException 
    {
        if ( ! ArrayUtils.isEmpty( classPathEntries ) ) 
        {
            // convert class name file-system path         
            String relPath = classToAnalyze.replace("." , File.separator );
            if ( ! relPath.endsWith(".class" ) ) {
                relPath += ".class";
            }
            // look through search-path entries
            for ( File parent : classPathEntries ) 
            {
                logger.logVerbose("Searching class in "+parent.getAbsolutePath());
                if ( parent.isDirectory() ) // path entry is a directory
                {
                    final File classFile = new File( parent , relPath );
                    if ( !classFile.exists() ) 
                    {
                        continue;
                    }
                    try {
                        logger.logVerbose("Loading class '"+classToAnalyze+"' from "+classFile.getAbsolutePath()+"");
                        return new ClassReader( new FileInputStream( classFile ) );
                    }
                    catch (IOException e) {
                        throw new IOException("Failed to load class '"+classToAnalyze+"' from "+classFile.getAbsolutePath(),e);
                    }
                } 
                else if ( parent.isFile() ) // path entry is a (ZIP/JAR) file 
                { 
                    final Path archive = Paths.get( parent.getAbsolutePath() );
                    final FileSystem fs = FileSystems.newFileSystem(archive , null);
                    final Path classFilePath = fs.getPath( relPath );

                    if ( Files.exists( classFilePath ) ) 
                    {
                        // load class from archive
                        try {
                            logger.logVerbose("Loading class '"+classToAnalyze+"' from archive "+archive.toAbsolutePath());
                            InputStream in = fs.provider().newInputStream( classFilePath );
                            return new ClassReader( in );
                        } 
                        catch(IOException e) 
                        {
                            throw new IOException("Failed to load class '"+classToAnalyze+"' from "+classFilePath.toAbsolutePath(),e);
                        }
                    }
                    continue;
                }
                throw new IOException("Invalid entry on search classpath: '"+parent.getAbsolutePath()+"' is neither a directory nor JAR/ZIP archive");
            }
        }

        // fall-back to using standard classpath
        logger.logVerbose("Trying to load class "+classToAnalyze+" using system classloader.");
        
        try {
            return new ClassReader( classToAnalyze );
        } 
        catch (IOException e) {
            throw new IOException("Failed to load class '"+classToAnalyze+"'",e);
        }
    }     
}
