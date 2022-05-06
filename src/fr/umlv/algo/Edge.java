package fr.umlv.algo;

public record Edge(int start, int end) {

    public Edge {
        if(start<0 || end<0) throw new IllegalArgumentException();
    }

    public String fileOutput() {
        return start + " " + end;
    }
}
