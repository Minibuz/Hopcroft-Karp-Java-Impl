package fr.umlv.algo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.IntStream;

public class HopcroftKarp {

    private static boolean levelBFS(Graph g, List<Integer> V1, List<Edge> m, int[] level) {
        final var queue = new ArrayList<Integer>();

        /*
         * Add every vertex of V1 into queue if they are unmatched
         */
        V1.forEach(vertex -> {
            boolean toAdd = true;
            for(var edge : m) {
                if (edge.start() == vertex || edge.end() == vertex) {
                    toAdd = false;
                    break;
                }
            }
            if(toAdd) queue.add(vertex);
        });

        final var newQueue = new ArrayList<Integer>();

        // Reset level array
        Utils.emptyArray(level);

        // Initialize an array for visited vertex
        var visited = new boolean[g.numberOfVertices()];
        Utils.emptyArray(visited);

        // Initialize currentlevel to 0
        var currentLevel = 0;

        /*
         * As long as there is at least one element in queue
         */
        while(!queue.isEmpty()) {
            /*
             * For each vertex in queue
             */
            for(var vertex : queue) {
                /*
                 * Compute for every neighbor if they are visited before doing the rest of the computation.
                 */
                for (Iterator<Edge> it = g.edgeIterator(vertex); it.hasNext();) {
                    Edge edge = it.next();
                    int i = edge.end();
                    if(!visited[i]) {
                        /*
                         * If (currentlevel is even and {v,v'} not in M) or (currentlevel is odd and {v,v'} in M)
                         */
                        if((currentLevel%2==0 && !m.contains(new Edge(vertex, i)))
                                || (currentLevel%2!=0 && m.contains(new Edge(i, vertex)))) {
                            newQueue.add(i);
                            visited[i] = true;
                            level[i] = currentLevel + 1;
                        }
                    }
                }
            }

            /*
             * Increment the current level and clear queue
             */
            currentLevel++;
            queue.clear();

            /*
             * If any vertex in newqueue is unmatched and not in V1
             */
            for(var vertex : newQueue) {
                    if (!V1.contains(vertex)) {
                        var unmatched = true;
                        for (var edge : m) {
                            if (edge.start() == vertex || edge.end() == vertex) {
                                unmatched = false;
                                break;
                            }
                        }
                        if (unmatched) return true;
                    }
            }

            // Add newQueue into queue and clear newQueue
            queue.addAll(newQueue);
            newQueue.clear();
        }

        return false;
    }

    private static boolean levelDFS(Graph g, List<Integer> v1, ArrayList<Edge> m, int[] level, boolean[] visited, int v) {
        visited[v] = true;

        /*
         * If v is not in V1 and unmatched in M
         */
        if(!v1.contains(v)) {
            var unmatched = true;
            for(var edge : m) {
                if (edge.start() == v || edge.end() == v) {
                    unmatched = false;
                    break;
                }
            }
            if(unmatched) return true;
        }


        /*
         * Compute for every neighbor if they are visited before doing the rest of the computation.
         */
        for (Iterator<Edge> it = g.edgeIterator(v); it.hasNext();) {
            Edge edge = it.next();
            int i = edge.end();
            if(!visited[i]) {
                /*
                 * This is V1 to V2
                 * If level[v] is even, and {v, v'} is not in M
                 */
                if( (level[v]%2 == 0  && !m.contains(new Edge(v, i)))) {
                    /*
                     * If level[v'] is equal to level[v]+1 and levelDFS(G, V1, M, level, visited, v') is true.
                     *      levelDFS is true means there's an unmatched vertex.
                     */
                    if( level[i] == level[v]+1 && levelDFS(g, v1, m, level, visited, i)) {
                        if(m.contains(new Edge(v, i))) {
                            m.remove(new Edge(v, i));
                        } else {
                            m.add(new Edge(v, i));
                        }
                        return true;
                    }
                }
                /*
                 * This is V2 to V1
                 * If level[v] is odd, and {v, v'} is in M
                 */
                if(level[v]%2 != 0  && m.contains(new Edge(i, v))) {
                    /*
                     * If level[v'] is equal to level[v]+1 and levelDFS(G, V1, M, level, visited, v') is true.
                     *      levelDFS is true means there's an unmatched vertex.
                     */
                    if( level[i] == level[v]+1 && levelDFS(g, v1, m, level, visited, i)) {
                        if(m.contains(new Edge(i, v))) {
                            m.remove(new Edge(i, v));
                        } else {
                            m.add(new Edge(i, v));
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Compute the Hopcroft-Krap algorithm.<br>
     * Hopcroft-Krap is an algorithm that takes a bipartite graph as input and produces a maximum cardinality matching
     * as output. The output is a list of as many edges as possible with the property that no two edges share an
     * endpoint.
     *
     * @param g
     *          {@link Graph} is the source for the maximum cardinality matching.<br>
     *          Also known as a bipartite graph.
     * @param iterations
     *          {@link Integer}[] with one element representing the number of iterations to find the output.
     * @param V1
     *          {@link List} of {@link Integer} is the left part of the {@link Graph}
     * @param V2
     *          {@link List} of {@link Integer} is the right part of the {@link Graph}
     * @return
     *          {@link List} of {@link Edge} representing the best edge making the maximum cardinality matching.
     */
    public static List<Edge> hopcroftKarp(Graph g, int[] iterations, List<Integer> V1, List<Integer> V2) {
        var M = new ArrayList<Edge>();

        // Initialize the array of level for vertex
        var level = new int[g.numberOfVertices()];
        Utils.emptyArray(level);
        // Initialize the array of vertex visited
        var visited = new boolean[g.numberOfVertices()];
        Utils.emptyArray(visited);

        // LevelBFS detect every augmented path and return true if there's at least ONE augmented path.
        while(levelBFS(g, V1, M, level)) {
            // Detect every unmatched vertex, v in V1 to compute the levelDFS.
            V1.forEach(v -> {
                boolean toDo = true;
                for(var edge : M) {
                    if(edge.start() == v || edge.end() == v) {
                        toDo = false;
                    }
                }
                if(toDo) {
                    levelDFS(g, V1, M, level, visited, v);
                }
            });
            // Clear the array to be ready for another iterations
            Utils.emptyArray(level);
            Utils.emptyArray(visited);
            // Increment the number of iterations
            iterations[0]++;
        }
        return M;
    }

    private static void usage() {
        System.out.println("Usage : HopcroftKarp input_path");
    }
    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            usage();
            return;
        }

        // Main process
        String fileName = args[0];
        Graph graph = Graph.buildGraphFromFile(fileName + ".gr");
        var v1 = IntStream.range(0, graph.numberFromV1()).boxed().toList();
        var v2 =  IntStream.range(graph.numberFromV1(), graph.numberOfVertices()).boxed().toList();
        int[] iterations = new int[1]; // Used to know how many iterations in the while loop we did
        var M = hopcroftKarp(graph, iterations, v1, v2);

        // Display result
        writeFileOutput(fileName, graph.numberFromV1(), M);
        writeConsoleOutput(fileName, M, iterations[0]);
    }

    /**
     * Write the result of the graph to a file corresponding the input file given in argument.
     *
     * @param fileName
     *          {@link String}
     * @param V1
     *          {@link Integer} is the number of element in the right side of the graph
     * @param m
     *          {@link List} of {@link Edge}
     * @throws IOException
     */
    private static void writeFileOutput(String fileName, int V1, List<Edge> m) throws IOException {
        String result = getGraphSolution(V1, m);

        Path fileOutput = Paths.get(fileName + "_2.sol");
        if(!Files.exists(fileOutput)) {
            Files.createFile(fileOutput);
        }
        Files.writeString(fileOutput, result);
    }

    /**
     * Compute the string corresponding to the solution of the graph.
     *  Compose of the number of path as well as each of them from v1 to v2.
     *
     * @param V1
     * @param m
     * @return
     */
    private static String getGraphSolution(int V1, List<Edge> m) {
        m.sort(Comparator.comparingInt(Edge::start));

        StringBuilder sb = new StringBuilder();
        sb.append(m.size()).append("\n");
        m.forEach(edge -> sb.append(edge.start()).append(" ").append(edge.end() - V1).append("\n"));
        sb.replace(sb.length()-1, sb.length(), "");
        var result = sb.toString();
        return result;
    }

    /**
     * Write the result of the graph to the console.
     *
     * @param path
     * @param m
     * @param iterations
     */
    private static void writeConsoleOutput(String path, List<Edge> m, int iterations) {
        System.out.println("File " + path + ".gr, solution " + path + ".sol");
        System.out.println("Matching with " + m.size() + " edge(s)");
        System.out.println("Using " + iterations + " iteration(s)");
    }
}
