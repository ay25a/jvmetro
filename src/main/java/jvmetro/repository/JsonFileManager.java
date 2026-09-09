package jvmetro.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class JsonFileManager extends FileManager {
  public JsonFileManager() throws IOException {
    super();
  }

  @Override
  public Path getFilePath(String fileName) {
    return super.getFilePath(fileName + ".json");
  }

  @Override
  public ArrayList<HashMap<String, String>> readData(String fileName)
      throws IOException, FileProcessingException {

    String content = readFile(getFilePath(fileName)).trim();
    ArrayList<HashMap<String, String>> result = new ArrayList<>();
    if (content.isEmpty())
      return result;

    try {
      Cursor cursor = new Cursor(content);
      cursor.skipWhitespace();
      cursor.expect('[');
      cursor.skipWhitespace();

      if (cursor.peek() == ']') {
        cursor.advance(); // empty array
        return result;
      }

      while (true) {
        cursor.skipWhitespace();
        result.add(parseObject(cursor));
        cursor.skipWhitespace();

        char c = cursor.next();
        if (c == ',') {
          continue;
        } else if (c == ']') {
          break;
        } else {
          throw new IllegalStateException("Expected ',' or ']' but found '" + c + "'");
        }
      }
    } catch (IllegalStateException e) {
      throw new FileProcessingException("Failed to parse JSON file: " + e.getMessage());
    }

    return result;
  }

  private HashMap<String, String> parseObject(Cursor cursor) {
    HashMap<String, String> row = new HashMap<>();

    cursor.expect('{');
    cursor.skipWhitespace();

    if (cursor.peek() == '}') {
      cursor.advance();
      return row;
    }

    while (true) {
      cursor.skipWhitespace();
      String key = parseJsonString(cursor);
      cursor.skipWhitespace();
      cursor.expect(':');
      cursor.skipWhitespace();
      String value = parseJsonString(cursor);
      row.put(key, value);
      cursor.skipWhitespace();

      char c = cursor.next();
      if (c == ',') {
        continue;
      } else if (c == '}') {
        break;
      } else {
        throw new IllegalStateException("Expected ',' or '}' but found '" + c + "'");
      }
    }

    return row;
  }

  private String parseJsonString(Cursor cursor) {
    cursor.expect('"');
    StringBuilder sb = new StringBuilder();

    while (true) {
      char c = cursor.next();

      if (c == '"') {
        break;
      }

      if (c == '\\') {
        char escaped = cursor.next();
        switch (escaped) {
          case '"':
            sb.append('"');
            break;
          case '\\':
            sb.append('\\');
            break;
          case '/':
            sb.append('/');
            break;
          case 'n':
            sb.append('\n');
            break;
          case 'r':
            sb.append('\r');
            break;
          case 't':
            sb.append('\t');
            break;
          case 'b':
            sb.append('\b');
            break;
          case 'f':
            sb.append('\f');
            break;
          case 'u':
            String hex = cursor.nextChars(4);
            sb.append((char) Integer.parseInt(hex, 16));
            break;
          default:
            sb.append(escaped);
        }
      } else {
        sb.append(c);
      }
    }

    return sb.toString();
  }

  private static class Cursor {
    private final String text;
    private int pos = 0;

    Cursor(String text) {
      this.text = text;
    }

    void skipWhitespace() {
      while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
        pos++;
      }
    }

    char peek() {
      if (pos >= text.length()) {
        throw new IllegalStateException("Unexpected end of JSON input");
      }
      return text.charAt(pos);
    }

    char next() {
      char c = peek();
      pos++;
      return c;
    }

    void advance() {
      pos++;
    }

    void expect(char expected) {
      char c = next();
      if (c != expected) {
        throw new IllegalStateException("Expected '" + expected + "' but found '" + c + "' at position " + (pos - 1));
      }
    }

    String nextChars(int count) {
      String s = text.substring(pos, pos + count);
      pos += count;
      return s;
    }
  }

  @Override
  public void writeData(String fileName, ArrayList<HashMap<String, String>> data) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("[\n");

    for (int i = 0; i < data.size(); i++) {
      HashMap<String, String> row = data.get(i);
      sb.append("  {\n");

      int fieldIndex = 0;
      for (String key : row.keySet()) {
        sb.append("    \"").append(escapeJson(key)).append("\": \"").append(escapeJson(row.get(key))).append("\"");
        fieldIndex++;
        if (fieldIndex < row.size()) {
          sb.append(",");
        }
        sb.append("\n");
      }

      sb.append("  }");
      if (i < data.size() - 1) {
        sb.append(",");
      }
      sb.append("\n");
    }

    sb.append("]");

    writeToFile(getFilePath(fileName), sb.toString());
  }

  private String escapeJson(String value) {
    if (value == null) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        default:
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }
}
