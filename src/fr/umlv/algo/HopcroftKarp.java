package fr.umlv.algo;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class HopcroftKarp {

    private static boolean levelBFS(Graph g, List<Integer> V1, List<Edge> m, int[] level) {
        final var queue = new ArrayList<Integer>();
        V1.forEach(vertex -> {
            final boolean[] toAdd = {true};
            m.forEach(edge -> {
                if(edge.start() == vertex || edge.end() == vertex) {
                    toAdd[0] = false;
                }
            });
            if(toAdd[0]) {
                queue.add(vertex);
            }
        });

        final var newQueue = new ArrayList<Integer>();
        Utils.emptyArray(level);

        var visited = new boolean[g.numberOfVertices()];
        Utils.emptyArray(visited);

        var currentLevel = 0;

        while(!queue.isEmpty()) {
            for(var vertex : queue) {
                for(int i = 0; i < g.numberOfVertices(); i++) {
                    if((g.isEdge(vertex, i) || g.isEdge(i, vertex)) && !visited[i]) {
                        if((currentLevel%2==0 && !m.contains(new Edge(vertex, i))) || (currentLevel%2!=0 && m.contains(new Edge(vertex, i)))) {
                            newQueue.add(i);
                            visited[i] = true;
                            level[i] = currentLevel + 1;
                        }
                    }
                }
            }

            currentLevel++;
            queue.clear();

            final boolean[] matched = {true};
            newQueue.forEach(vertex -> {
                if(V1.contains(vertex)) {
                    m.forEach(edge -> {
                        if(edge.end() == vertex || edge.start() == vertex) {
                            matched[0] = false;
                        }
                    });
                }
            });

            if(matched[0]) {
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
            final boolean[] found = {true};
            m.forEach(edge -> {
                if (edge.start() == v) {
                    found[0] = false;
                }
                if (edge.end() == v) {
                    found[0] = false;
                }
            });
            if (found[0]) return true;
        }

        for(int i = 0; i < g.numberOfVertices(); i++) {
            if((g.isEdge(v, i) || g.isEdge(i, v)) && !visited[i]) {
                /*
                 *   if (level[v] even and {v,v'} not in M) or (level[v] odd and {v,v'} in M)
                 */
                if( (level[v]%2 == 0 ) && !m.contains(new Edge(v, i)) || ((level[v]%2 != 0 ) && m.contains(new Edge(v, i))) ) {
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

        var level = new int[g.numberOfVertices()];
        Utils.emptyArray(level);

        var visited = new boolean[g.numberOfVertices()];
        Utils.emptyArray(visited);

        while(levelBFS(g, V1, M, level)) {
            V1.forEach(vertex -> M.forEach(edge -> {
                    if(edge.start() != vertex && edge.end() != vertex) {
                        levelDFS(g, V1, M, level, visited, vertex);
                    }
            }));
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
    }
}
