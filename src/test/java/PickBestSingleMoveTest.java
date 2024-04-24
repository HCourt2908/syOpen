import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.max;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.STANDARD24MOVES;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.UNDERGROUND;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.readGraph;

public class PickBestSingleMoveTest {

    MyGameStateFactory stateFactory = new MyGameStateFactory();
    TestBase testBase = new TestBase();

    public static Move.SingleMove pickBestSingleMove(List<Move.SingleMove> moves, Board.GameState state) {

        List<ScotlandYard.Ticket> tickets = moves.stream().map(move -> move.ticket).toList();
        //takes the move list and transforms it into a list of tickets using streams.

        if (tickets.contains(SECRET) && state.getSetup().moves.get(max(0,(state.getMrXTravelLog().size())-1))) {
            //the max function is there to avoid doing a query for an index -1
            return moves.get(tickets.indexOf(SECRET));
            //if mr x has just made a reveal turn, I want to reveal as little info as possible about where I am going next
        } else {
            return moves.get(IdealIndexTest.idealIndex(tickets));
        }

    }

    @Test
    public void testPicksAMoveIfOneChoice() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(1,0,0,0,0),1),
                new Player(BLUE, TestBase.makeTickets(0,0,0,0,0), 5));

        List<Move.SingleMove> moves1 = Arrays.asList(
                new Move.SingleMove(MRX, 1, TAXI, 8)
        );
        assert(pickBestSingleMove(moves1, state1).equals(new Move.SingleMove(MRX, 1, TAXI, 8)));
        //checks that the algorithm will actually return a move, and the correct one

        Board.GameState state2 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(0,0,0,0,1),1),
                new Player(BLUE, TestBase.makeTickets(0,0,0,0,0), 5));

        List<Move.SingleMove> moves2 = Arrays.asList(
                new Move.SingleMove(MRX, 1, SECRET, 8)
        );
        assert(pickBestSingleMove(moves2, state2).equals(new Move.SingleMove(MRX, 1, SECRET, 8)));
        //checks that the algorithm will pick the one move provided if it is not a taxi move
    }

    @Test
    public void testPicksBetterMoveOfTwo() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(1,0,0,0,1),1),
                new Player(BLUE, TestBase.makeTickets(0,0,0,0,0), 5));

        List<Move.SingleMove> moves1 = Arrays.asList(
                new Move.SingleMove(MRX, 1, SECRET, 8),
                new Move.SingleMove(MRX, 1, TAXI, 8)
        );
        assert(pickBestSingleMove(moves1,state1).equals(new Move.SingleMove(MRX, 1, TAXI, 8)));
        //checks that taxi has higher priority over secret

        Board.GameState state2 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(0,1,1,0,0),1),
                new Player(BLUE, TestBase.makeTickets(0,0,0,0,0), 5));

        List<Move.SingleMove> moves2 = Arrays.asList(
                new Move.SingleMove(MRX, 1, BUS, 46),
                new Move.SingleMove(MRX, 1, UNDERGROUND, 46)
        );
        assert(pickBestSingleMove(moves2,state2).equals(new Move.SingleMove(MRX, 1, BUS, 46)));
        //checks that bus has a higher priority over underground

        Board.GameState state3 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(0,1,0,0,1),1),
                new Player(BLUE, TestBase.makeTickets(0,0,0,0,0), 5));

        List<Move.SingleMove> moves3 = Arrays.asList(
                new Move.SingleMove(MRX, 1, BUS, 58),
                new Move.SingleMove(MRX, 1, SECRET, 58)
        );
        assert(pickBestSingleMove(moves3,state3).equals(new Move.SingleMove(MRX, 1, SECRET, 58)));

    }

    @Test
    public void testPicksSecretIfJustRevealed() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(1,1,1,1,1),1),
                new Player(BLUE, TestBase.makeTickets(5,0,0,0,0), 2));

        state1 = state1.advance(new Move.SingleMove(MRX, 1, TAXI, 9));
        state1 = state1.advance(new Move.SingleMove(BLUE, 2, TAXI, 20));
        state1 = state1.advance(new Move.SingleMove(MRX, 9, TAXI, 1));
        state1 = state1.advance(new Move.SingleMove(BLUE, 20, TAXI, 2));
        state1 = state1.advance(new Move.SingleMove(MRX, 1, TAXI, 9));
        state1 = state1.advance(new Move.SingleMove(BLUE, 2, TAXI, 20));
        //now, three turns have passed. mr x has just revealed his position

        List<Move.SingleMove> moves1 = Arrays.asList(
                new Move.SingleMove(MRX, 9, TAXI, 1),
                new Move.SingleMove(MRX, 9, SECRET, 1)
        );
        assert(pickBestSingleMove(moves1, state1).equals(new Move.SingleMove(MRX, 9, SECRET, 1)));
        //this tests that mr x should use a secret ticket after he has just revealed his whereabouts, even if he has a taxi

        Board.GameState state2 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(1,1,1,1,1),9),
                new Player(BLUE, TestBase.makeTickets(5,0,0,0,0), 2));

        state2 = state2.advance(new Move.SingleMove(MRX, 9, TAXI, 1));
        state2 = state2.advance(new Move.SingleMove(BLUE, 2, TAXI, 20));
        state2 = state2.advance(new Move.SingleMove(MRX, 1, TAXI, 9));
        state2 = state2.advance(new Move.SingleMove(BLUE, 20, TAXI, 2));
        state2 = state2.advance(new Move.SingleMove(MRX, 9, TAXI, 1));
        state2 = state2.advance(new Move.SingleMove(BLUE, 2, TAXI, 20));
        //now, three turns have passed. mr x has just revealed his position

        List<Move.SingleMove> moves2 = Arrays.asList(
                new Move.SingleMove(MRX, 1, UNDERGROUND, 46),
                new Move.SingleMove(MRX, 1, SECRET, 46)
        );
        assert(pickBestSingleMove(moves2,state2).equals(new Move.SingleMove(MRX, 1, SECRET, 46)));

    }



}
