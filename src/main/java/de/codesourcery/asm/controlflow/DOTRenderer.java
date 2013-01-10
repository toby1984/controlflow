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

import de.codesourcery.asm.controlflow.Edge.EdgeType;

/**
 * Crude DOT (graphviz) renderer to control-flow graphs.
 * 
 * @author tobias.gierke@code-sourcery.de
 * 
 * @see ControlFlowGraph
 * @see ControlFlowAnalyzer
 */
public class DOTRenderer
{
    public String render(ControlFlowGraph graph) 
    {
        final StringBuilder result = new StringBuilder( "digraph \""+mangleNodeName( graph.getMethod().name )+"()\" {\n" );
        
        // enumerate vertices
        for ( IBlock block : graph.getAllNodes() ) 
        {
            String label;
            String shape="ellipse";
            if ( block instanceof MethodEntry ) {
                label = "method_entry";
            } else if ( block instanceof MethodExit ) {
                label = "method_exit";
            } 
            else 
            {
                if ( block.isVirtual( graph.getMethod() ) ) {
                    continue;
                }
                
                label = block.disassemble( graph.getMethod() , false , true ).replace("\n" , "\\l").replace("\"" , "\\\"");
                label += "\\l";
                shape="box";
            }
            shape = "shape="+shape;
            label="label=\""+label+"\"";
            if ( block.getId() == null ) {
                throw new IllegalArgumentException("Block "+block+" has no ID?");
            }
            result.append( "    "+mangleNodeName( block.getId() )+" ["+shape+","+label+"]\n" );
        }
        
        // enumerate edges
        for ( IBlock block : graph.getAllNodes() ) 
        {
            if ( block instanceof MethodEntry || block instanceof MethodExit || ! block.isVirtual( graph.getMethod() ) ) 
            {
                for ( Edge edge: block.getEdges() ) 
                {
                    if ( edge.isSuccessor( block ) ) 
                    {
                        final IBlock succ = edge.dst;
                        String style = "";
                        if ( edge.hasType( EdgeType.CAUGHT_EXCEPTION ) ) // exception
                        {
                            String type =(String) edge.metaData;
                            if ( type == null ) {
                                type = "ANY";
                            } else {
                                type = type.replace("/",".");
                                if ( type.startsWith("java.lang." ) ) {
                                    type = type.substring("java.lang.".length() );
                                }
                            }
                            style="[style=dotted,label=\"ex: "+type+"\"]";
                        } else if ( edge.hasType( EdgeType.TABLE_SWITCH) || edge.hasType( EdgeType.LOOKUP_SWITCH ) ) { // lookup/table switch
                            Integer key =(Integer) edge.metaData;
                            String color="";
                            if ( edge.hasType( EdgeType.LOOKUP_SWITCH ) ) {
                                color ="color=red,";
                            }
                            style="[style=dashed,"+color+"label=\"case: "+key+"\"]";                            
                        } else if ( edge.metaData != null ) {
                            style="[label=\""+edge.metaData+"\"]";     
                        }
                        result.append( "    "+mangleNodeName( block.getId() )+" -> "+mangleNodeName( succ.getId() )+" "+style+"\n" );
                    }
                }
            } 
        }
        result.append("}");
        return result.toString();
    }
    
    private static final String mangleNodeName(String id) {
        return id.replace("<", "" ).replace(">", "");
    }
}