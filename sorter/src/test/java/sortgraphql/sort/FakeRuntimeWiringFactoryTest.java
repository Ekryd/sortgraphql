package sortgraphql.sort;

import static org.junit.jupiter.api.Assertions.assertSame;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.ObjectValue;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** */
class FakeRuntimeWiringFactoryTest {

  @Test
  void coercingShouldJustPassThroughVales() {
    var coercing = FakeRuntimeWiringFactory.emptyCoercing;
    var anInstance = new Object();
    var context = GraphQLContext.newContext().build();
    var locale = Locale.getDefault();
    var value = new ObjectValue(List.of());

    assertSame(anInstance, coercing.serialize(anInstance, context, locale));
    assertSame(value, coercing.parseLiteral(value, CoercedVariables.of(Map.of()), context, locale));
    assertSame(anInstance, coercing.parseValue(anInstance, context, locale));
  }
}
