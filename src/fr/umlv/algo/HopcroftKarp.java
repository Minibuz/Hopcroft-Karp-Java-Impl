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
         * vertices in V1 that is unmatched in M
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

        Utils.emptyArray(level);

        var visited = new boolean[g.numberOfVertices()];
        Utils.emptyArray(visited);

        var currentLevel = 0;

        while(!queue.isEmpty()) {
            /*
             * for each v in queue
             */
            for(var vertex : queue) {
                /*
                 * for each neighbor v' of v not in visited
                 */
                for(int i = 0; i < g.numberOfVertices(); i++) {
                    if(g.isEdge(vertex, i) && g.isEdge(i, vertex) && !visited[i]) {
                        /*
                         * (clvl even and {v,v'} not in M) or (clvl odd and {v,v'} in M)
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
             * increment clvl
             * clear queue
             */
            currentLevel++;
            queue.clear();

            /*
             * any vertex in newqueue is unmatched and not in V1
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

            queue.addAll(newQueue);
            newQueue.clear();
        }

        return false;
    }

    private static boolean levelDFS(Graph g, List<Integer> v1, ArrayList<Edge> m, int[] level, boolean[] visited, int v) {
        visited[v] = true;

        /*
         * if v is not in V1 and unmatched in M
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
         * for each neighbor v' of v not visited
         */
        for(int i = 0; i < g.numberOfVertices(); i++) {
            if(g.isEdge(v, i) && g.isEdge(i, v) && !visited[i]) {
                /*
                 *   if (level[v] even and {v,v'} not in M) or (level[v] odd and {v,v'} in M)
                 */
                if( (level[v]%2 == 0  && !m.contains(new Edge(v, i)))) {
                    /*
                     * if level[v'] == level[v]+1 and levelDFS(G, V1, M, level, visited, v')
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
                if(level[v]%2 != 0  && m.contains(new Edge(i, v))) {
                    /*
                     * if level[v'] == level[v]+1 and levelDFS(G, V1, M, level, visited, v')
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

    public static List<Edge> hopcroftKarp(Graph g, int[] iterations, List<Integer> V1, List<Integer> V2) {
        var M = new ArrayList<Edge>();

        // initialize the array of level
        var level = new int[g.numberOfVertices()];
        Utils.emptyArray(level);
        // initialize the array of visited
        var visited = new boolean[g.numberOfVertices()];
        Utils.emptyArray(visited);

        // while levelBFS(G, V1, M, level)         // Still has augmenting paths
        while(levelBFS(g, V1, M, level)) {
            // for each unmatched v in V1
            V1.forEach(v -> {
                boolean toDo = true;
                for(var edge : M) {
                    if(edge.start() == v || edge.end() == v) {
                        toDo = false;
                    }
                }
                if(toDo) {
                    // levelDFS(G, V1, M, level, visited, v)
                    levelDFS(g, V1, M, level, visited, v);
                }
            });
            // clear up the array of level and visited
            Utils.emptyArray(level);
            Utils.emptyArray(visited);
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

        Graph graph = Graph.buildGraphFromFile(args[0] + ".gr");
        var v1 = IntStream.range(0, graph.numberFromV1()).boxed().toList();
        var v2 =  IntStream.range(graph.numberFromV1(), graph.numberOfVertices()).boxed().toList();
        int[] iterations = new int[1];
        var M = hopcroftKarp(graph, iterations, v1, v2);

        writeFileOutput(args[0], graph.numberFromV1(), M);
        writeConsoleOutput(args[0], M, iterations[0]);
    }

    private static void writeFileOutput(String path, int V1, List<Edge> m) throws IOException {
        m.sort(Comparator.comparingInt(Edge::start));
        StringBuilder sb = new StringBuilder();
        sb.append(m.size()).append("\n");
        m.forEach(edge -> sb.append(edge.start()).append(" ").append(edge.end() - V1).append("\n"));
        sb.replace(sb.length()-1, sb.length(), "");

        Path fileOutput = Paths.get(path + "_2.sol");
        if(!Files.exists(fileOutput)) {
            Files.createFile(fileOutput);
        }
        Files.writeString(fileOutput, sb.toString());
    }

    private static void writeConsoleOutput(String path, List<Edge> m, int iterations) {
        System.out.println("File " + path + ".gr, solution " + path + ".sol");
        System.out.println("Matching with " + m.size() + " edge(s)");
        System.out.println("Using " + iterations + " iteration(s)");
    }
}
