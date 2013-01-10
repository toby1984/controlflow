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
package de.codesourcery.asm.util;

import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

/**
 * Disassembles a single {@link AbstractInsnNode} node.
 * 
 * @author tobias.gierke@code-sourcery.de
 */
public class Disassembler
{
	/**
	 * Disassemble a method.
	 * 
	 * @param method method to disassemble
	 * @param includeVirtual whether to 'disassemble' virtual (ASM-generated) nodes that
     * have no equivalent in .class files
	 * @param printInsnIndices
     * @param printInsnIndices whether to output the instruction index in front of the mnemonic	 
	 * @return disassembled method
	 */
	public static String disassemble(MethodNode method,boolean includeVirtual,boolean printInsnIndices) {
	
		final StringBuilder result = new StringBuilder();
		@SuppressWarnings("unchecked")
		final ListIterator<AbstractInsnNode> it = method.instructions.iterator();
		while ( it.hasNext() ) {
			AbstractInsnNode node = it.next();
			String line = disassemble(node,method,includeVirtual , printInsnIndices );
			if ( line == null ) {
				continue;
			}
			
			if ( result.length() > 0 ) {
				result.append("\n");
			}
			result.append( line );
		}
		return result.toString();
	}
	
    /**
     * Disassemble a single {@link AbstractInsnNode} node.
     * 
     * @param node the node to disassemble
     * @param method the method this node comes from
     * @param includeVirtual whether to 'disassemble' virtual (ASM-generated) nodes that
     * have no equivalent in .class files
     * @param printInsnIndices whether to output the instruction index in front of the mnemonic
     * @return disassembled instruction or <code>null</code> if the node does not map 
     * to a bytecode (=is virtual) and the <code>includeVirtual</code> flag was <code>false</code>
     */
    public static String disassemble(AbstractInsnNode node,MethodNode method,boolean includeVirtual,boolean printInsnIndices) 
    {
        final int opCode = node.getOpcode();
        final String mnemonic;
        if ( opCode < 0 || opCode >= Printer.OPCODES.length ) 
        {
            if ( ! includeVirtual ) {
                return null;
            } 
          	mnemonic = "// "+node.getClass().getSimpleName();
        } else {
        	mnemonic = disassemble(node,method);
        }
        
        if ( printInsnIndices ) {
        	final int indexOf = method.instructions.indexOf( node );
        	String index = Integer.toString( indexOf );
        	if ( index.length() < 4 ) {
        		index = StringUtils.leftPad(index, 4 );
        	}
        	return index+": "+mnemonic;
        }
        return mnemonic;
    }

    private static String disassemble(AbstractInsnNode node,MethodNode method) 
    {
        final int opCode = node.getOpcode();
        String mnemonic = Printer.OPCODES[opCode];

        switch( node.getType() ) 
        {
            case AbstractInsnNode.FIELD_INSN: // GETSTATIC, PUTSTATIC, GETFIELD , PUTFIELD
            FieldInsnNode tmp = (FieldInsnNode) node;
            mnemonic += " "+ ( tmp.owner+"#"+tmp.name );
            break;
            case AbstractInsnNode.IINC_INSN: // IINC
                IincInsnNode tmp2 = (IincInsnNode) node;
                mnemonic += " "+ ( tmp2.var+" , "+tmp2.incr );
                break;
            case AbstractInsnNode.INSN: // regular opcodes
                break;
            case AbstractInsnNode.INT_INSN: // BIPUSH, SIPUSH or NEWARRAY
                IntInsnNode tmp3 = (IntInsnNode) node;
                mnemonic += " "+ ( tmp3.operand );
                break;
            case AbstractInsnNode.INVOKE_DYNAMIC_INSN: // INVOKEDYNAMIC
                break;
            case AbstractInsnNode.JUMP_INSN: // IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE,IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL
                JumpInsnNode tmp4 = (JumpInsnNode) node;
                int index = method.instructions.indexOf( tmp4.label );
                while ( method.instructions.get( index ).getOpcode() == -1 ) {
                	index++;
                }
                mnemonic += " "+ index;
                break;
            case AbstractInsnNode.LDC_INSN: // load constant
                LdcInsnNode tmp5 = (LdcInsnNode) node;
                Class<?> clazz = tmp5.cst.getClass();
                if ( clazz == String.class ) {
                    mnemonic += " \""+ tmp5.cst+"\"";
                } else if ( clazz == org.objectweb.asm.Type.class ) {
                    org.objectweb.asm.Type type = (org.objectweb.asm.Type) tmp5.cst;
                    mnemonic += " (a "+type.getClassName()+")";
                } else {
                    mnemonic += " "+ tmp5.cst+" ("+tmp5.cst.getClass().getName()+")";
                }
                break;
            case AbstractInsnNode.LOOKUPSWITCH_INSN: // LOOKUPSWITCH
                break;
            case AbstractInsnNode.METHOD_INSN: // INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC , INVOKEINTERFACE
                MethodInsnNode tmp6 = (MethodInsnNode) node;
                mnemonic += " "+ ( tmp6.owner+"#"+tmp6.name+"()" );
                break;
            case AbstractInsnNode.MULTIANEWARRAY_INSN: // MULTIANEWARRAY
                break;
            case AbstractInsnNode.TABLESWITCH_INSN: // TABLESWITCH
                break;
            case AbstractInsnNode.TYPE_INSN: // NEW, ANEWARRAY, CHECKCAST , INSTANCEOF
                TypeInsnNode tmp8 = (TypeInsnNode) node;
                mnemonic += " "+tmp8.desc;
                break;
            case AbstractInsnNode.VAR_INSN: // ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE , RET
                VarInsnNode tmp7 = (VarInsnNode) node;
                mnemonic += "_"+tmp7.var;
                break;
                // -- VIRTUAL --
            case AbstractInsnNode.FRAME: /* VIRTUAL */
            case AbstractInsnNode.LABEL: /* VIRTUAL */
            case AbstractInsnNode.LINE: /* VIRTUAL */
            default:
                throw new RuntimeException("Internal error, unhandled node type: "+node);
        }
        return mnemonic;
    }    

}
