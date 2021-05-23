package sortpom.parameter;

/** Contains all parameters that are sent to the plugin */
public class PluginParameters {
    public final String schemaFile;
    public final boolean createBackupFile;
    public final String backupFileExtension;

    public PluginParameters(String schemaFile, boolean createBackupFile, String backupFileExtension) {
        this.schemaFile = schemaFile;
        this.createBackupFile = createBackupFile;
        this.backupFileExtension = backupFileExtension;
    }

    public static Builder builder() {
        return new PluginParameters.Builder();
    }

    /** Builder for the PluginParameters class */
    public static class Builder {
        private String schemaFile;
        private boolean createBackupFile;
        private String backupFileExtension;

        private Builder() {
        }

        /** Sets schema file location */
        public Builder setSchemaFile(String schemaFile) {
            this.schemaFile = schemaFile;
            return this;
        }

        /** Sets information regarding backup file */
        public Builder setFileOutput(final boolean createBackupFile, final String backupFileExtension) {
            this.createBackupFile = createBackupFile;
            this.backupFileExtension = backupFileExtension;
            return this;
        }

        /** Build the PluginParameters instance */
        public PluginParameters build() {
            return new PluginParameters(schemaFile, createBackupFile, backupFileExtension);
        }
    }

}
