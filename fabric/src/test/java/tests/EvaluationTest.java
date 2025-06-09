package tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EvaluationTest {

    @SuppressWarnings({"NumericOverflow", "PointlessArithmeticExpression"})
    @Test
    void testNumericOverflow() {
        assertEquals(Integer.MAX_VALUE + 0, 2147483647);
        assertEquals(Integer.MAX_VALUE + 1, -2147483648);
        assertEquals(Integer.MAX_VALUE + 2, -2147483647);
    }
}
