package de.codesourcery.asm.controlflow;

import de.codesourcery.asm.util.ASMUtil;
import de.codesourcery.asm.misc.TestingUtil;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;

import static org.junit.Assert.*;

public class ControlFlowAnalyzerTest {
    private static File[] classPath = {new File(TestingUtil.TEST_CLASSES)};
    @Test
    public void analyze() throws Exception {
        ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();

        ClassReader reader = ASMUtil.createClassReader("Tests", classPath);
        ClassNode cn = new ClassNode();
        reader.accept(cn, 0);

        for (Object m : cn.methods) {
            MethodNode mn = (MethodNode) m;
            if (! isConstructor(mn)) {
                // TODO: Test structural properties
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

    public static boolean isConstructor(MethodNode m) {
        return "<init>".equals(m.name);
    }
}