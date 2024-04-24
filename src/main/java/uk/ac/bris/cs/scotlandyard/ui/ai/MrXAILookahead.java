package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeBuilder.*;
import static uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeEvaluator.evaluateGameTree;

public class MrXAILookahead implements Ai {
    @Nonnull
    @Override
    public String name() {
        return "Mr X AI - Lookahead";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        Random rand = new Random();
        //should return the best possible move, considering future turns also
        Board.GameState state = (Board.GameState) board;
        ImmutableSet<Move> availableMoves = state.getAvailableMoves();
        int depth = 2;
        //the depth variable dictates how many moves the program should 'look ahead'
        GameNode rootNode = new GameNode(state, availableMoves.asList().get(0).source(), Optional.empty(), true,null);

        expandTree(rootNode, depth);
        //this method expands the tree for as many rows as the depth variable tells it to

        Optional<Move> evaluatedTree = evaluateGameTree(rootNode);
        return evaluatedTree.orElseGet(() -> availableMoves.asList().get(rand.nextInt(availableMoves.size())));
        //should only every return an optional.empty() when all moves lead to the end of a game
    }



}
