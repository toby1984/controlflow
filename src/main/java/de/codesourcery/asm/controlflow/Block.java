package de.codesourcery.asm.controlflow;

/**
 * A regular node in the control-flow graph.
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public class Block extends AbstractBlock 
{
    @Override
    public void addRegularPredecessor(IBlock block) {
        edges.add( new Edge( block , this ) );
    }
    
    @Override
    public void addRegularSuccessor(IBlock block) {
        edges.add( new Edge( this , block ) );
    }        

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("Block( instructions="+instructionsToString()+" , ");
        
        result.append(",  edges = ");
        for ( Edge edge : getEdges() ) {
            result.append( edge.toString( this ) );
            result.append(",");
        }
        result.append( ")" );
        return result.toString();
    }
    
    @Override
    public String toSimpleString()
    {
        return "Block( instructions = "+instructionsToString()+" )";               
    }           
}