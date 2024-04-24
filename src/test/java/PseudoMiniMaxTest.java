import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.GameNode;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;
import static uk.ac.bris.cs.scotlandyard.ui.ai.BestMoveConfiguration.bestMoveConfiguration;
import static uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeBuilder.destinationVisitor;
import static uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeBuilder.expandTree;
import static uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeEvaluator.closestDetectiveDistance;

public class PseudoMiniMaxTest implements Ai {
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

        if(maximisedMinimum.getMoveChosen().isPresent()) {
            Move maxedMove = maximisedMinimum.getMoveChosen().get();
            System.out.println(maxedMove);
            return bestMoveConfiguration(state, maxedMove);
        } else {
            System.out.println("womp womp");
            return availableMoves.asList().get(rand.nextInt(availableMoves.size()));
        }

    }

    private static GameNode evaluateTree(GameNode node) {
        if (isLeaf(node)) return node;
        else if (allChildrenAreLeaves(node)) {
            return evaluateLeavesParent(node);
        } else if (someChildrenAreLeaves(node)) {
            ArrayList<GameNode> children = node.getChildNodes();
            List<GameNode> nonLeafChildren = children.stream().filter(x -> !isLeaf(x)).toList();

            List<Integer> leafScores = nonLeafChildren.stream().map(x -> evaluateLeaf(evaluateTree(x))).toList();
            int maxIndex = leafScores.indexOf(Collections.max(leafScores));
            return nonLeafChildren.get(maxIndex);
        } else {
            List<Integer> leafScores = node.getChildNodes().stream().map(x -> evaluateLeaf(evaluateTree(x))).toList();
            int maxIndex = leafScores.indexOf(Collections.max(leafScores));
            return node.getChildNodes().get(maxIndex);
        }
    }

    private static GameNode evaluateLeavesParent(GameNode node) {
        ArrayList<GameNode> children = node.getChildNodes();
        List<Integer> closestDetective = children.stream().map(PseudoMiniMaxTest::evaluateLeaf).toList();
        return children.get(closestDetective.indexOf(Collections.min(closestDetective)));
    }

    private static int evaluateLeaf(GameNode node) {
        if (node.getMoveChosen().isPresent()) return closestDetectiveDistance(node.getCurrentState(), node.getMoveChosen().get().accept(destinationVisitor));
        else return Integer.MAX_VALUE;
    }

    private static boolean isLeaf(GameNode node) {
        return node.getChildNodes().isEmpty();
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

    public static void main(String[] args) {
        TestBase testBase = new TestBase();
        MyGameStateFactory stateFactory = new MyGameStateFactory();
        Board.GameState state = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, ScotlandYard.defaultMrXTickets(),1),
                new Player(BLUE, defaultDetectiveTickets(), 2));
        GameNode root = new GameNode(state, 1, Optional.empty(),true,null);
        expandTree(root,2);
    }
}
