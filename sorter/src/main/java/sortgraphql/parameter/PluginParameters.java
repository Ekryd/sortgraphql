package sortgraphql.parameter;

import java.io.File;
import java.util.List;

import static java.util.Collections.singletonList;

/** Contains all parameters that are sent to the plugin */
public class PluginParameters {
  public final List<File> schemaFiles;
  public final boolean createBackupFile;
  public final String backupFileExtension;
  public final String encoding;
  public final boolean skipUnionTypeSorting;
  public final boolean skipFieldArgumentSorting;
  public final boolean generateSchemaDefinition;
  public final boolean generateAllDirectiveDefinitions;

  public PluginParameters(
      List<File> schemaFiles,
      boolean createBackupFile,
      String backupFileExtension,
      String encoding,
      boolean skipUnionTypeSorting,
      boolean skipFieldArgumentSorting, boolean generateSchemaDefinition, boolean generateAllDirectiveDefinitions) {
    this.schemaFiles = schemaFiles;
    this.createBackupFile = createBackupFile;
    this.backupFileExtension = backupFileExtension;
    this.encoding = encoding;
    this.skipUnionTypeSorting = skipUnionTypeSorting;
    this.skipFieldArgumentSorting = skipFieldArgumentSorting;
    this.generateSchemaDefinition = generateSchemaDefinition;
    this.generateAllDirectiveDefinitions = generateAllDirectiveDefinitions;
  }

  public static Builder builder() {
    return new PluginParameters.Builder();
  }

  /** Builder for the PluginParameters class */
  public static class Builder {
    private List<File> schemaFiles;
    private boolean createBackupFile;
    private String backupFileExtension;
    private String encoding;
    private boolean skipUnionTypeSorting;
    private boolean skipFieldArgumentSorting;
    private boolean generateSchemaDefinition;
    private boolean generateAllDirectiveDefinitions;

    private Builder() {}

    /** Sets schema file location */
    public Builder setSchemaFile(File schemaFile, List<File> schemaFiles) {
      if (schemaFiles == null || schemaFiles.isEmpty())
        this.schemaFiles = singletonList(schemaFile);
      else this.schemaFiles = schemaFiles;
      return this;
    }

    /** Sets information regarding backup file */
    public Builder setBackup(final boolean createBackupFile, final String backupFileExtension) {
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
    
    public Builder setGenerationOptions(boolean generateSchemaDefinition, boolean generateAllDirectiveDefinitions) {
      this.generateSchemaDefinition = generateSchemaDefinition;
      this.generateAllDirectiveDefinitions = generateAllDirectiveDefinitions;
      return this;
    }

    /** Build the PluginParameters instance */
    public PluginParameters build() {
      return new PluginParameters(
          schemaFiles,
          createBackupFile,
          backupFileExtension,
          encoding,
          skipUnionTypeSorting,
          skipFieldArgumentSorting, 
          generateSchemaDefinition,
          generateAllDirectiveDefinitions);
    }
  }
}
