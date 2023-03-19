package sortgraphql.sort;

import graphql.parser.InvalidSyntaxException;
import graphql.parser.MultiSourceReader;
import graphql.parser.Parser;
import graphql.schema.idl.TypeDefinitionRegistry;
import sortgraphql.exception.FailureException;

public class SchemaParser {
  public TypeDefinitionRegistry parse(String schemaContent, String sourceName) {
    var schemaParser = new graphql.schema.idl.SchemaParser();
    try {
      var parser = new Parser();
      var multiSourceReader =
          MultiSourceReader.newMultiSourceReader()
              .string(schemaContent, sourceName)
              .trackData(true)
              .build();
      var document = parser.parseDocument(multiSourceReader);

      return schemaParser.buildRegistry(document);
    } catch (InvalidSyntaxException e) {
      throw new FailureException(
          String.format("Cannot parse schema '%s', %s", sourceName, e.getMessage()));
    }
  }
}
