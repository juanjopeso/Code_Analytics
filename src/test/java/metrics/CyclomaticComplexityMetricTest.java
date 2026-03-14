package metrics;

import static org.junit.jupiter.api.Assertions.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.Test;

public class CyclomaticComplexityMetricTest {

    @Test
    void shouldCalculateComplexityCorrectly() {
        String code = """
                class A {
                    void test(int x) {
                        if(x > 0) {
                            System.out.println(x);
                        } else {
                            System.out.println(-x);
                        }
                    }
                }
                """;

        MethodDeclaration method =
                StaticJavaParser.parse(code)
                        .findFirst(MethodDeclaration.class)
                        .get();

        CyclomaticComplexityMetric metric = new CyclomaticComplexityMetric();
        int cc = metric.calculate(method);

        assertTrue(cc >= 2);
    }
}