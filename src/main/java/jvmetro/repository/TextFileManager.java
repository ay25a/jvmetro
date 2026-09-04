package jvmetro.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Base64;

public class TextFileManager extends FileManager {
  private final static char SEP_NAME = ':';
  private final static char SEP_FIELD = ';';
  private final static char SEP_RECORD = '\n';

  public TextFileManager() throws IOException {
    super();
  }

  @Override
  public Path getFilePath(String fileName) {
    return super.getFilePath(fileName + ".txt");
  }

  @Override
  public ArrayList<HashMap<String, String>> readData(String fileName) throws IOException, FileProcessingException {
    String rawData = readFile(getFilePath(fileName));
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();

    for (int i = 0; i < rawData.length(); ++i) {
      HashMap<String, String> record = new HashMap<>();

      while (true) {
        int keyEnd = rawData.indexOf(SEP_NAME, i);
        int valueEnd = rawData.indexOf(SEP_FIELD, i);

        if (keyEnd <= -1 || valueEnd <= -1)
          throw new FileProcessingException("Corrupted File Content at " + i);

        String key = rawData.substring(i, keyEnd);
        String value = rawData.substring(keyEnd + 1, valueEnd);

        record.put(key, new String(Base64.getDecoder().decode(value.getBytes())));

        i = valueEnd + 1;
        if (i >= rawData.length() || rawData.charAt(i) == SEP_RECORD)
          break;
      }

      parsed.add(record);
    }

    return parsed;
  }

  @Override
  public void writeData(String fileName, ArrayList<HashMap<String, String>> data) throws IOException {
    StringBuilder parsed = new StringBuilder();

    data.forEach(record -> {
      record.forEach((key, value) -> {
        parsed.append(key);
        parsed.append(SEP_NAME);
        parsed.append(Base64.getEncoder().encodeToString(value.getBytes()));
        parsed.append(SEP_FIELD);
      });

      parsed.append(SEP_RECORD);
    });

    writeToFile(getFilePath(fileName), parsed.toString());
  }
}
