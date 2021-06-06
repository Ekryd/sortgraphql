package sortgraphql;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import sortgraphql.exception.ExceptionConverter;
import sortgraphql.logger.MavenLogger;
import sortgraphql.parameter.PluginParameters;

/** Sorts a GraphQL Schema definition file. */
@Mojo(name = "sort", threadSafe = true, defaultPhase = LifecyclePhase.VALIDATE)
public class SortMojo extends AbstractParentMojo {

  @Override
  public void setup() throws MojoFailureException {
    new ExceptionConverter(
            () -> {
              var pluginParameters =
                  PluginParameters.builder()
                      .setSchemaFile(schemaFile, schemaFiles)
                      .setBackup(createBackupFile, backupFileExtension)
                      .setEncoding(encoding)
                      .setSorting(skipUnionTypeSorting, skipFieldArgumentSorting)
                      .setGenerationOptions(generateSchemaDefinition, false, generateHashDescriptions)
                      .build();

              sorter.setup(new MavenLogger(getLog()), pluginParameters);
            })
        .executeAndConvertException();
  }

  @Override
  protected void sortGraphQLSchema() throws MojoFailureException {
    new ExceptionConverter(sorter::sortSchemas).executeAndConvertException();
  }
}
