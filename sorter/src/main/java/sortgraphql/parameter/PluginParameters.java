package sortgraphql.parameter;

import java.io.File;

/** Contains all parameters that are sent to the plugin */
public class PluginParameters {
    public final File schemaFile;
    public final boolean createBackupFile;
    public final String backupFileExtension;
    public final String encoding;

    public PluginParameters(File schemaFile, boolean createBackupFile, String backupFileExtension, String encoding) {
        this.schemaFile = schemaFile;
        this.createBackupFile = createBackupFile;
        this.backupFileExtension = backupFileExtension;
        this.encoding = encoding;
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

        private Builder() {
        }

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

        /** Build the PluginParameters instance */
        public PluginParameters build() {
            return new PluginParameters(schemaFile, createBackupFile, backupFileExtension, encoding);
        }
    }

}
