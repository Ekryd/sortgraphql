package sortgraphql.sort;

import graphql.language.Document;
import graphql.parser.InvalidSyntaxException;
import graphql.parser.Parser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;

import java.util.Collections;

public class SchemaParser {
  public TypeDefinitionRegistry parse(String schemaContent, String sourceName) {
    graphql.schema.idl.SchemaParser schemaParser = new graphql.schema.idl.SchemaParser();
    try {
      Parser parser = new Parser();
      Document document = parser.parseDocument(schemaContent, sourceName);

      return schemaParser.buildRegistry(document);
    } catch (InvalidSyntaxException e) {
      throw new SchemaProblem(Collections.singletonList(e.toInvalidSyntaxError()));
    }
  }
}
