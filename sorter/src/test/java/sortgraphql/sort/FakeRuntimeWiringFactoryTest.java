package sortgraphql.sort;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 *
 */
class FakeRuntimeWiringFactoryTest {

  @Test
  void coercingShouldJustPassThroughVales() {
    var coercing = FakeRuntimeWiringFactory.emptyCoercing;
    var anInstance = new Object();
    assertSame(anInstance, coercing.serialize(anInstance));
    assertSame(anInstance, coercing.parseLiteral(anInstance));
    assertSame(anInstance, coercing.parseValue(anInstance));
  }
}
