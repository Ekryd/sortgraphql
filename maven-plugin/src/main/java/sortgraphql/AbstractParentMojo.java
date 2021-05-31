package sortgraphql;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

/** Common parent for SortMojo */
@SuppressWarnings("unused")
abstract class AbstractParentMojo extends AbstractMojo {

  final SorterImpl sorter = new SorterImpl();

  /** Location of the graphql schema file that should be sorted. If multiple, use schemaFiles. */
  @Parameter(
      property = "sortgraphql.schemaFile",
      defaultValue = "src/main/resources/schema.graphqls")
  protected File schemaFile;

  /**
   * Location of multiple graphql schema file that should be sorted. Overrides parameter schemaFile.
   * The schema files can reference each other, but shared definitions is not allowed.
   */
  @Parameter(property = "sortgraphql.schemaFiles")
  protected List<File> schemaFiles;

  /** Should a backup copy be created for the sorted schema. */
  @Parameter(property = "sortgraphql.createBackupFile", defaultValue = "true")
  protected boolean createBackupFile;

  /** Name of the file extension for the backup file. */
  @Parameter(property = "sortgraphql.backupFileExtension", defaultValue = ".bak")
  protected String backupFileExtension;

  /** Encoding for the files. */
  @Parameter(property = "sortgraphql.encoding", defaultValue = "UTF-8")
  protected String encoding;

  /** Skip sorting the types in a union. */
  @Parameter(property = "sortgraphql.skipUnionTypeSorting", defaultValue = "false")
  protected boolean skipUnionTypeSorting;

  /** Skip sorting the arguments for a field in a type. */
  @Parameter(property = "sortgraphql.skipFieldArgumentSorting", defaultValue = "false")
  protected boolean skipFieldArgumentSorting;

  /** Set this to 'true' to bypass SortGraphQL plugin */
  @Parameter(property = "sortgraphql.skip", defaultValue = "false")
  private boolean skip;

  /**
   * Execute plugin.
   *
   * @throws org.apache.maven.plugin.MojoFailureException exception that will be handled by plugin
   *     framework
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

  protected abstract void setup() throws MojoFailureException;

  protected abstract void sortGraphQLSchema() throws MojoFailureException;
}
