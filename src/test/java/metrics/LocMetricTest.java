package metrics;

import static org.junit.jupiter.api.Assertions.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.Test;

public class LocMetricTest {

    @Test
    void shouldCalculateLocCorrectly() {
        String code = """
                class A {
                    void test() {
                        int a = 1;
                        int b = 2;
                        int c = a + b;
                    }
                }
                """;

        MethodDeclaration method =
                StaticJavaParser.parse(code)
                        .findFirst(MethodDeclaration.class)
                        .get();

        LocMetric metric = new LocMetric();
        int loc = metric.calculate(method);

        assertEquals(5, loc);
    }
}