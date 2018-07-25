package de.codesourcery.asm.controlflow;

import de.codesourcery.asm.util.ASMUtil;
import de.codesourcery.asm.misc.TestingUtil;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;

import static org.junit.Assert.*;

public class ControlFlowAnalyzerTest {
    private static File[] classPath = {new File(TestingUtil.TEST_CLASSES)};

    public static ClassNode readClass(String className) throws IOException {
        ClassReader reader = ASMUtil.createClassReader(className, classPath);
        ClassNode cn = new ClassNode();
        reader.accept(cn, 0);
        return cn;
    }

    public static void generateDOTFile(String className, ControlFlowGraph graph) throws FileNotFoundException {
        String cfg = new DOTRenderer().render(graph);
        PrintWriter pw = new PrintWriter(new FileOutputStream(
                new File(TestingUtil.RESOURCES + "/dot/" + className + ".dot")));
        pw.write(cfg);
        pw.close();
    }

    public static boolean isConstructor(MethodNode m) {
        return "<init>".equals(m.name);
    }

    @Test
    public void testTests() throws Exception {
        ClassNode cn = readClass("Tests");

       for (Object m : cn.methods) {
            MethodNode mn = (MethodNode) m;
            if (! isConstructor(mn)) {
                // TODO: Test structural properties
                ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
                final ControlFlowGraph graph = analyzer.analyze("Tests", mn);
                if ("emptyBlock".equals(mn.name)) {
                    assertEquals(5, graph.getAllNodes().size());
                }
                else if ("emptyBlockWithSideEffects".equals(mn.name)) {
                    assertEquals(5, graph.getAllNodes().size());
                } else if ("tripleAnd".equals(mn.name)) {
                    assertEquals(9, graph.getAllNodes().size());
                }
            }

        }
    }

    @Test
    public void testIf() throws Exception {
        String className = "IfTest";
        ClassNode cn = readClass(className);

       for (Object m : cn.methods) {
           MethodNode mn = (MethodNode) m;
           if (!isConstructor(mn)) {
               ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
                final ControlFlowGraph graph = analyzer.analyze(className, mn);

                if ("f".equals(mn.name)) {
                    generateDOTFile(className, graph);
                    assertEquals(5, graph.getAllNodes().size());

                    assertEquals(1, graph.getStart().getRegularSuccessorCount());
                    assertEquals(2, graph.getEnd().getRegularPredecessorCount());

                    IBlock firstBlock = graph.getStart().getRegularSuccessor();
                    assertEquals(2, firstBlock.getRegularSuccessorCount());
                }
           }
       }
    }

    @Test
    public void testTriangle127() throws Exception {
        String className = "Triangle127";
        ClassNode cn = readClass(className);

        for (Object m : cn.methods) {
            MethodNode mn = (MethodNode) m;
            if (!isConstructor(mn)) {
                ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
                final ControlFlowGraph graph = analyzer.analyze(className, mn);

                if ("classify".equals(mn.name)) {
                    generateDOTFile(className, graph);

                    assertEquals(1, graph.getStart().getRegularSuccessorCount());
                    assertEquals(7, graph.getEnd().getRegularPredecessorCount());
                }
            }
        }
    }
}