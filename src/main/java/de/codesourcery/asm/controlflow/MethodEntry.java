package de.codesourcery.asm.controlflow;

/**
 * Control-flow graph node: Method entry.
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public class MethodEntry extends AbstractBlock 
{
    @Override
    public void addRegularSuccessor(IBlock block) {
        edges.add( new Edge( this , block ) );
    }        

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("StartBlock( instructions="+instructionsToString()+" , ");
        
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
        return "StartBlock( instructions = "+instructionsToString()+" )";            
    }          
} 