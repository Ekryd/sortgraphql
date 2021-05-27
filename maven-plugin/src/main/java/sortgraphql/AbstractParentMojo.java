package sortgraphql;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Common parent for SortMojo
 */
abstract class AbstractParentMojo extends AbstractMojo {

    /**
     * Location of the graphql schema file that should be sorted.
     */
    @Parameter(property = "sortgraphql.schemaFile", defaultValue = "src/main/resources/schema.graphqls")
    protected File schemaFile;
    
    /**
     * Should a backup copy be created for the sorted schema.
     */
    @Parameter(property = "sortgraphql.createBackupFile", defaultValue = "true")
    protected boolean createBackupFile;

    /**
     * Name of the file extension for the backup file.
     */
    @Parameter(property = "sortgraphql.backupFileExtension", defaultValue = ".bak")
    protected String backupFileExtension;

    /**
     * Encoding for the files.
     */
    @Parameter(property = "sortgraphql.encoding", defaultValue = "UTF-8")
    protected String encoding;

    /**
     * Set this to 'true' to bypass SortGraphQL plugin
     */
    @Parameter(property = "sortgraphql.skip", defaultValue = "false")
    private boolean skip;

    final SorterImpl sorter = new SorterImpl();

    /**
     * Execute plugin.
     *
     * @throws org.apache.maven.plugin.MojoFailureException exception that will be handled by plugin framework
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoFailureException {
        if (skip) {
            getLog().info("Skipping SortGraphQL");
        } else {
            setup();
            sortGraphQLSchema();
        }

    }

    protected abstract void sortGraphQLSchema() throws MojoFailureException;

    protected abstract void setup() throws MojoFailureException;
}
