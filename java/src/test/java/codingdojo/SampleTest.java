package codingdojo;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleTest {
    @Test
    void sample() {
        Approvals.verify("Hello World");
    }

}
