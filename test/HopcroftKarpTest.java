import fr.umlv.algo.Graph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static fr.umlv.algo.HopcroftKarp.hopcroftKarp;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HopcroftKarpTest {

    @ParameterizedTest()
    @CsvSource(value = {
            "testcase/graphs/g1:7",
            "testcase/graphs/g2:4",
            "testcase/graphs/g3:9",
            "testcase/graphs/g4:8",
            "testcase/graphs/g5:75",
            "testcase/graphs/g6:74",
            "testcase/graphs/g7:838",
            "testcase/graphs/g8:831",
            "testcase/graphs/g9:985",
            "testcase/graphs/g10:979",
            "testcase/graphs/g11:1000",
            "testcase/graphs/g12:1000",
            "testcase/graphs/g13:9264",
            "testcase/graphs/g14:9928"
    }, delimiter = ':')
    @DisplayName("Test of every graph")
    @Timeout(value = 20)
    public void GraphTest(String path, String result) throws IOException {
        Graph graph = Graph.buildGraphFromFile(path + ".gr");
        var v1 = IntStream.range(0, graph.numberFromV1()).boxed().toList();
        var v2 =  IntStream.range(graph.numberFromV1(), graph.numberOfVertices()).boxed().toList();
        int[] iterations = new int[1]; // Used to know how many iterations in the while loop we did
        var M = hopcroftKarp(graph, iterations, v1, v2);

        var actual = M.size();
        var expected = Integer.parseInt(result);

        assertEquals(expected, actual);
    }
}
