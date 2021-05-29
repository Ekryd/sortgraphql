package sortgraphql.sort;

import graphql.parser.InvalidSyntaxException;
import graphql.parser.Parser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;

import java.util.Collections;

public class SchemaParser {
  public TypeDefinitionRegistry parse(String schemaContent, String sourceName) {
    var schemaParser = new graphql.schema.idl.SchemaParser();
    try {
      var parser = new Parser();
      var document = parser.parseDocument(schemaContent, sourceName);

      return schemaParser.buildRegistry(document);
    } catch (InvalidSyntaxException e) {
      throw new SchemaProblem(Collections.singletonList(e.toInvalidSyntaxError()));
    }
  }
}
