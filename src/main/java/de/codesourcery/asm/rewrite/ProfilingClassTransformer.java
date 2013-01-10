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
package de.codesourcery.asm.rewrite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.ClassReader;

import de.codesourcery.asm.util.IClassReaderProvider;
import de.codesourcery.asm.util.IJoinpointFilter;

/**
 * A Java instrumentation agent that applies the {@link ProfilingRewriter} to
 * selected classes.
 * 
 * <p>To use this agent, it needs to be part of a JAR file with a 'Pre-mainclass:' attribute in it's manifest
 * file that points to this class.</p>
 * 
 * <p>An example JVM invocation would look like</p>
 * 
 * <pre>
 * java "-javaagent:target/profilingagent.jar=packages=TestClass;debug=true" ...
 * </pre>
 * 
 * <p>
 * The agent supports the following agent options of which only <code>packages</code> is mandatory. Multiple
 * options need to be separated by semicolons (packages=a;debug=true;...)</p>
 * 
 * <table border="1">
 *   <tr>
 *     <td>Option</td>
 *     <td>Mandatory?</td> 
 *     <td>Description</td>
 *     <td>Example</td>
 *   </tr> 
 *   <tr>
 *     <td>packages</td>
 *     <td>YES</td>
 *     <td>comma-separated list of patterns a fully-qualified classname needs to match in order to be instrumented</td>
 *     <td>packages=some.package,some.package.MyClass,ClassInAnyPackage</td>
 *   </tr>
 *   <tr>
 *     <td>debug</td>
 *     <td>no</td>
 *     <td>comma-separated list of patterns a fully-qualified classname needs to match in order to be instrumented</td>
 *     <td>debug=true</td>
 *   </tr> 
 *   <tr>
 *     <td>debugDir</td>
 *     <td>no</td>
 *     <td>name of directory where instrumented classes should be written to</td>
 *     <td>debugDir=/tmp</td>
 *   </tr>  
 * </table>
 * </p>
 * 
 * @author tobias.gierke@code-sourcery.de
 * @see ProfilingRewriter
 */
public class ProfilingClassTransformer
{
    private static final String OPTION_DEBUG = "debug";
    private static final String OPTION_DEBUG_WRITE_CLASSFILES = "debugDir";
    private static final String OPTION_PACKAGES = "packages";

    public static void premain(String agentArgs, Instrumentation inst) 
    {
        // parse options
        final Map<String, String> options = parseArgs( agentArgs);
        
        if ( StringUtils.isBlank( options.get(OPTION_PACKAGES ) ) )
        {
            throw new RuntimeException("Agent "+ProfilingClassTransformer.class.getName()+" requires the 'packages=....' option");
        }

        final boolean debug = options.containsKey(OPTION_DEBUG);
        
        final String[] packages = options.get(OPTION_PACKAGES ).split(",");
        
        if ( ArrayUtils.isEmpty( packages ) ) 
        {
            throw new RuntimeException("Agent "+ProfilingClassTransformer.class.getName()+" requires at least one pattern with the 'packages=....' option");            
        }
        
        if ( debug ) {
            System.out.println("ProfilingClassTransformer activated (packages: "+StringUtils.join(packages," , " )+")");
        }

        final IJoinpointFilter filter = new IJoinpointFilter() {
            
            @Override
            public boolean matches(String clazz, String methodName)
            {
                return true;
            }
            
            @Override
            public boolean matches(String clazz)
            {
                for ( String p : packages ) {
                    if ( clazz.contains( p ) ) {
                        return true;
                    }
                }
                return false;
            }
        };
        
        final File debugOutputDir = options.containsKey( OPTION_DEBUG_WRITE_CLASSFILES ) ? new File(  options.get( OPTION_DEBUG_WRITE_CLASSFILES ) ) : null;
        inst.addTransformer(new MyTransformer(filter,debug,debugOutputDir) , false ); // no re-transformation support
    }

    private static Map<String,String> parseArgs(String arguments) 
    {
        final Map<String,String> result = new HashMap<>();
        if ( StringUtils.isBlank( arguments ) ) {
            return result;
        }
        for ( String pair : arguments.split(";") ) {
            final String[] keyValue = pair.split("=");
            if ( keyValue.length != 2 ) {
                throw new IllegalArgumentException("Malformed agent options: '"+arguments+"'");
            }
            result.put( keyValue[0] , keyValue[1] );
        }
        return result;
    }
    
    public static final class MyTransformer implements ClassFileTransformer 
    {
        private final ProfilingRewriter rewriter = new ProfilingRewriter();
        private final IJoinpointFilter filter;
        private final boolean debug;
        private final File debugWriteClassfiles;
        
        public MyTransformer(IJoinpointFilter filter,boolean debug,File debugWriteClassfiles) 
        {
            this.filter = filter;
            this.debug = debug;
            if ( debug ) {
                rewriter.setDebugMode( true );
                rewriter.setVerboseMode( true );
            }
            this.debugWriteClassfiles = debugWriteClassfiles;
        }
        
        public byte[] transform(ClassLoader l, String name, Class<?> c,ProtectionDomain d, final byte[] b) throws IllegalClassFormatException 
        {
            final String fqName = name.replace("/",".");
            if ( ! filter.matches( fqName ) ) {
                return b;
            }
            
            if ( debug ) {
                System.out.println("Transforming class "+fqName);
            }
            
            final IClassReaderProvider provider = new IClassReaderProvider() {
                
                @Override
                public ClassReader getClassReader() throws IOException
                {
                    return new ClassReader(b);
                }
                
                @Override
                public String getClassName()
                {
                    return fqName;
                }
            };
            
            try {
                final byte[] result = rewriter.rewrite(provider, filter);
                if ( debugWriteClassfiles != null ) 
                {
                    writeDebugClassfile( fqName , result );
                }
                return result;
            } 
            catch (Exception e) {
                e.printStackTrace();
                throw new IllegalClassFormatException("Transformer failed for class "+fqName);
            }
        }
        
        private void writeDebugClassfile(String fqName , byte[] data) 
        {
            final File outfile = new File( debugWriteClassfiles , fqName.replace(".",File.separator)+".class" );
            
            final File outputFolder = outfile.getParentFile();
            if ( ! outputFolder.exists() ) {
                outputFolder.mkdirs();
            }
            
            if ( outputFolder.exists() && outputFolder.isDirectory() ) 
            {
                if ( debug ) {
                    System.out.println("Writing transformed class "+fqName+" to "+outfile.getAbsolutePath());
                }
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream( outfile );
                    out.write( data );
                } catch(Exception e) {
                    System.err.println("Failed to write transformed class "+fqName+" to "+outfile.getAbsolutePath()+" ("+e.getMessage()+")");
                } 
                finally {
                    try { 
                        if ( out != null) {
                            out.close();
                        }
                    } catch(Exception e) { /* ok */ }
                }
            }
        }
    }        
}
