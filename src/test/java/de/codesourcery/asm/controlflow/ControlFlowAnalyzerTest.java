package de.codesourcery.asm.controlflow;

import de.codesourcery.asm.util.ASMUtil;
import de.codesourcery.misc.TestingUtil;
import jdk.internal.org.objectweb.asm.tree.analysis.Analyzer;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.File;
import java.io.FileNotFoundException;

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
                System.out.println("Constructing graph for " + mn.name);
                final ControlFlowGraph graph = analyzer.analyze("Tests", mn);
                System.out.println(graph);
            }
        }
    }

    public static boolean isConstructor(MethodNode m) {
        System.out.println("name: " + m.name);
        return "<init>".equals(m.name);
    }
}