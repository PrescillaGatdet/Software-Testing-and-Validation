package com.budgetmanager.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileManager.
 *
 * All tests use a temporary directory created by JUnit so they do not touch
 * the real data/ folder or leave artefacts behind.
 *
 * Covers:
 *   - readLines from non-existent file → empty list
 *   - writeLines then readLines → round-trip equality
 *   - appendLine → lines accumulate in order
 *   - writeLines overwrites existing content
 *   - fileExists before and after file creation
 *   - deleteFile removes the file; no-op on missing file
 *   - Empty/whitespace lines are excluded from readLines output
 *   - Parent directories are created automatically on first write
 */
@DisplayName("FileManager — Unit Tests")
class FileManagerTest {

    private FileManager fileManager;
    private Path tempDir;
    private String testFile;

    @BeforeEach
    void setUp() throws IOException {
        fileManager = new FileManager();
        tempDir     = Files.createTempDirectory("fm-test-");
        testFile    = tempDir.resolve("test.csv").toString();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Delete all files in tempDir then the directory itself
        Files.walk(tempDir)
             .sorted((a, b) -> b.compareTo(a)) // children before parent
             .forEach(p -> p.toFile().delete());
    }

    // =========================================================================
    // readLines
    // =========================================================================

    @Test
    @DisplayName("readLines returns empty list when file does not exist")
    void testReadNonExistentFile() throws IOException {
        List<String> lines = fileManager.readLines(testFile);
        assertNotNull(lines);
        assertTrue(lines.isEmpty());
    }

    @Test
    @DisplayName("readLines returns correct lines after writeLines")
    void testReadAfterWrite() throws IOException {
        List<String> original = Arrays.asList("header,col1,col2", "row1,val1,val2");
        fileManager.writeLines(testFile, original);
        List<String> result = fileManager.readLines(testFile);
        assertEquals(original, result);
    }

    @Test
    @DisplayName("readLines skips empty lines")
    void testReadSkipsEmptyLines() throws IOException {
        fileManager.writeLines(testFile, Arrays.asList("line1", "", "line2", "   "));
        List<String> result = fileManager.readLines(testFile);
        assertEquals(2, result.size());
        assertEquals("line1", result.get(0));
        assertEquals("line2", result.get(1));
    }

    // =========================================================================
    // writeLines
    // =========================================================================

    @Test
    @DisplayName("writeLines overwrites existing file content")
    void testWriteOverwritesExisting() throws IOException {
        fileManager.writeLines(testFile, Arrays.asList("old1", "old2"));
        fileManager.writeLines(testFile, Arrays.asList("new1"));
        List<String> result = fileManager.readLines(testFile);
        assertEquals(1, result.size());
        assertEquals("new1", result.get(0));
    }

    @Test
    @DisplayName("writeLines creates parent directories automatically")
    void testWriteCreatesParentDirs() throws IOException {
        String nested = tempDir.resolve("a/b/c/file.csv").toString();
        fileManager.writeLines(nested, Arrays.asList("data"));
        assertTrue(fileManager.fileExists(nested));
    }

    // =========================================================================
    // appendLine
    // =========================================================================

    @Test
    @DisplayName("appendLine creates a new file and adds the first line")
    void testAppendCreatesFile() throws IOException {
        fileManager.appendLine(testFile, "first");
        List<String> result = fileManager.readLines(testFile);
        assertEquals(1, result.size());
        assertEquals("first", result.get(0));
    }

    @Test
    @DisplayName("appendLine adds lines in the order they are appended")
    void testAppendOrder() throws IOException {
        fileManager.appendLine(testFile, "line1");
        fileManager.appendLine(testFile, "line2");
        fileManager.appendLine(testFile, "line3");
        List<String> result = fileManager.readLines(testFile);
        assertEquals(3, result.size());
        assertEquals("line1", result.get(0));
        assertEquals("line2", result.get(1));
        assertEquals("line3", result.get(2));
    }

    @Test
    @DisplayName("appendLine creates parent directories automatically")
    void testAppendCreatesParentDirs() throws IOException {
        String nested = tempDir.resolve("sub/dir/data.csv").toString();
        fileManager.appendLine(nested, "row");
        assertTrue(fileManager.fileExists(nested));
    }

    // =========================================================================
    // fileExists
    // =========================================================================

    @Test
    @DisplayName("fileExists returns false for a path that has never been written")
    void testFileExistsFalse() {
        assertFalse(fileManager.fileExists(testFile));
    }

    @Test
    @DisplayName("fileExists returns true after the file is created")
    void testFileExistsTrue() throws IOException {
        fileManager.appendLine(testFile, "x");
        assertTrue(fileManager.fileExists(testFile));
    }

    // =========================================================================
    // deleteFile
    // =========================================================================

    @Test
    @DisplayName("deleteFile removes an existing file")
    void testDeleteExistingFile() throws IOException {
        fileManager.appendLine(testFile, "data");
        assertTrue(fileManager.fileExists(testFile));
        fileManager.deleteFile(testFile);
        assertFalse(fileManager.fileExists(testFile));
    }

    @Test
    @DisplayName("deleteFile on a non-existent file does not throw")
    void testDeleteNonExistentFileNoThrow() {
        assertDoesNotThrow(() -> fileManager.deleteFile(testFile));
    }

    // =========================================================================
    // Content integrity
    // =========================================================================

    @Test
    @DisplayName("Write then read preserves special characters and spaces")
    void testSpecialCharacters() throws IOException {
        List<String> lines = Arrays.asList(
            "id,100.00,2026-03-01,Grocery store,FOOD,EXPENSE",
            "id2,50.00,2026-03-02,\"Lunch, with friend\",FOOD,EXPENSE"
        );
        fileManager.writeLines(testFile, lines);
        List<String> result = fileManager.readLines(testFile);
        assertEquals(lines, result);
    }
}
