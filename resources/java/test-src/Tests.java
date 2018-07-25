public class Tests {

    /**
     * Test controlflow's ability to handle empty blocks
     */
    public int emptyBlock(int a) {
        if (a < 0) ;
        if (a > 0) {}
        return a;
    }

    public int emptyBlockWithSideEffects(int a) {
        if (++a < 0) {

        } if (++a > 0) {

        }
        return a;
    }
    /**
     * Test shortcircuiting and control flow graphs
     */
    public boolean tripleAnd(boolean b1, boolean b2, boolean b3, boolean b4) {
        return b1 && b2 && b3 && b4;
    }
}