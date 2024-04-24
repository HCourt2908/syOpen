package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.io.IOException;
import java.util.*;

public class SimpleDijkstra {

    private ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> gameGraph;

    public SimpleDijkstra(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> gameGraph) {
        this.gameGraph = gameGraph;
    }

    public ArrayList<Integer> shortestPath(int startNode, int endNode) {

        ArrayList<Integer> pathSet[] = new ArrayList[gameGraph.nodes().size()];
        Arrays.fill(pathSet, new ArrayList<>(Collections.nCopies(100, 0)));
        //this array contains a lot of arraylists. each one of these holds the shortest path to each node
        //each arraylist is initialised to hold 100 zeros each. there is no shortest path from a to b
        //in the standard scotland yard map that is over 100 moves long.
        Set<Integer> visitedNodes = new HashSet<>();
        //a set to contain all the nodes that have been visited

        int currentNode = startNode;
        //initialises the first current node as our starting node
        pathSet[currentNode-1] = new ArrayList<>();
        //clears the path in the position of our first current node

        while(true) {

            visitedNodes.add(currentNode);
            //if a node is visited, it should not ever be visited again

            for(Integer neighbour : gameGraph.adjacentNodes(currentNode)) {
                if (pathSet[currentNode-1].size() + 1 < pathSet[neighbour-1].size() && !gameGraph.edgeValue(currentNode,neighbour).get().contains(ScotlandYard.Transport.FERRY)) {
                    //here we compare two values:
                    //the length of the path from the start node to the current node plus from node to neighbour (1)
                    //the current length of the path from the start node to the neighbour node
                    //the smaller of the two is the current shortest path from start to neighbour

                    pathSet[neighbour-1] = new ArrayList<>();
                    pathSet[neighbour-1].addAll(pathSet[currentNode-1]);
                    pathSet[neighbour-1].add(currentNode);
                    //if we find that start->current->neighbour is shorter, we:
                    //clear the neighbour's path list, copy the current node's path list and add the current node to it
                }
            }

            if (visitedNodes.containsAll(gameGraph.nodes())) {
                //if all nodes have been visited, return the path from the start node to the end node
                return pathSet[endNode - 1];
            }

            currentNode = getClosestNodePath(pathSet, visitedNodes) + 1;
            //by convention, the next current node is always unvisited and has the current shortest distance of all unvisited nodes

        }

    }

    private int getClosestNodePath(ArrayList<Integer>[] pathSet, Set<Integer> visitedNodes) {

        int smallestIndex = 0;

        for (int i = 0; i < pathSet.length; i++) {
            if (pathSet[i].size() < 100 && !visitedNodes.contains(i+1)) {
                smallestIndex = i;
                break;
            }
        }

        //finds the first instance of an unvisited node in the array

        for (int i = smallestIndex; i < pathSet.length; i++) {
            if (pathSet[i].size() < pathSet[smallestIndex].size() && !visitedNodes.contains(i+1)) {
                smallestIndex = i;
            }
        }

        //for every other node, if it is unvisited and the distance is shorter than our current closest node,
        //then this node is our newest current closest node

        return smallestIndex;
    }

    public static void main(String[] args) throws IOException {
        SimpleDijkstra d = new SimpleDijkstra(ScotlandYard.standardGraph());
        ArrayList<Integer> shortestPath = d.shortestPath(194, 157);
        System.out.println(shortestPath);

        // the shortest path function returns a list of the fewest nodes to traverse in order to get from a to b
        // If I ever want to find the shortest distance, I can just do list.size() and it will return the same value
        // as if I were to write a whole function for it
        //
        // justification: Originally, I started by writing a function that performed this task. I then wrote:
        // assert (shortestPath.size() == shortestDistance) and iterated over all possible combinations in the graph
        // no error was ever recorded, so I safely deleted the shortestDistance method.

    }


}
