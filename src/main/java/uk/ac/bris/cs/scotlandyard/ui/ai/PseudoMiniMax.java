package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static uk.ac.bris.cs.scotlandyard.ui.ai.BestMoveConfiguration.bestMoveConfiguration;
import static uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeBuilder.destinationVisitor;
import static uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeBuilder.expandTree;
import static uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeEvaluator.closestDetectiveDistance;

public class PseudoMiniMax implements Ai {
    @Nonnull
    @Override
    public String name() {
        return "Pseudo MiniMax";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        Random rand = new Random();
        Board.GameState state = (Board.GameState) board;
        ImmutableSet<Move> availableMoves = state.getAvailableMoves();
        int depth = 2;
        //dictates how many levels deep we should go

        List<Integer> destinations = availableMoves.stream().map(x->x.accept(destinationVisitor)).distinct().toList();
        if(destinations.size()==1) return bestMoveConfiguration(state, availableMoves.asList().get(0));

        GameNode rootNode = new GameNode(state, availableMoves.asList().get(0).source(), Optional.empty(), true,null);

        expandTree(rootNode, depth);
        //this method expands the tree for as many rows as the depth variable tells it to

        GameNode maximisedMinimum = evaluateTree(rootNode);

        while(true) {
            if (maximisedMinimum.getParent()!=null) {
                if (maximisedMinimum.getParent().isRoot()) {
                    break;
                } else {
                    maximisedMinimum = maximisedMinimum.getParent();
                }
            }
        }
        //if the node isn't a child of the root, traverse up the tree until it is

        if(maximisedMinimum.getMoveChosen().isPresent()) {
            Move maxedMove = maximisedMinimum.getMoveChosen().get();
            return bestMoveConfiguration(state, maxedMove);
            //decides what ticket combination to use for this move
        } else {
            return availableMoves.asList().get(rand.nextInt(availableMoves.size()));
            //if an unforeseen error occurs, then a move will still be returned
        }

    }

    private static GameNode evaluateTree(GameNode node) {
        if (isLeaf(node)) return node;
        //gives a base case for the recursion
        else if (allChildrenAreLeaves(node)) {
            return evaluateLeavesParent(node);
        } else if (someChildrenAreLeaves(node)) {
            ArrayList<GameNode> children = node.getChildNodes();
            List<GameNode> nonLeafChildren = children.stream().filter(x -> !isLeaf(x)).toList();
            //if a node contains some leaves but not all, we ignore the leaf children and pretend it has only non-leaf children

            List<Integer> leafScores = nonLeafChildren.stream().map(x -> evaluateLeaf(evaluateTree(x))).toList();
            //gets the scores for the children of the node. it will keep recursing through evaluateTree until it hits a leaf
            //the recursion will then unravel
            int maxIndex = leafScores.indexOf(Collections.max(leafScores));
            //the maximum index should be chosen, since we are maximising the worst-case scenario
            return nonLeafChildren.get(maxIndex);

        } else {
            List<Integer> leafScores = node.getChildNodes().stream().map(x -> evaluateLeaf(evaluateTree(x))).toList();
            int maxIndex = leafScores.indexOf(Collections.max(leafScores));
            return node.getChildNodes().get(maxIndex);
        }
    }

    private static GameNode evaluateLeavesParent(GameNode node) {
        ArrayList<GameNode> children = node.getChildNodes();
        List<Integer> closestDetective = children.stream().map(PseudoMiniMax::evaluateLeaf).toList();
        return children.get(closestDetective.indexOf(Collections.min(closestDetective)));
        //when evaluating the parent of all leaves, we need to pick the smallest closestDetective. this represents the worst-case scenario
    }

    private static int evaluateLeaf(GameNode node) {
        if (node.getMoveChosen().isPresent()) return closestDetectiveDistance(node.getCurrentState(), node.getMoveChosen().get().accept(destinationVisitor));
        else return Integer.MAX_VALUE;
        //this gives the score of the leaf node. this is used to decide the worst-case scenario
    }

    private static boolean isLeaf(GameNode node) {
        return node.getChildNodes().isEmpty();
        //a node is a leaf if it has no children
    }

    private static boolean allChildrenAreLeaves(GameNode node) {
        boolean allChildrenAreLeaves = true;
        for(int i = 0; i < node.getChildNodes().size(); ++i) {
            allChildrenAreLeaves = allChildrenAreLeaves && isLeaf(node.getChildNodes().get(i));
        }
        return allChildrenAreLeaves;
    }

    private static boolean someChildrenAreLeaves(GameNode node) {
        boolean someChildrenAreLeaves = false;
        for(int i = 0; i < node.getChildNodes().size(); ++i) {
            someChildrenAreLeaves = someChildrenAreLeaves || isLeaf(node.getChildNodes().get(i));
        }
        return someChildrenAreLeaves;
    }
}
