package jvmetro.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class XMLFileManager extends FileManager {
  private static final String ROOT_OPEN = "<data>";
  private static final String ROOT_CLOSE = "</data>";
  private static final String RECORD_OPEN = "<record>";
  private static final String RECORD_CLOSE = "</record>";

  public XMLFileManager() throws IOException {
    super();
  }

  @Override
  public Path getFilePath(String fileName) {
    return super.getFilePath(fileName + ".xml");
  }

  @Override
  public ArrayList<HashMap<String, String>> readData(String fileName)
      throws IOException, FileProcessingException {

    String content = readFile(getFilePath(fileName)).trim();
    ArrayList<HashMap<String, String>> result = new ArrayList<>();
    if (content.isEmpty())
      return result;

    try {
      int dataStart = content.indexOf(ROOT_OPEN);
      int dataEnd = content.lastIndexOf(ROOT_CLOSE);

      if (dataStart == -1 || dataEnd == -1) {
        throw new IllegalStateException("Missing <data> root element");
      }

      String body = content.substring(dataStart + ROOT_OPEN.length(), dataEnd);

      int pos = 0;
      while (true) {
        int recordStart = body.indexOf(RECORD_OPEN, pos);
        if (recordStart == -1) {
          break;
        }
        int recordContentStart = recordStart + RECORD_OPEN.length();
        int recordEnd = body.indexOf(RECORD_CLOSE, recordContentStart);
        if (recordEnd == -1) {
          throw new IllegalStateException("Unclosed <record> element");
        }

        String recordBody = body.substring(recordContentStart, recordEnd);
        result.add(parseFields(recordBody));

        pos = recordEnd + RECORD_CLOSE.length();
      }
    } catch (IllegalStateException e) {
      throw new FileProcessingException("Failed to parse XML file: " + e.getMessage());
    }

    return result;
  }

  private HashMap<String, String> parseFields(String recordBody) {
    HashMap<String, String> row = new HashMap<>();
    int pos = 0;

    while (true) {
      int openStart = recordBody.indexOf('<', pos);
      if (openStart == -1) {
        break;
      }
      int openEnd = recordBody.indexOf('>', openStart);
      if (openEnd == -1) {
        throw new IllegalStateException("Malformed tag in record");
      }

      String tagName = recordBody.substring(openStart + 1, openEnd);
      String closeTag = "</" + tagName + ">";
      int valueStart = openEnd + 1;
      int closeStart = recordBody.indexOf(closeTag, valueStart);
      if (closeStart == -1) {
        throw new IllegalStateException("Missing closing tag for <" + tagName + ">");
      }

      String rawValue = recordBody.substring(valueStart, closeStart);
      row.put(tagName, unescapeXml(rawValue));

      pos = closeStart + closeTag.length();
    }

    return row;
  }

  private String unescapeXml(String value) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (i < value.length()) {
      char c = value.charAt(i);
      if (c == '&') {
        if (value.startsWith("&amp;", i)) {
          sb.append('&');
          i += 5;
        } else if (value.startsWith("&lt;", i)) {
          sb.append('<');
          i += 4;
        } else if (value.startsWith("&gt;", i)) {
          sb.append('>');
          i += 4;
        } else if (value.startsWith("&quot;", i)) {
          sb.append('"');
          i += 6;
        } else if (value.startsWith("&apos;", i)) {
          sb.append('\'');
          i += 6;
        } else {
          sb.append(c);
          i++;
        }
      } else {
        sb.append(c);
        i++;
      }
    }
    return sb.toString();
  }

  @Override
  public void writeData(String fileName, ArrayList<HashMap<String, String>> data) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(ROOT_OPEN).append("\n");

    for (HashMap<String, String> row : data) {
      sb.append("  ").append(RECORD_OPEN).append("\n");

      for (String key : row.keySet()) {
        String tag = sanitizeTagName(key);
        sb.append("    <").append(tag).append(">")
            .append(escapeXml(row.get(key)))
            .append("</").append(tag).append(">\n");
      }

      sb.append("  ").append(RECORD_CLOSE).append("\n");
    }

    sb.append(ROOT_CLOSE);

    writeToFile(getFilePath(fileName), sb.toString());
  }

  private String escapeXml(String value) {
    if (value == null) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '&':
          sb.append("&amp;");
          break;
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '"':
          sb.append("&quot;");
          break;
        case '\'':
          sb.append("&apos;");
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  // XML element names cannot start with a digit or contain spaces
  private String sanitizeTagName(String key) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      boolean isValidChar = Character.isLetterOrDigit(c) || c == '_' || c == '-';
      sb.append(isValidChar ? c : '_');
    }

    String sanitized = sb.toString();
    if (sanitized.isEmpty() || Character.isDigit(sanitized.charAt(0))) {
      sanitized = "_" + sanitized;
    }
    return sanitized;
  }
}
