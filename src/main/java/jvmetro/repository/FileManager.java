package jvmetro.repository;

import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class FileManager {
  private static final Path STORAGE_DIR_PATH = Path.of("storage_data").toAbsolutePath();

  public FileManager() throws IOException {
    if (!Files.exists(STORAGE_DIR_PATH))
      Files.createDirectory(STORAGE_DIR_PATH);
  }

  // Get full file path (including file extension for different FileManager).
  public Path getFilePath(String fileName) {
    return Path.of(STORAGE_DIR_PATH.toString(), fileName);
  }

  // "readFile" and "WriteToFile" provide an easy way to change file reading if
  // needed.
  protected String readFile(Path path) throws IOException {
    if (!Files.exists(path))
      Files.createFile(path);

    return Files.readString(path);
  }

  protected void writeToFile(Path path, String content) throws IOException {
    Files.writeString(path, content);
  }

  // the main interface functions to uniformally read and write data
  public abstract ArrayList<HashMap<String, String>> readData(String fileName)
      throws IOException, FileProcessingException;

  public abstract void writeData(String fileName, ArrayList<HashMap<String, String>> data) throws IOException;
}
