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

import org.apache.commons.lang.ObjectUtils;

/**
 * An edge of the control-flow graph.
 * 
 * @author tobias.gierke@code-sourcery.de
 * 
 * @see IBlock
 */
public final class Edge
{
    public final IBlock src;
    public final IBlock dst;
    public final EdgeType type;
    public final Object metaData;
    
    /**
     * Type of graph edge.
     * 
     * @author tobias.gierke@code-sourcery.de
     */
    public static enum EdgeType 
    {
        /**
         * Regular transition.
         * 
         * For conditional jumps, this edge type requires either the string <code>true</code> or <code>false</code> 
         * as meta-data.
         */
        REGULAR,
        /**
         * Transition because of caught exception.
         * 
         * This edge type requires the handled exception type as meta-data.
         */
        CAUGHT_EXCEPTION,
        /**
         * case of a LOOKUPSWITCH instruction.
         * 
         * This edge type requires the case label as meta-data.
         */
        LOOKUP_SWITCH,
        /**
         * case of a TABLESWITCH instruction.
         * 
         * This edge type requires the case label as meta-data.
         */        
        TABLE_SWITCH;
    }
    
    public Edge withSource(IBlock source) {
        return new Edge( source , this.dst , this.type , this.metaData );
    }
    
    public Edge withDestination(IBlock dest) {
        return new Edge( this.src , dest , this.type , this.metaData );
    }    
    
    public boolean isSuccessor(IBlock block) {
        return src == block;
    }
    
    public boolean isPredecessor(IBlock block) {
        return dst == block;
    }  
    
    public Edge(IBlock src, IBlock dst)
    {
        this(src,dst,EdgeType.REGULAR,null);
    }
    
    public Edge(IBlock src, IBlock dst,EdgeType type,Object metaData)
    {
        if ( src == null ) {
            throw new IllegalArgumentException("src must not be NULL.");
        }
        if ( dst == null ) {
            throw new IllegalArgumentException("dst must not be NULL.");
        }
        if ( type == null ) {
            throw new IllegalArgumentException("type must not be NULL.");
        }
        
        this.src = src;
        this.dst = dst;
        this.type = type;
        this.metaData = metaData;
    }
    
    public boolean hasType(EdgeType type) {
        return type.equals(this.type);
    }
    
    @Override
    public int hashCode()
    {
        int result = 31 + dst.hashCode();
        result = 31 * result + src.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + ( metaData != null ? metaData.hashCode() : 0 );
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ( obj instanceof Edge) 
        {
            final Edge that = (Edge) obj;
            return this.src.equals( that.src ) && this.dst.equals( that.dst ) && this.type == that.type && ObjectUtils.equals( this.metaData , that.metaData );
        }
        return false;
    }

    public String toString(IBlock caller)
    {
        if ( src == caller ) {
            if ( hasType(EdgeType.REGULAR) ) {
                return "Edge[ to=" + dst.toSimpleString() + "]";
            }
            return "Edge[ to=" + dst.toSimpleString() + ", type=" + type + ", metaData=" + metaData + "]";               
        } 
        
        if ( dst == caller ) {
            if ( hasType(EdgeType.REGULAR) ) {
                return "Edge[ from =" + src.toSimpleString() + " ]";
            }
            return "Edge[ from=" + src.toSimpleString() + ", type=" + type + ", metaData=" + metaData + "]";               
        } 
        return toString();
    }
    
    @Override
    public String toString()
    {
        if ( hasType(EdgeType.REGULAR) ) {
            return "Edge[src=" + src.toSimpleString() + ", dst=" + dst.toSimpleString() + "]";
        }
        return "Edge[src=" + src.toSimpleString() + ", dst=" + dst.toSimpleString() + ", type=" + type + ", metaData=" + metaData + "]";
    }
}