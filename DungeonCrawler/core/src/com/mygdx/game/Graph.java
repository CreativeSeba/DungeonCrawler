package com.mygdx.game;

import java.util.*;

public class Graph {
    ArrayList<Node> nodes = new ArrayList<>();
    ArrayList<Edge> edges = new ArrayList<>();

    public void printGraph() {
        for (Node node : nodes) {
            System.out.println("\nNode id: " + node.id + " | Node name: " + node.name + "\nEdges: ");
            for (Edge edge : edges) {
                if (node.id == edge.id1 || node.id == edge.id2) {
                    System.out.println("Edge id1: " + edge.id1 + " | id2: " + edge.id2 + " | weight " + edge.weight);
                }
            }
        }
    }

    public Node addNode(String name) {
        Node node = new Node(name);
        nodes.add(node);
        return node;
    }


    public Node removeNode(int id) {
        if (nodes.size() == 0) {
            System.out.println("No nodes to remove");
        }
        ArrayList<Edge> edgesToRemove = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.id1 == id || edge.id2 == id) {
                edgesToRemove.add(edge);
            }
        }
        edges.removeAll(edgesToRemove);

        for (Node node : nodes) {
            if (node.id == id) {
                nodes.remove(node);
                return node;
            }
        }
        return null;
    }


    public Edge addEdge(int weight, int id1, int id2) {
        boolean node1Exists = false;
        boolean node2Exists = false;

        for (Node node : nodes) {
            if (node.id == id1) {
                node1Exists = true;
            }
            if (node.id == id2) {
                node2Exists = true;
            }
        }
        if (!node1Exists || !node2Exists || id1 == id2) {
            System.out.println("Error with adding edge");
        } else {
            Edge edge = new Edge(weight, id1, id2);
            edges.add(edge);
            return edge;
        }
        return null;
    }

    public Edge removeEdge(int id1, int id2) {
        Edge edgeToRemove = null;

        for (Edge edge : edges) {
            if ((edge.id1 == id1 && edge.id2 == id2) || (edge.id1 == id2 && edge.id2 == id1)) {
                edgeToRemove = edge;
                break;
            }
        }
        if (edgeToRemove != null) {
            edges.remove(edgeToRemove);
            return edgeToRemove;
        } else {
            System.out.println("Edge not found");
        }
        return null;
    }

    public ArrayList<Integer> shortestPath(int id1, int id2) {
        HashMap<Integer, Integer> nodeDistance = new HashMap<>();
        ArrayList<Integer> visitedNodes = new ArrayList<>();
        HashMap<Integer, Integer> previousNodes = new HashMap<>();

        for (Node node : nodes) {
            nodeDistance.put(node.id, Integer.MAX_VALUE);
        }
        nodeDistance.put(id1, 0);
        int currentNodeId = -1;
        while (visitedNodes.size() != nodes.size()) {
            int smallestDistance = Integer.MAX_VALUE;

            for (Map.Entry<Integer, Integer> entry : nodeDistance.entrySet()) {
                if (!visitedNodes.contains(entry.getKey()) && entry.getValue() < smallestDistance) {
                    smallestDistance = entry.getValue();
                    currentNodeId = entry.getKey();
                }
            }
            visitedNodes.add(currentNodeId);
            for (Edge edge : edges) {
                if (edge.id1 == currentNodeId || edge.id2 == currentNodeId) {
                    int neighborId = (edge.id1 == currentNodeId) ? edge.id2 : edge.id1;
                    int newDistance = nodeDistance.get(currentNodeId) + edge.weight;
                    if (newDistance < nodeDistance.get(neighborId)) {
                        nodeDistance.put(neighborId, newDistance);
                        previousNodes.put(neighborId, currentNodeId);
                    }
                }
            }
        }
        if (nodeDistance.get(id2) == Integer.MAX_VALUE) {
            System.out.println("\nNo path exists from node " + id1 + " to node " + id2);
            return new ArrayList<>();
        }
        currentNodeId = id2;
        String path = "" + id2;
        System.out.println("\nShortest path from node " + id1 + " to node " + id2 + ":");
        ArrayList<Integer> pathList = new ArrayList<>();
        while (currentNodeId != id1 && previousNodes.containsKey(currentNodeId)) {
            pathList.add(currentNodeId);
            currentNodeId = previousNodes.get(currentNodeId);
        }
        pathList.add(currentNodeId);
        System.out.println(path);
        System.out.println("Distance from node " + id1 + " to node " + id2 + " is " + nodeDistance.get(id2));

        return pathList;
    }

}