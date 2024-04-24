package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import org.checkerframework.checker.units.qual.A;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class AdvancedDijkstra {

    private ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> gameGraph;
    private Board.GameState state;

    public AdvancedDijkstra(Board.GameState state) {
        this.state = state;
        this.gameGraph = state.getSetup().graph;
    }

    public ArrayList<Integer> shortestPath(int startNode, int endNode, Piece detective) {

        ArrayList<Integer> pathSet[] = new ArrayList[gameGraph.nodes().size()];
        Arrays.fill(pathSet, new ArrayList<>(Collections.nCopies(100,0)));
        //this array contains a lot of arrayLists. each one of these holds the shortest path to each node currently known.
        //each arrayList is initialised to hold 100 zeros each. this is intended to mimic the distance:infinity we see in the pseudocode algorithm
        //in the standard map, there is no shortest path a to b that is over 100 moves long
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
                if (pathSet[currentNode-1].size() + 1 < pathSet[neighbour-1].size()
                        && !gameGraph.edgeValue(currentNode,neighbour).get().contains(ScotlandYard.Transport.FERRY)
                        && detectiveCapableOfMaking(detective, pathSet, currentNode, neighbour)) {
                    //here we do three things:
                    //compare the length of the new potential shortest path to the currently shortest known path
                    //make sure that the edge node is not a ferry because the detectives cant use a ferry
                    //check if the detective can actually get to this node taking this path with the tickets they have

                    pathSet[neighbour-1] = new ArrayList<>();
                    pathSet[neighbour-1].addAll(pathSet[currentNode-1]);
                    pathSet[neighbour-1].add(currentNode);
                    //if we find that the start->current->neighbour is shorter (plus all other checks), we:
                    //clear the neighbour's path list, copy the current node's path list and add the current node to it

                }
            }

            if(visitedNodes.containsAll(gameGraph.nodes())) {
                //if all nodes have been visited, return the path from the start node to the end node
                if (pathSet[endNode-1].size() == 100) {
                    //this means that the destination node for this move cannot be reached by this detective
                    ArrayList<Integer> unreachableNode = new ArrayList<>();
                    unreachableNode.add(-1);
                    return unreachableNode;
                } else return pathSet[endNode - 1];
            }

            currentNode = getClosestNodePath(pathSet, visitedNodes) + 1;
            //by convention, the next current node is always unvisited and has the current shortest distance of all unvisited nodes
        }

    }

    private int getClosestNodePath(ArrayList<Integer>[] pathSet, Set<Integer> visitedNodes) {

        int smallestIndex = 0;
        boolean foundFlag = false;

        for (int i = 0; i < pathSet.length; i++) {
            if (pathSet[i].size() < 100 && !visitedNodes.contains(i+1)) {
                smallestIndex = i;
                foundFlag = true;
                break;
            }
        }

        if(!foundFlag) {
            for (int i = 0; i < pathSet.length; i++) {
                if (!visitedNodes.contains(i+1)) {
                    smallestIndex = i;
                    break;
                }
            }
        }

        //finds the first instance of an unvisited node in the array
        //if it reaches a point where it has only not visited indexes that the player can't reach, it ignores the list size limit and just finds an unvisited node

        for(int i = smallestIndex; i < pathSet.length; i++) {
            if (pathSet[i].size() < pathSet[smallestIndex].size() && !visitedNodes.contains(i+1)) {
                smallestIndex = i;
            }
        }

        //for every other node, if it is unvisited and the distance is shorter than our current closest node,
        //then this node is our newest current closest node

        return smallestIndex;
    }

    private boolean detectiveCapableOfMaking(Piece detective, ArrayList<Integer>[] pathSet, int aNode, int bNode) {

        Board.TicketBoard detectiveTickets = state.getPlayerTickets(detective).get();
        //gets all the tickets of the detective
        ArrayList<Integer[]> ticketBoards = new ArrayList<>();
        ticketBoards.add(new Integer[]{0,0,0});
        //creates a list to store all possible configurations of the ticket boards needed
        //the ticket board arrays are three indexes long: taxi, bus, underground

        for(int i = 0; i <= pathSet[aNode-1].size(); i++) {

            ImmutableSet<ScotlandYard.Transport> transportTypes;

            if (i == pathSet[aNode-1].size()) {
                transportTypes = gameGraph.edgeValue(aNode, bNode).get();
            } else if (i == pathSet[aNode-1].size() - 1) {
                transportTypes = gameGraph.edgeValue(pathSet[aNode-1].get(i), aNode).get();
            } else {
                transportTypes = gameGraph.edgeValue(pathSet[aNode-1].get(i), pathSet[aNode-1].get(i+1)).get();
            }
            //get the set of possible ticket types for each edge connection

            if (transportTypes.size()==1) {
                for (Integer[] ticketBoard : ticketBoards) {
                    if (transportTypes.contains(ScotlandYard.Transport.TAXI)) ticketBoard[0] ++;
                    else if (transportTypes.contains(ScotlandYard.Transport.BUS)) ticketBoard[1] ++;
                    else if (transportTypes.contains(ScotlandYard.Transport.UNDERGROUND)) ticketBoard[2] ++;
                }
            } else if (transportTypes.size()==2){
                //we now need to copy the number of arrays in the ticket boards list. this is to have all possible combinations of ticket uses
                ArrayList<Integer[]> ticketBoardsCopy = new ArrayList<>(ticketBoards);
                for(int j = 0; j < ticketBoardsCopy.size(); j++) {
                    ticketBoards.add(ticketBoardsCopy.get(j).clone());
                }

                List<ScotlandYard.Transport> transportList = new ArrayList<>(transportTypes);
                for(int j = 0; j < 1.0 * ticketBoards.size()/2; j++) {
                    if (transportList.get(0)== ScotlandYard.Transport.TAXI) ticketBoards.get(j)[0] ++;
                    else if (transportList.get(0)== ScotlandYard.Transport.BUS) ticketBoards.get(j)[1] ++;
                    else if (transportList.get(0)== ScotlandYard.Transport.UNDERGROUND) ticketBoards.get(j)[2] ++;
                }
                for(int j = ticketBoards.size()/2; j < ticketBoards.size(); j++) {
                    if(transportList.get(1)== ScotlandYard.Transport.TAXI) ticketBoards.get(j)[0] ++;
                    else if (transportList.get(1)== ScotlandYard.Transport.BUS) ticketBoards.get(j)[1] ++;
                    else if (transportList.get(1)== ScotlandYard.Transport.UNDERGROUND) ticketBoards.get(j)[2] ++;
                }
                //this duplicates the list, and then adds the first type of ticket to one half, and the second type of ticket to the other half

            } else if (transportTypes.size()==3){
                ArrayList<Integer[]> ticketBoardsCopy = new ArrayList<>(ticketBoards);
                for(int j = 0; j < ticketBoardsCopy.size(); j++) {
                    ticketBoards.add(ticketBoardsCopy.get(j).clone());
                }
                for(int j = 0; j < ticketBoards.size(); j++) {
                    ticketBoards.add(ticketBoardsCopy.get(j).clone());
                }
                //now the ticketBoards arrayList will contain three of the original arrayList

                List<ScotlandYard.Transport> transportList = new ArrayList<>(transportTypes);
                for(int j = 0; j < ticketBoards.size()/3; j++) {
                    if (transportList.get(0)== ScotlandYard.Transport.TAXI) ticketBoards.get(j)[0] ++;
                    else if (transportList.get(0)== ScotlandYard.Transport.BUS) ticketBoards.get(j)[1] ++;
                    else if (transportList.get(0)== ScotlandYard.Transport.UNDERGROUND) ticketBoards.get(j)[2] ++;
                }
                for(int j = ticketBoards.size()/3; j < 2 * ticketBoards.size()/3; j++) {
                    if(transportList.get(1)== ScotlandYard.Transport.TAXI) ticketBoards.get(j)[0] ++;
                    else if (transportList.get(1)== ScotlandYard.Transport.BUS) ticketBoards.get(j)[1] ++;
                    else if (transportList.get(1)== ScotlandYard.Transport.UNDERGROUND) ticketBoards.get(j)[2] ++;
                }
                for(int j = 2 * ticketBoards.size()/3; j < ticketBoards.size(); j++) {
                    if(transportList.get(2)== ScotlandYard.Transport.TAXI) ticketBoards.get(j)[0] ++;
                    else if (transportList.get(2)== ScotlandYard.Transport.BUS) ticketBoards.get(j)[1] ++;
                    else if (transportList.get(2)== ScotlandYard.Transport.UNDERGROUND) ticketBoards.get(j)[2] ++;
                }
                //this duplicates the list, and then adds the first type of ticket to one third,
                //the second type of ticket to the second third and the third type of ticket to the last third
            }
            //transportTypes can never be larger than size 3, since only taxi, bus and underground appear in the edges
            //ferry does technically count too, but we have already ignored ferry in a method before this

        }
        //iterates over all consecutive pairs of nodes in the corresponding pathSet. adds a ticket to the required ticket count for each edge value
        for(Integer[] ticketsNeeded : ticketBoards) {
            if (detectiveTicketsNecessary(ticketsNeeded, detectiveTickets)) return true;
        }
        //if we find any way that the detective can get to the destination from where they are currently, then we should return true
        return false;
        //if there is no way for the detective to reach this location from where they are now, return false
    }

    private boolean detectiveTicketsNecessary(Integer[] ticketsNeeded, Board.TicketBoard ticketsHave) {
        if(ticketsHave.getCount(ScotlandYard.Ticket.TAXI) >= ticketsNeeded[0]
                && ticketsHave.getCount(ScotlandYard.Ticket.BUS) >= ticketsNeeded[1]
                && ticketsHave.getCount(ScotlandYard.Ticket.UNDERGROUND) >= ticketsNeeded[2]) {
            return true;
        } else return false;
    }

    public static void main(String[] args) throws IOException {
        Integer a[] = new Integer[]{1, 8, 18, 43, 57, 73, 92, 93, 94, 95, 77, 96, 109, 124, 123, 122, 121, 120, 144, 177, 176, 189, 190, 192, 194, 195, 197, 184, 185, 198, 199, 171, 175, 162, 136, 119, 107, 91, 56, 42, 30, 17, 7, 6, 29, 16, 5, 15, 14, 13, 4, 3, 11, 10, 2, 20, 21, 33, 9};
        Arrays.sort(a);
        System.out.println(Arrays.toString(a));
        Integer b[] = new Integer[]{31, 19, 44, 58, 74, 75, 59, 76, 61, 78, 97, 98, 110, 111, 130, 139, 153, 152, 138, 150, 149, 148, 164, 147, 137, 146, 145, 163, 178, 191, 179, 165, 180, 193, 181, 182, 183, 196, 167, 168, 155, 156, 169, 157, 170, 159, 186, 172, 187, 188, 173, 174, 161, 135, 108, 105, 106, 90, 72, 71, 55, 54, 41, 28, 27, 26, 39, 25, 38, 24, 37, 23, 12, 22, 34, 47, 46, 45, 32};
        Arrays.sort(b);
        System.out.println(Arrays.toString(b));
        Integer c[] = new Integer[]{60, 62, 79, 63, 80, 99, 112, 125, 131, 114, 132, 140, 154, 133, 141, 158, 142, 128, 160, 143, 129, 117, 88, 89, 70, 69, 53, 40, 52, 51, 67, 50, 49, 36, 35, 48, 151, 166};
        Arrays.sort(c);
        System.out.println(Arrays.toString(c));
        Integer d[] = new Integer[]{64, 81, 100, 113, 101, 65, 66, 83, 102, 103, 85, 84, 68, 86, 87, 104, 116, 118, 134, 127, 126, 115, 82};
        Arrays.sort(d);
        System.out.println(Arrays.toString(d));

    }

}