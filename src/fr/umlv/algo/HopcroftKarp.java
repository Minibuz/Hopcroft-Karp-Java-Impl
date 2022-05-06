package fr.umlv.algo;

import java.io.IOException;
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
                    if(g.isEdge(vertex, i) && !visited[i]) {
                        /*
                         * (clvl even and {v,v'} not in M) or (clvl odd and {v,v'} in M)
                         */
                        if((currentLevel%2==0 && !m.contains(new Edge(vertex, i)))
                                || (currentLevel%2!=0 && m.contains(new Edge(vertex, i)))) {
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
            final boolean[] matched = {true, true};
            newQueue.forEach(vertex -> {
                if(!V1.contains(vertex)) {
                    matched[0] = false;
                }
                m.forEach(edge -> {
                    if(edge.end() == vertex || edge.start() == vertex) {
                        matched[1] = false;
                    }
                });
            });
            if(matched[0] && matched[1]) {
                return true;
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
            if(g.isEdge(v, i) && !visited[i]) {
                /*
                 *   if (level[v] even and {v,v'} not in M) or (level[v] odd and {v,v'} in M)
                 */
                if( (level[v]%2 == 0  && !m.contains(new Edge(v, i)))
                        || (level[v]%2 != 0  && m.contains(new Edge(v, i)))) {
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
            }
        }
        return false;
    }

    public static List<Edge> hopcroftKarp(Graph g, List<Integer> V1 , List<Integer> V2) {
        var M = new ArrayList<Edge>();

        // initialize the array of level
        var level = new int[g.numberOfVertices()];
        Utils.emptyArray(level);
        // initialize the array of visited
        var visited = new boolean[g.numberOfVertices()];
        Utils.emptyArray(visited);

        // while levelBFS(G, V1, M, level)         // Still has augmenting paths
        while(levelBFS(g, V1, M, level)) {
            System.out.println(M);
            // for each unmatched v in V1
            V1.forEach(v -> {
                final boolean[] toDo = {true};
                M.forEach(edge -> {
                    if(edge.start() == v || edge.end() == v) {
                        toDo[0] = false;
                    }
                });
                if(toDo[0]) {
                    // levelDFS(G, V1, M, level, visited, v)
                    levelDFS(g, V1, M, level, visited, v);
                }
            });
            // clear up the array of level and visited
            Utils.emptyArray(level);
            Utils.emptyArray(visited);
        }
        return M;
    }

    public static void main(String[] args) throws IOException {
        //testFile("testcase/graphs/g1.gr");
        //testFile("testcase/graphs/g2.gr");
        testFile("testcase/graphs/g3.gr");
        //testFile("testcase/graphs/g4.gr");
        //testFile("testcase/graphs/g5.gr");
        //testFile("testcase/graphs/g6.gr");
        //testFile("testcase/graphs/g7.gr");
        //testFile("testcase/graphs/g8.gr");
    }

    private static void testFile(String path) throws IOException {
        Graph g = Graph.buildGraphFromFile(path);

        var v1 = IntStream.range(0, g.numberFromV1()).boxed().toList();
        var v2 =  IntStream.range(g.numberFromV1(), g.numberOfVertices()).boxed().toList();
        var result = hopcroftKarp(g, v1, v2);
        System.out.println(result.size());
        System.out.println(result);
    }
}
