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
package de.codesourcery.asm.controlflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.tree.MethodNode;

/**
 * Execution control-flow graph.
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public class ControlFlowGraph
{
    private final MethodNode method;
    
    private Set<IBlock> allNodes = null; // populated lazily
    private MethodEntry start;
    private MethodExit end;
    
    /**
     * Creates the graph and automatically assigns unique IDs to all nodes witout an ID.
     * 
     * @param method
     * @param blocks
     * 
     * @see IBlock#getId()
     */
    public ControlFlowGraph(MethodNode method, List<IBlock> blocks) 
    {
        if ( method == null ) {
            throw new IllegalArgumentException("method must not be NULL.");
        }
        if ( blocks == null ) {
            throw new IllegalArgumentException("blocks must not be NULL.");
        }
        if ( blocks.isEmpty() ) {
            throw new IllegalArgumentException("Method "+method.name+" has no blocks?");
        }
        
        this.method = method;
        final Set<String> ids = new HashSet<>();
        
        for ( IBlock b : blocks ) 
        {
            if ( b instanceof MethodEntry ) 
            {
                if ( start != null ) {
                    throw new IllegalArgumentException("Input contains more than one method-entry block?");
                }
                if ( b.hasRegularPredecessor() ) {
                    throw new IllegalArgumentException("Input contains method-entry block with predecessor ?");
                }
                start = (MethodEntry) b;
            } 
            else if ( b instanceof MethodExit ) 
            {
                if ( end != null ) {
                    throw new IllegalArgumentException("Input contains more than one method-exit block");
                }
                if ( b.hasRegularSuccessor() ) {
                    throw new IllegalArgumentException("Input contains method-exit block with successor ?");
                }                
                end = (MethodExit) b;
            }
            
            // assign unique ID if not already done so
            if ( StringUtils.isEmpty( b.getId() ) ) 
            {
                String newId;
                if ( b == start ) {
                    newId = "START";
                }
                else if ( b == end ) 
                {
                    newId = "END";
                } 
                else 
                {
                    List<Integer> sorted = new ArrayList<>( b.getInstructionNums() );
                    Collections.sort( sorted );
                    newId = "\""+StringUtils.join( sorted , "_" )+"\"";
                    if ( ids.contains( newId ) ) 
                    {
                        int i = 1;
                        while ( ids.contains( Integer.toString( i ) ) ) {
                            i++;
                        }
                        newId = Integer.toString( i );
                    }
                }
                ids.add( newId );
                b.setId( newId );
            } 
            else 
            {
                if ( ids.contains( b.getId() ) ) {
                    throw new IllegalArgumentException("Input contains multiple blocks with ID '"+b.getId()+"'");
                }
                ids.add( b.getId() );
            }            
        }
        
        if ( start == null ) {
            throw new IllegalArgumentException("Input contains no start block");
        }
        
        if ( end == null ) {
            throw new IllegalArgumentException("Input contains no end block");
        }        
    }
    
    /**
     * Returns the block that contains a given instruction node.
     * 
     * @param instructionNum ASM instruction node index 
     * @return block or <code>null</code>
     */
    public IBlock getBlockForInstruction(int instructionNum) 
    {
        for ( IBlock block : getAllNodes() ) {
            if ( block.containsInstructionNum( instructionNum ) ) {
                return block;
            }
        }
        return null;
    }
    
    /**
     * Returns all nodes of this graph.
     * 
     * @return
     */
    public Set<IBlock> getAllNodes() 
    {
        if ( allNodes == null ) {
            final Set<IBlock> result = new HashSet<>();
            getAllNodes( start , result );
            allNodes = result;
        }
        return allNodes;
    }
    
    private void getAllNodes(IBlock current , Set<IBlock> result) 
    {
        if ( result.contains( current ) ) {
            return;
        }
        result.add( current );
        
        for ( Edge e : current.getEdges() ) {
            getAllNodes(e.src , result );
            getAllNodes(e.dst , result );
        }
    }    
    
    /**
     * Returns the method node this graph was generated from. 
     * @return
     */
    public MethodNode getMethod()
    {
        return method;
    }
    
    /**
     * Returns the starting (method-entry) node of this graph.
     * 
     * @return
     */
    public MethodEntry getStart()
    {
        return start;
    }
    
    /**
     * Returns the ending (method-exit) node of this graph.
     * @return
     */
    public MethodExit getEnd()
    {
        return end;
    }
}