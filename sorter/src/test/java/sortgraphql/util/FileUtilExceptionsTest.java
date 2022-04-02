package sortgraphql.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import refutils.ReflectionHelper;
import sortgraphql.exception.FailureException;
import sortgraphql.parameter.PluginParameters;

class FileUtilExceptionsTest {

  private File backupFileTemp;
  private File schemaFileTemp;

  @BeforeEach
  void setup() throws IOException {
    schemaFileTemp = File.createTempFile("pom", ".xml", new File("target"));
    schemaFileTemp.deleteOnExit();
    backupFileTemp = File.createTempFile("backupFile", ".xml", new File("target"));
    backupFileTemp.deleteOnExit();
  }

  @Test
  void whenOldBackupFileCannotBeDeletedShouldThrowException() {
    FileUtil fileUtil = createFileUtil();
    doNotAccessRealBackupFile(fileUtil);

    final Executable testMethod = () -> fileUtil.backupFile(schemaFileTemp);

    final FailureException thrown = assertThrows(FailureException.class, testMethod);

    assertThat(
        "Unexpected message",
        thrown.getMessage(),
        is(
            equalTo(
                "Could not remove old backup file, filename: "
                    + backupFileTemp.getParentFile().getAbsolutePath())));
  }

  private void doNotAccessRealBackupFile(FileUtil fileUtil) {
    // Set backup file to a directory (which raises DirectoryNotEmptyException)
    when(fileUtil.createBackupFileHandle(schemaFileTemp))
        .thenReturn(backupFileTemp.getParentFile());
  }

  @Test
  void whenSourceFileCannotBeCopiedShouldThrowException() {
    assertTrue(schemaFileTemp.delete());

    FileUtil fileUtil = createFileUtil();
    final Executable testMethod = () -> fileUtil.backupFile(schemaFileTemp);

    final FailureException thrown = assertThrows(FailureException.class, testMethod);

    assertThat(
        "Unexpected message",
        thrown.getMessage(),
        is(
            equalTo(
                "Could not create backup file to filename: "
                    + schemaFileTemp.getAbsolutePath()
                    + ".bak")));
  }

  @Test
  void whenSchemaFileCannotBeReadShouldThrowException() {
    assertTrue(schemaFileTemp.delete());

    FileUtil fileUtil = createFileUtil();

    final Executable testMethod = () -> fileUtil.getSchemaContent(schemaFileTemp);

    final FailureException thrown = assertThrows(FailureException.class, testMethod);

    assertThat(
        "Unexpected message",
        thrown.getMessage(),
        is(equalTo("Could not read schema file: " + schemaFileTemp.getAbsolutePath())));
  }

  @Test
  void whenFileHasWrongEncodingShouldThrowException() {
    FileUtil fileUtil = createFileUtil();

    new ReflectionHelper(fileUtil).setField("encoding", "gurka-2000");

    final Executable testMethod = () -> fileUtil.getSchemaContent(schemaFileTemp);

    final FailureException thrown = assertThrows(FailureException.class, testMethod);

    assertThat(
        "Unexpected message",
        thrown.getMessage(),
        is(equalTo("Could not handle encoding: gurka-2000")));
  }

  @Test
  void whenPomFileCannotBeSavedShouldThrowException() {
    assertTrue(schemaFileTemp.setReadOnly());

    FileUtil fileUtil = createFileUtil();

    final Executable testMethod = () -> fileUtil.saveSchema("Whatever", schemaFileTemp);

    final FailureException thrown = assertThrows(FailureException.class, testMethod);

    assertThat(
        "Unexpected message",
        thrown.getMessage(),
        is(equalTo("Could not save sorted schema file: " + schemaFileTemp.getAbsolutePath())));
    assertTrue(schemaFileTemp.setReadable(true));
  }

  private FileUtil createFileUtil() {
    FileUtil originalFileUtil = new FileUtil();
    originalFileUtil.setup(
        PluginParameters.builder().setEncoding("UTF-8").setBackup(true, ".bak").build());

    return spy(originalFileUtil);
  }
}
