package sortgraphql;

import graphql.schema.GraphQLSchema;
import org.junit.jupiter.api.Test;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;
import sortgraphql.util.FileUtil;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

/** */
class SorterServiceTest {
  private final SortingLogger log = mock(SortingLogger.class);

  private final SorterService sorterService = new SorterService();
  private final FileUtil fileUtil = new FileUtil();

  @Test
  void testMultipleSchemas() {
    PluginParameters pluginParameters =
        PluginParameters.builder()
            .setSchemaFile(
                null,
                List.of(
                    new File("src/test/resources/schema.graphqls"),
                    new File("src/test/resources/mutations.graphqls")))
            .build();

    sorterService.setup(log, pluginParameters);

    List<String> contents =
        pluginParameters.schemaFiles.stream()
            .map(fileUtil::getSchemaContent)
            .collect(Collectors.toList());
    GraphQLSchema mergedSchema =
        sorterService.createMergedSchema(contents, pluginParameters.schemaFiles);

    String sortSchema = sorterService.sortSchema(mergedSchema, "mutations.graphqls");
    System.out.println(sortSchema);
  }
}
