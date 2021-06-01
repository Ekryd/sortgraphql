package sortgraphql.util;

import org.apache.commons.io.IOUtils;
import sortgraphql.exception.FailureException;
import sortgraphql.parameter.PluginParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;

/** Used to interface with file system */
public class FileUtil {
  private String backupFileExtension;
  private String encoding;

  /** Initializes the class with plugin parameters. */
  public void setup(PluginParameters parameters) {
    this.backupFileExtension = parameters.backupFileExtension;
    this.encoding = parameters.encoding;
  }

  /**
   * Loads the schema file that will be sorted.
   *
   * @return Content of the file
   */
  public String getSchemaContent(File schemaFile) {
    String content;
    try (InputStream inputStream = new FileInputStream(schemaFile)) {
      content = IOUtils.toString(inputStream, encoding);
    } catch (UnsupportedCharsetException ex) {
      throw new FailureException("Could not handle encoding: " + encoding, ex);
    } catch (IOException ex) {
      throw new FailureException("Could not read schema file: " + schemaFile.getAbsolutePath(), ex);
    }
    return content;
  }

  /** Saves a backup of the schema file before saving. */
  public void backupFile(File schemaFile) {
    var backupFile = createBackupFileHandle(schemaFile);
    deleteExistingBackupFile(backupFile);
    createBackupFile(schemaFile, backupFile);
  }

  File createBackupFileHandle(File schemaFile) {
    var backupFilename = schemaFile.getAbsolutePath() + backupFileExtension;
    return new File(backupFilename);
  }

  private void deleteExistingBackupFile(File backupFile) {
    try {
      Files.deleteIfExists(backupFile.toPath());
    } catch (IOException e) {
      throw new FailureException(
          "Could not remove old backup file, filename: " + backupFile.getAbsolutePath(), e);
    }
  }

  private void createBackupFile(File schemaFile, File backupFile) {
    try {
      Files.copy(schemaFile.toPath(), backupFile.toPath());
    } catch (IOException e) {
      throw new FailureException(
          "Could not create backup file to filename: " + backupFile.getAbsolutePath(), e);
    }
  }

  /**
   * Saves sorted schema file.
   *
   * @param sortedSchema The content to save
   */
  public void saveSchema(String sortedSchema, File schemaFile) {
    saveFile(
        schemaFile,
        sortedSchema,
        "Could not save sorted schema file: " + schemaFile.getAbsolutePath());
  }

  private void saveFile(File fileToSave, String content, String errorMessage) {
    try {
      Files.createDirectories(fileToSave.getParentFile().toPath());
      Files.write(fileToSave.toPath(), content.getBytes(encoding));
    } catch (IOException e) {
      throw new FailureException(errorMessage, e);
    }
  }
}
