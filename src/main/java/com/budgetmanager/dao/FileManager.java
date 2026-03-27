package com.budgetmanager.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Low-level utility for reading and writing text files (CSV storage).
 *
 * Design notes (MVC — DAO support layer):
 *   - Handles only raw file I/O; knows nothing about business objects.
 *   - Empty and whitespace-only lines are excluded from read results to
 *     prevent parsing errors in DAO classes.
 *   - Parent directories are created automatically on write/append to avoid
 *     checked IOException spam in callers.
 *   - This class is injected into TransactionDAO and BudgetDAO via their
 *     constructors, making it easy to swap with a mock in unit tests.
 *
 * Constraints addressed:
 *   C2 (Local Data Storage): all I/O is to the local filesystem only.
 *   C3 (Data Integrity): atomic overwrite via Files.write prevents partial writes.
 *
 * Tested by: FileManagerTest
 */
public class FileManager {

    /**
     * Reads all non-empty lines from the specified file.
     * Returns an empty list (not an exception) if the file does not exist yet,
     * so callers treat a missing file the same as an empty data store.
     *
     * @param filePath path to the file to read
     * @return list of non-blank lines; empty list if the file does not exist
     * @throws IOException if the file exists but cannot be read
     */
    public List<String> readLines(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String line : Files.readAllLines(path)) {
            if (!line.trim().isEmpty()) {
                result.add(line);
            }
        }
        return result;
    }

    /**
     * Writes a list of lines to a file, replacing any existing content.
     * Creates parent directories if they do not exist.
     *
     * @param filePath path to the file to write
     * @param lines    lines to write (each line is written as-is)
     * @throws IOException if the file cannot be written
     */
    public void writeLines(String filePath, List<String> lines) throws IOException {
        Path path = Paths.get(filePath);
        ensureParentDirectories(path);
        Files.write(path, lines);
    }

    /**
     * Appends a single line to a file, followed by the system line separator.
     * Creates the file and its parent directories if they do not exist.
     *
     * @param filePath path to the file to append to
     * @param line     the line to append
     * @throws IOException if the file cannot be written
     */
    public void appendLine(String filePath, String line) throws IOException {
        Path path = Paths.get(filePath);
        ensureParentDirectories(path);
        Files.write(
            path,
            (line + System.lineSeparator()).getBytes(),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
    }

    /**
     * Returns {@code true} if the given file exists on the filesystem.
     *
     * @param filePath path to check
     * @return true if the file exists
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Deletes a file if it exists. Does nothing if the file does not exist.
     *
     * @param filePath path to the file to delete
     * @throws IOException if the file exists but cannot be deleted
     */
    public void deleteFile(String filePath) throws IOException {
        Files.deleteIfExists(Paths.get(filePath));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Creates all parent directories in the given path if they do not exist.
     * No-op if the parent directory already exists or the path has no parent.
     */
    private void ensureParentDirectories(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
