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

import java.util.NoSuchElementException;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import de.codesourcery.asm.controlflow.Edge.EdgeType;

/**
 * A node in the control-flow graph.
 * 
 * @author tobias.gierke@code-sourcery.de
 * 
 * @see Edge
 */
public interface IBlock 
{
    /**
     * Add an edge with type {@link EdgeType#CAUGHT_EXCEPTION} to this node.
     * 
     * @param handler exception handler 
     * @param exceptionType type of the caught exception
     * 
     * @see EdgeType#CAUGHT_EXCEPTION
     */
    public void addExceptionHandler( IBlock handler , String exceptionType );
    
    /**
     * Marks an instruction as being part of this node.
     * 
     * @param num
     * @see #getInstructionNums()
     */
    public void addInstructionNum(int num);    
    
    /**
     * Adds all instruction indices from another block to this one.
     * @param other
     * @see #getInstructionNums()
     */
    public void addInstructionNums(IBlock other);
    
    /**
     * Adds a predecessor edge with custom type and meta-data.
     *
     * @param block
     * @param edgeType
     * @param metaData meta-data, may be <code>null</code>     
     */    
    public void addPredecessor(IBlock block,Edge.EdgeType edgeType,Object metaData);
    
    /**
     * Adds a predecessor edge with type {@link EdgeType#REGULAR}.
     * 
     * @param block
     */
    public void addRegularPredecessor(IBlock block);
    
    /**
     * Adds a successor edge with type {@link EdgeType#REGULAR}.
     * 
     * @param block
     */    
    public void addRegularSuccessor(IBlock block); 
    
    /**
     * Adds a successor edge with custom type and meta-data.
     * 
     * @param block
     * @param edgeType
     * @param metaData meta-data, may be <code>null</code>
     */      
    public void addSuccessor(IBlock block,Edge.EdgeType edgeType,Object metaData);
    
    /**
     * Invoked when a block has been replaced with another.
     * 
     * <p>This method is responsible for replacing all internal references
     * to <code>oldBlock</code> with <code>newBlock</code>.</p>
     * 
     * @param oldBlock
     * @param newBlock
     */
    public void blockReplaced(IBlock oldBlock,IBlock newBlock);
    
    public boolean containsInstructionNum(int num);    
    
    /**
     * Disassembles this node.
     * 
     * @param method the method this node was generated from
     * @param includeVirtual whether to also 'disassemble' virtual (ASM-generated) instructions.
     * @param printInsnIndices whether to prefix each line with the ASM instruction index
     * @return
     */
    public String disassemble(MethodNode method,boolean includeVirtual,boolean printInsnIndices);
    
    /**
     * Returns the number of actual bytecode instructions for this node.
     * @param method
     * @return
     */
    public int getByteCodeInstructionCount(MethodNode method);
    
    /**
     * Returns all edges of this node.
     * 
     * @return
     */
    public Set<Edge> getEdges();
    
    /**
     * Returns the index of the first bytecode instruction in this block.
     * 
     * @return
     * @throws NoSuchElementException if this block is virtual or contains no instructions at all
     * @see #isVirtual(MethodNode)
     */    
    public int getFirstByteCodeInstructionNum(MethodNode method) throws NoSuchElementException;
    
    /**
     * Returns the index of the first ASM instruction in this block.
     * 
     * <p>Note that the returned instruction might be 'virtual' (=ASM-generated with
     * no equivalent in the .class file).</p>
     * 
     * @return
     * @throws NoSuchElementException if this block contains no instructions at all
     */
    public int getFirstInstructionNum() throws NoSuchElementException;
    
    /**
     * Returns this block's ID.
     * 
     * @return ID , may be <code>null</code> if the block is not yet part of a {@link ControlFlowGraph}.
     */
    public String getId();
    
    /**
     * Returns the index of the INVOKESPECIAL instruction in this 
     * node that invokes the super-classes constructor.
     * 
     * @param method the method this node was generated from
     * @return index or -1 if this block does not invoke a super-constructor or the passed method is no constructor method
     */
    public int getIndexOfSuperConstructorCall(MethodNode method);  
    
    /**
     * Returns the instruction indices covered by this block.
     * 
     * @return
     */
    public Set<Integer> getInstructionNums();    
    
    /**
     * Returns the regular ({@link EdgeType#REGULAR}) predecessor of this node.
     *  
     * @return
     * @throws IllegalStateException if this node has more than one regular predecessor
     * @throws NoSuchElementException if this node has no (regular) predecessors.     
     */
    public IBlock getRegularPredecessor() throws NoSuchElementException,IllegalStateException;        
    
    /**
     * Returns the number of predecessors of this block with {@link EdgeType#REGULAR}.
     * 
     * @return
     * @see #getRegularPredecessors()
     * @see #getRegularPredecessor()
     */        
    public int getRegularPredecessorCount();
    
    /**
     * Returns all predecessors of this block with {@link EdgeType#REGULAR}.
     * @return
     */    
    public Set<IBlock> getRegularPredecessors();        
    
    /**
     * Returns the regular ({@link EdgeType#REGULAR}) successor of this node.
     *  
     * @return
     * @throws IllegalStateException if this node has more than one regular successor
     * @throws NoSuchElementException if this node has no (regular) successors.
     */    
    public IBlock getRegularSuccessor() throws NoSuchElementException,IllegalStateException;
    
    /**
     * Returns the number of successors of this block with {@link EdgeType#REGULAR}.
     * 
     * @return
     */    
    public int getRegularSuccessorCount();
    
    /**
     * Returns all successors of this block with {@link EdgeType#REGULAR}.
     * @return
     */
    public Set<IBlock> getRegularSuccessors();
    
    /**
     * Check whether this node has a predecessor with {@link EdgeType#REGULAR}.
     * 
     * @return
     * @see #getRegularSuccessor()
     * @see #getRegularSuccessors()
     */    
    public boolean hasRegularPredecessor();
    
    /**
     * Check whether this node has a successor with {@link EdgeType#REGULAR}.
     * 
     * @return
     * @see #getRegularSuccessor()
     * @see #getRegularSuccessors()
     */
    public boolean hasRegularSuccessor();
    
    /**
     * Check whether this is a 'virtual' node.
     * 
     * <p>A virtual block refers only to ASM-generated {@link AbstractInsnNode}s that
     * have no equivalent in a .class file.</p>
     * 
     * @param method
     * @return
     */
    public boolean isVirtual(MethodNode method);
    
    /**
     * Removes a successor with @link EdgeType#REGULAR}.
     * @param block
     * @return <code>true</code> if the edge could be removed
     */
    public boolean removeRegularSuccessor(IBlock block);
    
    /**
     * Sets the ID of this block.
     * 
     * @param id ID , not <code>null</code>
     * @see #getId()
     */
    public void setId(String id);
    
    /**
     * Returns a simple string representation of this block.
     * @return
     */    
    public String toSimpleString();
    
    /**
     * Returns a string representation of this block.
     * @return
     */
    public String toString();
}