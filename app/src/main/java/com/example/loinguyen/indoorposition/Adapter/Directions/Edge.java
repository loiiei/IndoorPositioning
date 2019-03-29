package com.example.loinguyen.indoorposition.Adapter.Directions;

public class Edge {
    public final Vertex target;
    public final double weight;
    public Edge(Vertex argTarget, double argWeight)
    {
        target = argTarget;
        weight = argWeight;
    }
}
