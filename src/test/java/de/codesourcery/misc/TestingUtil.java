package de.codesourcery.misc;

public class TestingUtil {
    public static final String ROOT = System.getProperty("user.dir");
    public static final String RESOURCES = ROOT + "/resources";
    public static final String TEST_SRC = RESOURCES + "/java/test-src";
    public static final String TEST_CLASSES = RESOURCES + "/java/test-classes";
    public static final String OUT = ROOT + "/out";
    public static String getTestClass(String className) {
        return TEST_CLASSES + "/" + className;
    }
}
