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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;

import de.codesourcery.asm.controlflow.Edge.EdgeType;
import de.codesourcery.asm.util.Disassembler;

/**
 * Default control-flow graph node implementation.
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public abstract class AbstractBlock implements IBlock
{
    protected String id;
    
    protected final Set<Integer> instructions = new HashSet<>();  
    protected final Set<Edge> edges = new HashSet<>();
    
    @Override
    public String disassemble(MethodNode method,boolean includeVirtual,boolean printInsnIndices) {
        
        final StringBuilder builder = new StringBuilder();
        
        @SuppressWarnings("unchecked")
		final ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
        for ( int index = 0; iterator.hasNext() ; index++ ) 
        {
            final AbstractInsnNode instruction = iterator.next();
            
            if ( containsInstructionNum( index ) ) 
            {
                String line = Disassembler.disassemble( instruction , method , includeVirtual , printInsnIndices );
                if ( line != null ) 
                {
                    if ( builder.length() > 0 ) {
                        builder.append("\n");
                    }
                    builder.append( line );
                }
            }
        }
        return builder.toString();
    }
    
    @Override
    public int getIndexOfSuperConstructorCall(MethodNode method) 
    {
        if ( ! method.name.equals("<init>" ) ) {
            return -1;
        }
        
        @SuppressWarnings("unchecked")
		final ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
        for ( int index = 0; iterator.hasNext() ; index++ ) 
        {
            final AbstractInsnNode instruction = iterator.next();
            if ( containsInstructionNum( index ) && instruction.getOpcode() == Opcodes.INVOKESPECIAL ) 
            {
                final MethodInsnNode invocation = (MethodInsnNode) instruction;
                if ( invocation.name.equals("<init>") ) {
                    return index;
                }
            }
        }
        return -1;        
    }
    
    @Override
    public int getFirstInstructionNum() throws NoSuchElementException
    {
        final List<Integer> sorted = new ArrayList<>(instructions);
        Collections.sort(sorted);
        return sorted.get(0);
    }
    
    @Override
    public int getFirstByteCodeInstructionNum(MethodNode method) throws NoSuchElementException
    {
        final List<Integer> sorted = new ArrayList<>(instructions);
        Collections.sort(sorted);
        for ( int idx : sorted ) {
            AbstractInsnNode instruction = method.instructions.get( idx );
            if ( instruction.getOpcode() != -1 ) {
                return idx;
            }
        }
        throw new NoSuchElementException("Block contains only virtual instructions");
    }
    
    @Override
    public void addInstructionNum(int num)
    {
        instructions.add( num );
    }
    
    @Override
    public Set<Edge> getEdges() {
        return edges;
    }
    
    public void addPredecessor(IBlock block,Edge.EdgeType edgeType,Object metaData) {
        edges.add( new Edge( block , this , edgeType ,metaData ) );
    }
    
    public void addSuccessor(IBlock block,Edge.EdgeType edgeType,Object metaData) {
        edges.add( new Edge( this , block , edgeType ,metaData ) );
    }
    
    @Override
    public void addExceptionHandler( IBlock handler , String exceptionType ) 
    {
        addSuccessor( handler , EdgeType.CAUGHT_EXCEPTION , exceptionType );
    }
    
    @Override
    public boolean isVirtual(MethodNode method) {
        return getByteCodeInstructionCount( method ) == 0;
    }
    
    @Override
    public int getByteCodeInstructionCount(MethodNode method) {
        
        final InsnList instructions = method.instructions;
        @SuppressWarnings("unchecked")
		final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
        
        int count = 0;
        for ( int index = 0 ; iterator.hasNext() ; index++ ) 
        {
            final AbstractInsnNode node = iterator.next();
            if ( containsInstructionNum( index ) ) {
                final int opCode = node.getOpcode();
                if ( opCode >= 0 && opCode < Printer.OPCODES.length ) { 
                    count++;
                }
            }
        }
        return count;
    }
    
   
    
    public boolean containsInstructionNum(int num) {
        return instructions.contains(num);
    }
    
    public void addInstructionNums(IBlock other) {
        instructions.addAll( other.getInstructionNums() );
    }
    
    public Set<Integer> getInstructionNums() {
        return instructions;
    }
    
    public void blockReplaced(IBlock oldBlock,IBlock newBlock) 
    {
        for ( Edge e : new HashSet<Edge>( this.edges ) ) 
        {
            if ( e.src == oldBlock ) 
            {
                edges.remove( e );
                edges.add( e.withSource( newBlock ) );
            } 
            else if ( e.dst == oldBlock ) 
            {
                edges.remove( e );
                edges.add( e.withDestination( newBlock ) );                
            }
        }
    }
    
    public boolean removeRegularSuccessor(IBlock block) 
    {
        for ( Iterator<Edge> it = edges.iterator() ; it.hasNext() ; ) 
        {
            final Edge e = it.next();
            if ( isRegularSuccessor( e ) && e.dst == block ) {
                it.remove();
                return true;
            }
        }
        return false;
    }
    
    public boolean hasRegularSuccessor() 
    {
        for ( Edge edge : edges ) {
            if ( isRegularSuccessor( edge ) ) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isRegularSuccessor(Edge e) {
        return isRegular( e ) && e.isSuccessor( this );
    }    
    
    private boolean isRegular(Edge e) {
        switch( e.type) {
            case REGULAR:
            case LOOKUP_SWITCH:
            case TABLE_SWITCH:
                return true;
            default:
                return false;
        }
    }
    
    private boolean isRegularPredecessor(Edge e) {
        return isRegular( e ) && e.isPredecessor( this );
    }       
    
    @Override
    public boolean hasRegularPredecessor() 
    {
        for ( Edge edge : edges ) {
            if ( isRegularPredecessor( edge ) ) {
                return true;
            }
        }
        return false;        
    }
    
    @Override
    public IBlock getRegularSuccessor() throws NoSuchElementException,IllegalStateException {
        if ( getRegularSuccessorCount() > 1 ) {
            throw new IllegalStateException("Cannot call getSuccessor() on block with "+getRegularSuccessorCount()+" successors");
        }
        return getRegularSuccessors().iterator().next();
    }
    
    @Override
    public IBlock getRegularPredecessor() throws NoSuchElementException,IllegalStateException{
        if ( getRegularPredecessorCount() > 1 ) {
            throw new IllegalStateException("Cannot call getPredecessor() on block with "+getRegularPredecessorCount()+" predecessors");
        }
        return getRegularPredecessors().iterator().next();
    }
    
    @Override
    public int getRegularSuccessorCount() {
        return getRegularSuccessors().size();
    }
    
    @Override
    public int getRegularPredecessorCount() {
        return getRegularPredecessors().size();
    }
    
    @Override
    public void setId(String id)
    {
        if (id == null) {
            throw new IllegalArgumentException("id must not be NULL.");
        }
        this.id = id;
    }
    
    @Override
    public String getId()
    {
        return id;
    }
    
    @Override
    public Set<IBlock> getRegularSuccessors()
    {
        Set<IBlock> result = new HashSet<>();
        for ( Edge edge : edges ) {
            if ( isRegularSuccessor( edge ) ) {
                result.add( edge.dst );
            }
        }
        return result;        
    }
    
    @Override
    public Set<IBlock> getRegularPredecessors()
    {
        Set<IBlock> result = new HashSet<>();
        for ( Edge edge : edges ) {
            if ( isRegularPredecessor( edge ) ) {
                result.add( edge.src );
            }
        }
        return result;
    }
    
    @Override
    public void addRegularPredecessor(IBlock block) {
        throw new UnsupportedOperationException("Cannot add predecessor "+block+" to "+this);
    }
    
    @Override
    public void addRegularSuccessor(IBlock block) {
        throw new UnsupportedOperationException("Cannot add successor "+block+" to "+this);
    }        
    
    @Override
    public String toString()
    {
        return toSimpleString();
    }  
    
    public String toSimpleString()
    {
        return "Block( instructions = "+instructionsToString()+" )";
    }          
    
    protected final String instructionsToString() {
        List<Integer> sorted = new ArrayList<>(instructions);
        Collections.sort(sorted);
        return StringUtils.join( sorted , "," );
    }
}