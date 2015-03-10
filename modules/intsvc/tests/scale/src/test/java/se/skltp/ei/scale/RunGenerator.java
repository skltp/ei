package se.skltp.ei.scale;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class RunGenerator {

    @Test
    public void generateFiles() {
        Generator so = new Generator();
        so.generateTestFiles();
        assertTrue(Files.isRegularFile(Paths.get("data/test4.create5.xml")));
    }
}
