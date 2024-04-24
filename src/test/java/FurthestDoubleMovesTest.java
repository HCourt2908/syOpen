import io.atlassian.fugue.Pair;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory;
import uk.ac.bris.cs.scotlandyard.model.Player;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.UNDERGROUND;

public class FurthestDoubleMovesTest {

    MyGameStateFactory stateFactory = new MyGameStateFactory();
    TestBase testBase = new TestBase();

    public static List<Move.DoubleMove> furthestDoubleMoves(List<Pair<Move.DoubleMove,Double>> moves) {
        List<Pair<Move.DoubleMove, Double>> similarPairs = new ArrayList<>();
        //creates a list that will store all the different configs of best move
        for (Pair<Move.DoubleMove, Double> pair : moves) {

            if (similarPairs.isEmpty()) similarPairs.add(pair);
                //if the list is empty -> by default this move must be best

            else if ((pair.left().destination2 == similarPairs.get(0).left().destination2)) {
                similarPairs.add(pair);
            }
            //if the next double move has the same destination2 as one of the moves in the list, it should be considered as well

            else if ((pair.right() > similarPairs.get(0).right())) {
                similarPairs.clear();
                similarPairs.add(pair);
            }
            //if we find a move with a higher average detective distance, get rid of the rest of them and start count again
        }

        return similarPairs.stream().map(Pair::left).toList();
    }

    @Test
    public void testReturnsAMoveIfOneChoice() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(1,1,1,1,1),1),
                new Player(BLUE, TestBase.makeTickets(1,1,1,0,0),2));
        List<Pair<Move.DoubleMove,Double>> moves = List.of(
                new Pair<>((new Move.DoubleMove(MRX,1,TAXI,9,TAXI,1)),1.0)
        );
        assert(furthestDoubleMoves(moves).equals(List.of(new Move.DoubleMove(MRX,1,TAXI,9,TAXI,1)))
                &&furthestDoubleMoves(moves).size()==1);
    }

    @Test
    public void testReturnsOneMoveIfTwoChoices() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(10,1,1,1,1),1),
                new Player(BLUE, TestBase.makeTickets(1,1,1,0,0),2));
        List<Pair<Move.DoubleMove,Double>> moves = List.of(
                new Pair<>(new Move.DoubleMove(MRX, 1, TAXI, 8,TAXI,18),1.0),
                new Pair<>(new Move.DoubleMove(MRX, 1, TAXI, 8,TAXI,1),2.0));
        assert(furthestDoubleMoves(moves).containsAll(List.of(new Move.DoubleMove(MRX, 1, TAXI, 8,TAXI,1)))&&furthestDoubleMoves(moves).size()==1);
        //the first move of the two has the higher score, so it, and only it should be in the list
    }

    @Test
    public void testReturnsTwoMovesOfSameScore() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(10,10,10,1,1),1),
                new Player(BLUE, TestBase.makeTickets(1,1,1,0,0),2));
        List<Pair<Move.DoubleMove,Double>> moves = List.of(
                new Pair<>(new Move.DoubleMove(MRX, 1, BUS, 46,BUS,1), 2.0),
                new Pair<>(new Move.DoubleMove(MRX, 1, UNDERGROUND, 46,UNDERGROUND,1), 2.0));
        assert(furthestDoubleMoves(moves).containsAll(List.of(
                new Move.DoubleMove(MRX,1,BUS,46,BUS,1),
                new Move.DoubleMove(MRX,1,UNDERGROUND,46,UNDERGROUND,1)))&&furthestDoubleMoves(moves).size()==2);
        //both moves have the same destination, so they should be both be included in the return list
    }

    @Test
    public void testReturnsTwoMovesIfThreeChoices() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(10,10,10,1,1),1),
                new Player(BLUE, TestBase.makeTickets(1,1,1,0,0),2));
        List<Pair<Move.DoubleMove,Double>> moves = List.of(
                new Pair<>(new Move.DoubleMove(MRX, 1, BUS, 46,BUS,1), 0.5),
                new Pair<>(new Move.DoubleMove(MRX, 1, TAXI, 8,TAXI,18), 1.0),
                new Pair<>(new Move.DoubleMove(MRX, 1, SECRET,8,TAXI,18),1.0));
        assert(furthestDoubleMoves(moves).containsAll(List.of(
                new Move.DoubleMove(MRX, 1, TAXI, 8,TAXI,18),
                new Move.DoubleMove(MRX, 1, SECRET,8,TAXI,18)))&&furthestDoubleMoves(moves).size()==2);
    }


}
