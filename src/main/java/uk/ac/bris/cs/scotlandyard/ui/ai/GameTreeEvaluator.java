package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.primitives.Ints.min;
import static java.lang.Math.max;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.UNDERGROUND;
import static uk.ac.bris.cs.scotlandyard.ui.ai.BestMoveConfiguration.bestMoveConfiguration;
import static uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeBuilder.destinationVisitor;

public class GameTreeEvaluator {

    public static Optional<Move> evaluateGameTree(GameNode rootNode) {
        //I decide which of the leaves will give me an optimal game state configuration and return that


        List<GameNode> leaves = getLeaves(rootNode);

        if (leaves.isEmpty()) {
            return Optional.empty();
        }

        GameNode bestLeaf = leaves.get(0);
        int bestLeafDistance = 0;
        if (bestLeaf.getMoveChosen().isPresent()) {
            bestLeafDistance = closestDetectiveDistance(bestLeaf.getCurrentState(), bestLeaf.getMoveChosen().get().accept(destinationVisitor));
        }

        for (GameNode leaf : leaves) {
            if (leaf.getMoveChosen().isPresent()) {
                int closestDetective = closestDetectiveDistance(leaf.getCurrentState(), leaf.getMoveChosen().get().accept(destinationVisitor));
                if (closestDetective > bestLeafDistance) {
                    bestLeafDistance = closestDetective;
                    bestLeaf = leaf;
                }
            }
        }
        //finds the leaf with the furthest closest detective

        while(true) {
            if(bestLeaf.getParent() != null) {
                if (!bestLeaf.getParent().isRoot()) {
                    bestLeaf = bestLeaf.getParent();
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        //follows the game tree upwards until we reach the node with the root as its parent. this is the move we choose

        if (bestLeaf.getMoveChosen().isPresent()) {
            return Optional.of(bestMoveConfiguration(rootNode.getCurrentState(), bestLeaf.getMoveChosen().get()));
        } else return Optional.empty();

    }

    public static int closestDetectiveDistance(Board.GameState state, int destination) {

        List<Piece> detectivePieces  = new ArrayList<>(state.getPlayers().stream().toList());
        detectivePieces.remove(MRX);

        int closestDetective = Integer.MAX_VALUE;
        AdvancedDijkstra distanceFinder = new AdvancedDijkstra(state);
        for (Piece piece : detectivePieces) {
            int detectiveLocation = state.getDetectiveLocation((Piece.Detective) piece).get();
            closestDetective = min(closestDetective, distanceFinder.shortestPath(detectiveLocation, destination, piece).size());
        }

        return closestDetective;
    }

    private static List<GameNode> getLeaves(GameNode rootNode) {
        List<GameNode> nodes = new ArrayList<>();
        nodes.add(rootNode);

        while(!allLeaves(nodes)) {
            for(int i = 0; i < nodes.size(); i++) {
                //traverses the list until it finds a non-leaf node
                if (!nodes.get(i).getChildNodes().isEmpty()) {
                    nodes.addAll(nodes.get(i).getChildNodes());
                    nodes.remove(i);
                    //removes the non-leaf node and replaces it with it's children
                }
            }
        }

        return nodes;
    }

    private static boolean allLeaves(List<GameNode> nodes) {
        boolean allLeaves = true;
        for (GameNode node : nodes) {
            allLeaves = allLeaves && node.getChildNodes().isEmpty();
        }
        //if a node is a leaf, it will have no child nodes
        return allLeaves;
    }

}
