package de.codesourcery.asm.controlflow;

/**
 * Control-flow graph node: Method exit.
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public class MethodExit extends AbstractBlock 
{
    @Override
    public void addRegularPredecessor(IBlock block) 
    {
        edges.add( new Edge( block , this ) );
    }
    
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("EndBlock( instructions="+instructionsToString()+" , ");
        
        result.append(",  edges = ");
        for ( Edge edge : getEdges() ) {
            result.append( edge.toString(this) );
            result.append(",");
        }
        
        result.append( ")" );
        return result.toString();
    }
    
    @Override
    public String toSimpleString()
    {
        return "EndBlock( instructions = "+instructionsToString()+" )";               
    }           
}