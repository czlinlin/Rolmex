package com.mossle.bpm.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private Node initial;

    private Map<String, Node> nodeMap = new HashMap<String, Node>();
    
    public Node getInitial() {
        return initial;
    }

    public void setInitial(Node initial) {
        this.initial = initial;
    }

    public List<Node> getNodes() {
        List<Node> nodes = new ArrayList<Node>();
        visitNode(initial, nodes);

        return nodes;
    }

    public void visitNode(Node node, List<Node> nodes) {
        nodes.add(node);

        for (Edge edge : node.getOutgoingEdges()) {
            Node nextNode = edge.getDest();
            visitNode(nextNode, nodes);
        }
    }

    public List<Edge> getEdges() {
        List<Edge> edges = new ArrayList<Edge>();
        visitEdge(initial, edges);

        return edges;
    }

    public void visitEdge(Node node, List<Edge> edges) {
        for (Edge edge : node.getOutgoingEdges()) {
            edges.add(edge);

            Node nextNode = edge.getDest();
            visitEdge(nextNode, edges);
        }
    }

    public Node findById(String id) {
        for (Node node : this.getNodes()) {
            if (id.equals(node.getId())) {
                return node;
            }
        }

        return null;
    }

	public Map<String, Node> getNodeMap() {
		return nodeMap;
	}

	public void setNodeMap(Map<String, Node> nodeMap) {
		this.nodeMap = nodeMap;
	}
    
    
}
