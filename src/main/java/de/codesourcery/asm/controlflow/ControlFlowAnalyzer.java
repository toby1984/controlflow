package de.codesourcery.asm.controlflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import de.codesourcery.asm.controlflow.Edge.EdgeType;
import de.codesourcery.asm.util.ASMUtil;

public class ControlFlowAnalyzer
{
    private boolean debug = false;

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }
    
    @SuppressWarnings("unchecked")
    public ControlFlowGraph analyze(String owner,final MethodNode mn) throws AnalyzerException 
    {
        // line numbers with associated block
        // initially we'll create one block per line and merge adjacent ones later if control flow permits it  
        final Map<Integer,IBlock> blocks = new HashMap<>(); 

        final ListIterator<AbstractInsnNode> it = mn.instructions.iterator();

        IBlock currentLine=null;   
        Object previousMetadata = null;
        IBlock previous = null;
        final IBlock methodExit = new MethodExit();
        for( int instrCounter = 0 ; it.hasNext() ; instrCounter++ ) 
        {
            final AbstractInsnNode instruction = it.next();
            currentLine = getBlockForInstruction(instrCounter,blocks);

            if ( previous != null ) 
            {
                previous.addSuccessor( currentLine , EdgeType.REGULAR , previousMetadata );
                currentLine.addRegularPredecessor( previous );
                previousMetadata = null;
            }

            IBlock nextPrevious = currentLine;
            switch( instruction.getType() ) 
            {
                case AbstractInsnNode.LOOKUPSWITCH_INSN:
                    LookupSwitchInsnNode lookup = (LookupSwitchInsnNode) instruction;

                    // add edge for default handler
                    if ( lookup.dflt != null ) 
                    {
                        final IBlock target = getBlockForInstruction( lookup.dflt , mn , blocks );
                        target.addRegularPredecessor( currentLine );
                        currentLine.addRegularSuccessor( target );
                    }

                    @SuppressWarnings("cast")
                    final Iterator<Integer> keys = (Iterator<Integer> ) lookup.keys.iterator();

                    for ( LabelNode ln : (List<LabelNode>) lookup.labels ) 
                    {
                        final IBlock target = getBlockForInstruction( ln , mn , blocks );
                        final Integer key = keys.next();

                        target.addPredecessor( currentLine , EdgeType.LOOKUP_SWITCH , key );
                        currentLine.addSuccessor( target , EdgeType.LOOKUP_SWITCH , key );
                    }
                    nextPrevious = null;
                    break;                    

                case AbstractInsnNode.TABLESWITCH_INSN:

                    TableSwitchInsnNode tblSwitch = (TableSwitchInsnNode) instruction;

                    // add edge for default handler
                    if ( tblSwitch.dflt != null ) 
                    {
                        final IBlock target = getBlockForInstruction( tblSwitch.dflt , mn , blocks );
                        target.addRegularPredecessor( currentLine );
                        currentLine.addRegularSuccessor( target );
                    }
                    int currentKey = tblSwitch.min;

                    for ( LabelNode ln : (List<LabelNode>) tblSwitch.labels ) 
                    {
                        final IBlock target = getBlockForInstruction( ln , mn , blocks );

                        target.addPredecessor( currentLine , EdgeType.TABLE_SWITCH , currentKey );
                        currentLine.addSuccessor( target , EdgeType.TABLE_SWITCH , currentKey );

                        currentKey++;
                    }
                    nextPrevious = null;
                    break;

                case AbstractInsnNode.INSN:

                    if ( instruction.getOpcode() == Opcodes.RETURN || instruction.getOpcode() == Opcodes.IRETURN ) /* method exit */
                    {
                        currentLine.addRegularSuccessor( methodExit );
                        methodExit.addRegularPredecessor( currentLine );
                        nextPrevious = null;
                    } 
                    else if ( instruction.getOpcode() == Opcodes.ATHROW || instruction.getOpcode() == Opcodes.RET ) 
                    {
                        nextPrevious = null;
                    }
                    break;

                case AbstractInsnNode.JUMP_INSN: /* jump */

                    final JumpInsnNode jmp = (JumpInsnNode) instruction;
                    final LabelNode label = jmp.label;
                    final int target = mn.instructions.indexOf( label );

                    final boolean isConditional = ASMUtil.isConditionalJump( instruction );

                    if ( isConditional ) { // label edges of conditional jump instructions with "true" and "false
                        previousMetadata = "false";
                    }
                    
                    final IBlock targetBlock = getBlockForInstruction(target,blocks);
                    targetBlock.addRegularPredecessor( currentLine );

                    // create edge from current block to jump target 
                    currentLine.addSuccessor( targetBlock , EdgeType.REGULAR , isConditional ? "true" : null );

                    if ( instruction.getOpcode() == Opcodes.GOTO) {
                        nextPrevious = null;
                    }
                    break;
            }

            // link last instruction with method_exit block
            if ( ! it.hasNext() ) { 
                currentLine.addRegularSuccessor( methodExit );
                methodExit.addRegularPredecessor( currentLine );
            }
            previous = nextPrevious;
        }

        // try/catch blocks need special treatment because
        // they are not represented as opcodes
        for ( TryCatchBlockNode node : (List<TryCatchBlockNode>) mn.tryCatchBlocks ) 
        {
            final LabelNode startLabel = node.start;
            final int startTarget = mn.instructions.indexOf( startLabel );

            final LabelNode endLabel = node.end;
            final int endTarget = mn.instructions.indexOf( endLabel );            

            final int handlerTarget = mn.instructions.indexOf( node.handler );
            IBlock handler = getBlockForInstruction( node.handler , mn , blocks );

            for ( int i = startTarget ; i <= endTarget ; i++ ) 
            {
                if ( i != handlerTarget ) {
                    getBlockForInstruction( i , blocks ).addExceptionHandler( handler , node.type );
                }
            }
        }

        // merge adjacent instructions
        final Set<Integer> linesBeforeMerge = new HashSet<>();
        for ( IBlock block : blocks.values() ) {
        	linesBeforeMerge.addAll( block.getInstructionNums() );
        }
        
        final List<IBlock> result = mergeBlocks(blocks,mn);

    	if ( debug ) {
    		System.out.println("################ Control-blocks merged ################");
    	}
        // sanity check
        final Set<Integer> linesAfterMerge = new HashSet<>();
        for ( IBlock block : result ) 
        {
        	linesAfterMerge.addAll( block.getInstructionNums() );
        	if ( debug ) {
        		System.out.println("-----");
        		System.out.println( block+" has "+block.getByteCodeInstructionCount( mn )+" instructions.");
        		System.out.println( block.disassemble(mn , false , true ) );
        	}
            for ( Edge e : block.getEdges() ) {
                if ( ! result.contains( e.src ) && e.src != methodExit ) {
                    throw new RuntimeException( e+" has src that is not in result list?");
                }
                if ( ! result.contains( e.dst ) && e.dst != methodExit ) {
                    throw new RuntimeException( e+" has destination that is not in result list?");
                }
            }
        }
        
        if ( ! linesBeforeMerge.equals( linesAfterMerge ) ) {
        	throw new RuntimeException("Internal error, line count mismatch before/after control block merge: \n\n"+linesBeforeMerge+"\n\n"+linesAfterMerge);
        }

        // add starting block and link it with block that contains the lowest instruction number
        MethodEntry methodEntry = new MethodEntry();
        int lowest = Integer.MAX_VALUE;
        for ( Integer i : blocks.keySet() ) {
            if ( i < lowest ) {
                lowest = i;
            }
        }

        final IBlock firstBlock = blocks.get( lowest );
        if ( firstBlock.hasRegularPredecessor() ) {
            throw new IllegalStateException( firstBlock+" that constrains first instruction has a predecessor?");
        }        

        methodEntry.addRegularSuccessor( firstBlock );
        firstBlock.addRegularPredecessor( methodEntry );
        result.add( 0 , methodEntry );

        // add end block to results
        result.add( methodExit );

        return new ControlFlowGraph( mn , result );
    }    

    private IBlock getBlockForInstruction(LabelNode label , MethodNode mn, Map<Integer,IBlock> blocks) 
    {
        final int target = mn.instructions.indexOf( label );
        return getBlockForInstruction( target , blocks );
    }

    private int getSuccessorCountIgnoringEndBlock(IBlock block) {

        int count = 0;
        for ( IBlock b : block.getRegularSuccessors() ) {
            if ( !( b instanceof MethodExit ) ) {
                count++;
            }
        }
        return count;
    }

    private IBlock getSuccessorIgnoringEndBlock(IBlock block) {

        if ( block.getRegularSuccessorCount() >= 0 ) 
        {
            if ( block.getRegularSuccessorCount() > 2 ) {
                return block.getRegularSuccessor();
            }        

            for ( IBlock b : block.getRegularSuccessors() ) {
                if ( !( b instanceof MethodExit ) ) {
                    return b;
                }
            }
        }
        return null;
    }    

    // merge adjactent blocks
    private List<IBlock> mergeBlocks(Map<Integer, IBlock> blocks, final MethodNode mn)
    {
        final List<Integer> lines = new ArrayList<>(blocks.keySet());
        if ( lines.isEmpty() ) {
            throw new IllegalStateException("Method with no lines?");
        }

        Collections.sort( lines );

        // make sure there are no 'holes' in the line number sequence
        for ( int i = 0 ; i < lines.size() - 2 ; i++) {
            if ( lines.get(i)+1 != lines.get(i+1 ) ) {
                throw new IllegalStateException("Missing line "+(lines.get(i)+1 ) );                
            }
        }

        final IBlock[] sorted = new IBlock[lines.size()];

        // initialize each block with their line number
        for ( Map.Entry<Integer,IBlock> entry : blocks.entrySet() ) 
        {
            final IBlock block = entry.getValue();
            final Integer lineNo = entry.getKey();
            sorted[ lineNo ] = block;
            block.addInstructionNum( lineNo );
        }

        if ( debug) {
            for ( Integer key : lines ) {
                System.out.println("---- Block #"+key+": "+blocks.get(key)+" ----");
                System.out.println( blocks.get(key).disassemble( mn , true , true ) );
            }        
        }

        final List<IBlock> sortedList = new ArrayList<>( Arrays.asList( sorted ) );

        boolean merged = false;
        do 
        {
            merged = false;
            for ( int i = 0 ; ! merged && sortedList.size() > 1 && (i+1) < sortedList.size() ; i++ ) 
            {
                final IBlock current = sortedList.get(i);
                final IBlock next = sortedList.get(i+1);

                if ( getSuccessorCountIgnoringEndBlock( current ) == 1 && next.getRegularPredecessorCount() == 1 ) 
                {
                    if ( getSuccessorIgnoringEndBlock( current ) == next && next.getRegularPredecessor() == current ) 
                    {
                        if ( debug ) {
                            System.out.println("*** MERGING ***");
                            System.out.println("---------- First: "+current+" ------------------");
                            System.out.println( current.disassemble( mn , false , true ) );
                            System.out.println("---------- Second: "+next+" ------------------");
                            System.out.println( next.disassemble( mn , false , true ) );
                            System.out.println("--------------------------");
                        }

                        merged = true;
                        sortedList.remove( i+1 );
                        i--;

                        current.addInstructionNums( next );

                        current.removeRegularSuccessor( next );

                        for ( Edge edge : next.getEdges() ) 
                        {
                            if ( edge.isSuccessor( next ) ) 
                            {
                                final IBlock succ = edge.dst;
                                succ.blockReplaced( next , current );
                                current.addSuccessor( succ , edge.type , edge.metaData );
                            } 
                            else if ( edge.isPredecessor( next ) ) 
                            {
                                final IBlock pred = edge.src;
                                if ( pred != current ) {
                                    pred.blockReplaced( next , current );
                                }
                            }
                        }
                        if ( debug ) {
                            // TODO: remove debug code
                            System.out.println("*** AFTER MERGE ***");    
                            System.out.println("---------- MERGED: "+current+" ------------------");                        
                            System.out.println( current.disassemble( mn , false , true ) );
                        }

                    } else {
                        if ( debug) {
                            System.err.println("Not merged - no direct edge: "+current+" <-> "+next);
                        }
                    }
                } else {
                    if ( debug ) {
                        System.err.println("Not merged - successor/Predecessor count mismatch: "+current+" <-> "+next);
                    }
                }
            }
        } while ( merged );

        return sortedList;
    }

    private IBlock getBlockForInstruction(int index,Map<Integer,IBlock> blocks) {
        IBlock result = blocks.get( index );
        if ( result == null ) {
            result = new Block();
            result.addInstructionNum( index );
            blocks.put( index , result );
        }
        return result;
    }
}
