import io.atlassian.fugue.Pair;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.STANDARD24MOVES;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.readGraph;

public class FurthestSingleMovesTest {

    MyGameStateFactory stateFactory = new MyGameStateFactory();
    TestBase testBase = new TestBase();

    public static List<Move.SingleMove> furthestSingleMoves(List<Pair<Move.SingleMove,Double>> moves) {
        List<Pair<Move.SingleMove, Double>> similarPairs = new ArrayList<>();
        //creates a list that will store all the different configurations of the best move (differing ticket types etc.)
        for (Pair<Move.SingleMove, Double> pair : moves) {

            if (similarPairs.isEmpty()) similarPairs.add(pair);
            //if the list is empty then by default this next move is the best

            else if ((pair.left()).destination == similarPairs.get(0).left().destination) {
                similarPairs.add(pair);
            }
            //if the next move has the same destination as one of the moves in the list, it must be the same move with different transport

            else if ((pair.right() > similarPairs.get(0).right())) {
                similarPairs.clear();
                similarPairs.add(pair);
            }
            //if we find a move with a higher average detective distance, get rid of the rest of them and start count again
        }

        //using streams, I can convert a list of tuples to a list of single moves, which is what I want
        return similarPairs.stream().map(Pair::left).toList();
    }

    @Test
    public void testReturnsAMoveIfOneChoice() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(1,1,1,1,1),1),
                new Player(BLUE, TestBase.makeTickets(1,1,1,0,0), 2));
        List<Pair<Move.SingleMove,Double>> moves = List.of(
                new Pair<>(new Move.SingleMove(MRX,1,TAXI,8),1.0));
        assert(furthestSingleMoves(moves).containsAll(List.of(new Move.SingleMove(MRX,1,TAXI,8)))&&furthestSingleMoves(moves).size()==1);
        //since I only provided one move, it should return this exact move and only this exact move
    }

    @Test
    public void testReturnsOneMoveIfTwoChoices() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(1,1,1,1,1),1),
                new Player(BLUE, TestBase.makeTickets(1,1,1,0,0),2));
        List<Pair<Move.SingleMove,Double>> moves = List.of(
                new Pair<>(new Move.SingleMove(MRX, 1, TAXI, 8),2.0),
                new Pair<>(new Move.SingleMove(MRX, 1, TAXI, 9),1.0));
        assert(furthestSingleMoves(moves).containsAll(List.of(new Move.SingleMove(MRX, 1, TAXI, 8)))&&furthestSingleMoves(moves).size()==1);
        //the first move of the two has the higher score, so it, and only it should be in the list
    }

    @Test
    public void testReturnsTwoMovesOfSameScore() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(1,1,1,1,1),1),
                new Player(BLUE, TestBase.makeTickets(1,1,1,0,0),2));
        List<Pair<Move.SingleMove,Double>> moves = List.of(
                new Pair<>(new Move.SingleMove(MRX, 1, BUS, 46), 2.0),
                new Pair<>(new Move.SingleMove(MRX, 1, UNDERGROUND, 46), 2.0));
        assert(furthestSingleMoves(moves).containsAll(List.of(
                new Move.SingleMove(MRX,1,BUS,46),
                new Move.SingleMove(MRX,1,UNDERGROUND,46)))&&furthestSingleMoves(moves).size()==2);
        //both moves have the same destination, so they should be both be included in the return list
    }

    @Test
    public void testReturnsTwoMovesIfThreeChoices() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(1,1,1,1,1),1),
                new Player(BLUE, TestBase.makeTickets(1,1,1,0,0),2));
        List<Pair<Move.SingleMove,Double>> moves = List.of(
                new Pair<>(new Move.SingleMove(MRX, 1, BUS, 46), 0.5),
                new Pair<>(new Move.SingleMove(MRX, 1, TAXI, 8), 1.0),
                new Pair<>(new Move.SingleMove(MRX, 1, SECRET,8),1.0));
        assert(furthestSingleMoves(moves).containsAll(List.of(
                new Move.SingleMove(MRX, 1, TAXI, 8),
                new Move.SingleMove(MRX, 1, SECRET,8)))&&furthestSingleMoves(moves).size()==2);
    }
}
