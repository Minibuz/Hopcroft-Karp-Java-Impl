package fr.umlv.algo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class Graph {
    private final ArrayList<LinkedList<Edge>> adj;
    private final int v1;
    private final int v2;
    private final int n; // Number of vertices
    private int m;


    public Graph(int v1, int v2) {
        if (v1 <= 0 || v2 <= 0) throw new IllegalArgumentException();

        this.v1 = v1;
        this.v2 = v2;
        this.n = v1 + v2;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            this.adj.add(new LinkedList<>());
        }
    }

    public int numberOfVertices() {
        return n;
    }

    public int numberOfEdges() {
        return m;
    }

    public int numberFromV1() {
        return v1;
    }

    public int numberFromV2() {
        return v2;
    }

    public void addEdge(int i, int j) {
        Objects.checkIndex(i, n);

        j = j + v1;
        Objects.checkIndex(j, n);

        if(isEdge(i,j)) {
            throw new IllegalArgumentException("There's already an edge from " + i + " to " + j + " !");
        }
        if(isEdge(j,i)) {
            throw new IllegalArgumentException("There's already an edge from " + j + " to " + i + " !");
        }
        adj.get(i).add(new Edge(i,j));
        adj.get(j).add(new Edge(j,i));

        m++;
    }

    public boolean isEdge(int i, int j) {
        Objects.checkIndex(i, n);
        Objects.checkIndex(j, n);

        var list = this.adj.get(i);
        return list.stream().anyMatch(edge -> edge.end() == j);
    }

    public Iterator<Edge> edgeIterator(int i) {
        Objects.checkIndex(i, n);

        var list = this.adj.get(i);
        var size = list.size();
        return new Iterator<>() {
            private int currentSize = 0;

            @Override
            public boolean hasNext() {
                return currentSize < size;
            }

            @Override
            public Edge next() {
                if (!hasNext()) throw new NoSuchElementException();
                return list.get(currentSize++);
            }
        };
    }

    public void forEachEdge(int i, Consumer<Edge> consumer) {
        edgeIterator(i).forEachRemaining(consumer);
    }

    public static Graph buildGraphFromFile(String uri) throws IOException {
        var list = Files.readAllLines(Paths.get(uri));

        // Sizes of both side
        var sizes = list.get(0).split(" ");
        var sizeOfV1 = Integer.parseInt(sizes[0]);
        var sizeOfV2 = Integer.parseInt(sizes[1]);
        list.remove(0);

        // Number of edges
        var edges = Integer.parseInt(list.get(0));
        list.remove(0);

        var graph = new Graph(sizeOfV1, sizeOfV2);
        for (var i = 0; i < edges; i++) {
            var edge = list.get(i).split(" ");
            var start = Integer.parseInt(edge[0]);
            var end = Integer.parseInt(edge[1]);
            graph.addEdge(start, end);
        }

        if (graph.numberOfEdges() != edges) throw new IllegalStateException();

        return graph;
    }
}
