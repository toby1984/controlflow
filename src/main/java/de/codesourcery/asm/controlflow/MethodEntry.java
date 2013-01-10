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