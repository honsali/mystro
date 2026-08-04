package app.reading.description.common.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DoctrineLotMathTest {
    private final DoctrineLotMath math = new DoctrineLotMath();

    @Test
    void normalizeExamples() {
        assertEquals(0.0, math.normalize360(0.0));
        assertEquals(0.0, math.normalize360(360.0));
        assertEquals(359.0, math.normalize360(-1.0));
        assertEquals(1.0, math.normalize360(721.0));
    }

    @Test
    void forwardArcExamples() {
        assertEquals(20.0, math.forwardArc(350.0, 10.0));
        assertEquals(340.0, math.forwardArc(10.0, 350.0));
    }
}
