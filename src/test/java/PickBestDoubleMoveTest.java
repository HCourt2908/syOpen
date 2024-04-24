import io.atlassian.fugue.Pair;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.List;

import static java.lang.Math.max;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.BLUE;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

public class PickBestDoubleMoveTest {

    MyGameStateFactory stateFactory = new MyGameStateFactory();
    TestBase testBase = new TestBase();

    public static Move.DoubleMove pickBestDoubleMove(List<Move.DoubleMove> moves, Board.GameState state) {

        List<Pair<ScotlandYard.Ticket, ScotlandYard.Ticket>> tickets =
                moves.stream().map(move -> new Pair<ScotlandYard.Ticket, ScotlandYard.Ticket>(move.ticket1,move.ticket2)).toList();
        //takes the move list and transforms it into a list of tickets using streams.

        if (state.getSetup().moves.get(max(0,state.getMrXTravelLog().size()-1))) {
            List<ScotlandYard.Ticket> secretTicketFirst = tickets.stream().filter(x -> x.left()==SECRET).map(Pair::right).toList();
            //transforms a list of ticket pairs into a list of ticket2's where ticket1 is a secret
            if (!secretTicketFirst.isEmpty()) {
                int bestSecondaryTicket = IdealIndexTest.idealIndex(secretTicketFirst);
                Pair<ScotlandYard.Ticket, ScotlandYard.Ticket> idealTicketCombo = new Pair<>(SECRET, secretTicketFirst.get(bestSecondaryTicket));
                return moves.get(tickets.indexOf(idealTicketCombo));
                //decides the best move, where the first ticket is a secret one
            }
        }
        if (state.getSetup().moves.get(max(0,state.getMrXTravelLog().size()))) {
            List<ScotlandYard.Ticket> secretTicketSecond = tickets.stream().filter(x -> x.right()==SECRET).map(Pair::left).toList();
            //transforms a list of ticket pairs into a list of ticket1's where ticket2 is a secret
            if(!secretTicketSecond.isEmpty()) {
                int bestPrimaryTicket = IdealIndexTest.idealIndex(secretTicketSecond);
                Pair<ScotlandYard.Ticket, ScotlandYard.Ticket> idealTicketCombo = new Pair<>(secretTicketSecond.get(bestPrimaryTicket), SECRET);
                return moves.get(tickets.indexOf(idealTicketCombo));
                //decides the best move, where the second ticket is a secret one
            }
        }

        //if the program reaches here then it must mean that we will not interact at all with a reveal turn
        //Or that we don't have any secret tickets for the reveal turn
        List<ScotlandYard.Ticket> firstTickets = tickets.stream().map(Pair::left).toList();
        int idealFirstIndex = IdealIndexTest.idealIndex(firstTickets);
        //now I know the best ticket to use for the initial move
        List<ScotlandYard.Ticket> secondTickets = tickets.stream()
                .filter(x -> x.left()== firstTickets.get(idealFirstIndex))
                .map(Pair::right).toList();
        //creates a list of ticket2's where the first ticket is the ideal ticket we just found
        int idealSecondIndex = IdealIndexTest.idealIndex(secondTickets);
        //now I know the best ticket pair to use
        Pair<ScotlandYard.Ticket, ScotlandYard.Ticket> idealTicketCombo = new Pair<>(firstTickets.get(idealFirstIndex), secondTickets.get(idealSecondIndex));
        return moves.get(tickets.indexOf(idealTicketCombo));

    }

    @Test
    public void testPicksAMoveIfOneChoice() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX,TestBase.makeTickets(2,1,1,1,1),1),
                new Player(BLUE,TestBase.makeTickets(1,1,1,0,0),2));
        List<Move.DoubleMove> moves1 = List.of(
                new Move.DoubleMove(MRX, 1,TAXI,8,TAXI,18)
        );
        assert(pickBestDoubleMove(moves1, state1).equals(new Move.DoubleMove(MRX,1,TAXI,8,TAXI,18)));

        Board.GameState state2 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX,TestBase.makeTickets(2,1,1,1,1),1),
                new Player(BLUE,TestBase.makeTickets(1,1,1,0,0),2));
        List<Move.DoubleMove> moves2 = List.of(
                new Move.DoubleMove(MRX, 1,TAXI,8,SECRET,18));
        assert(pickBestDoubleMove(moves2, state2).equals(new Move.DoubleMove(MRX,1,TAXI,8,SECRET,18)));
    }

    @Test
    public void testPicksAMoveIfTwoChoices() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX,TestBase.makeTickets(2,1,1,1,1),1),
                new Player(BLUE,TestBase.makeTickets(1,1,1,0,0),2));
        List<Move.DoubleMove> moves1 = List.of(
                new Move.DoubleMove(MRX, 1,TAXI,8,SECRET,18),
                new Move.DoubleMove(MRX, 1,TAXI,8,TAXI,18)
        );
        assert(pickBestDoubleMove(moves1, state1).equals(new Move.DoubleMove(MRX,1,TAXI,8,TAXI,18)));
        //double taxi takes priority over taxi->secret

        Board.GameState state2 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX,TestBase.makeTickets(2,1,1,1,1),1),
                new Player(BLUE,TestBase.makeTickets(1,1,1,0,0),2));
        List<Move.DoubleMove> moves2 = List.of(
                new Move.DoubleMove(MRX, 1,UNDERGROUND,46,SECRET,58),
                new Move.DoubleMove(MRX, 1,UNDERGROUND,46,BUS,58)
        );
        assert(pickBestDoubleMove(moves2, state2).equals(new Move.DoubleMove(MRX,1,UNDERGROUND,46,SECRET,58)));
    }

    @Test
    public void testFirstMoveHiddenIfJustRevealed() {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX,TestBase.makeTickets(10,10,10,5,5),1),
                new Player(BLUE,TestBase.makeTickets(10,10,10,0,0),2));
        state1 = state1.advance(new Move.DoubleMove(MRX,1,TAXI,9,TAXI,1));
        state1 = state1.advance(new Move.SingleMove(BLUE,2,TAXI,10));
        state1 = state1.advance(new Move.SingleMove(MRX, 1, TAXI, 9));
        state1 = state1.advance(new Move.SingleMove(BLUE,10,TAXI,2));
        //this last move of Mr X's has been revealed

        List<Move.DoubleMove> moves1 = List.of(
                new Move.DoubleMove(MRX, 9,TAXI,1,TAXI,9),
                new Move.DoubleMove(MRX,9,SECRET,1,TAXI,9)
        );
        assert(pickBestDoubleMove(moves1, state1).equals(new Move.DoubleMove(MRX,9,SECRET,1,TAXI,9)));
    }

    @Test
    public void testSecondMoveHiddenIfJustRevealed() {} {
        Board.GameState state1 = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, TestBase.makeTickets(10,10,10,5,5),1),
                new Player(BLUE,TestBase.makeTickets(10,10,10,0,0),2));
        state1 = state1.advance(new Move.DoubleMove(MRX,1,TAXI,9,TAXI,1));
        state1 = state1.advance(new Move.SingleMove(BLUE,2,TAXI,10));
        //mr x has made two moves. If he makes a double move now, the first of the two moves will be revealed

        List<Move.DoubleMove> moves1 = List.of(
                new Move.DoubleMove(MRX, 1,TAXI,9,TAXI,1),
                new Move.DoubleMove(MRX, 1,TAXI,9,SECRET,1),
                new Move.DoubleMove(MRX, 1,SECRET,9,SECRET,1),
                new Move.DoubleMove(MRX, 1,SECRET,9,TAXI,1));

        assert(pickBestDoubleMove(moves1,state1).equals(new Move.DoubleMove(MRX,1,TAXI,9,SECRET,1)));
    }
}




