package sortgraphql.parameter;

import java.io.File;

/** Contains all parameters that are sent to the plugin */
public class PluginParameters {
  public final File schemaFile;
  public final boolean createBackupFile;
  public final String backupFileExtension;
  public final String encoding;
  public final boolean skipUnionTypeSorting;
  public final boolean skipFieldArgumentSorting;

  public PluginParameters(
      File schemaFile,
      boolean createBackupFile,
      String backupFileExtension,
      String encoding,
      boolean skipUnionTypeSorting,
      boolean skipFieldArgumentSorting) {
    this.schemaFile = schemaFile;
    this.createBackupFile = createBackupFile;
    this.backupFileExtension = backupFileExtension;
    this.encoding = encoding;
    this.skipUnionTypeSorting = skipUnionTypeSorting;
    this.skipFieldArgumentSorting = skipFieldArgumentSorting;
  }

  public static Builder builder() {
    return new PluginParameters.Builder();
  }

  /** Builder for the PluginParameters class */
  public static class Builder {
    private File schemaFile;
    private boolean createBackupFile;
    private String backupFileExtension;
    private String encoding;
    private boolean skipUnionTypeSorting;
    private boolean skipFieldArgumentSorting;

    private Builder() {}

    /** Sets schema file location */
    public Builder setSchemaFile(File schemaFile) {
      this.schemaFile = schemaFile;
      return this;
    }

    /** Sets information regarding backup file */
    public Builder setFileOutput(final boolean createBackupFile, final String backupFileExtension) {
      this.createBackupFile = createBackupFile;
      this.backupFileExtension = backupFileExtension;
      return this;
    }

    /** Sets which encoding should be used throughout the plugin */
    public Builder setEncoding(final String encoding) {
      this.encoding = encoding;
      return this;
    }

    /** Sets sorting options */
    public Builder setSorting(boolean skipUnionTypeSorting, boolean skipFieldArgumentSorting) {
      this.skipUnionTypeSorting = skipUnionTypeSorting;
      this.skipFieldArgumentSorting = skipFieldArgumentSorting;
      return this;
    }

    /** Build the PluginParameters instance */
    public PluginParameters build() {
      return new PluginParameters(
          schemaFile,
          createBackupFile,
          backupFileExtension,
          encoding,
          skipUnionTypeSorting,
          skipFieldArgumentSorting);
    }
  }
}
