import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.AdvancedDijkstra;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;


public class AdvancedDijkstraTest {

    MyGameStateFactory stateFactory = new MyGameStateFactory();
    private TestBase testBase= new TestBase();

    //default detective tickets: taxi=11,bus=8,underground=4
    //default mr x tickets: taxi=4,bus=3,underground=3,double=2,secret=5

    //These first initial tests are the same as the SimpleDijkstraTest tests
    //this is to ensure that the base functionality of Dijkstra still works

    @Test
    public void shortestPathAdjacentNodes() {
        Board.GameState state = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, defaultMrXTickets(), 2),
                new Player(RED, defaultDetectiveTickets(), 1));
        AdvancedDijkstra d = new AdvancedDijkstra(state);
        assert(d.shortestPath(1, 1, RED).isEmpty());
        assert(d.shortestPath(1,8, RED).equals(List.of(1)));
        assert(d.shortestPath(148,149,RED).equals(List.of(148)));
    }

    @Test
    public void shortestPathSeparationOfTwo() {
        Board.GameState state = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, defaultMrXTickets(), 2),
                new Player(RED, defaultDetectiveTickets(), 1));
        AdvancedDijkstra d = new AdvancedDijkstra(state);
        assert(d.shortestPath(1,18,RED).equals(Arrays.asList(1,8)));
        assert(d.shortestPath(2,33,RED).equals(Arrays.asList(2,20)));
        assert(d.shortestPath(61,48,RED).equals(Arrays.asList(61,62)));
    }

    @Test
    public void shortestPathMultipleTransportMethods() {
        Board.GameState state = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, defaultMrXTickets(), 2),
                new Player(RED, defaultDetectiveTickets(),1));
        AdvancedDijkstra d = new AdvancedDijkstra(state);
        assert(d.shortestPath(1,46,RED).equals(Arrays.asList(1)));
        assert(d.shortestPath(34,63,RED).equals(Arrays.asList(34)));
        assert(!d.shortestPath(194,157,RED).equals(Arrays.asList(194)));
    }

    @Test
    public void randomShortestPathTest() {
        Board.GameState state = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, defaultMrXTickets(), 2),
                new Player(RED, defaultDetectiveTickets(),1));
        AdvancedDijkstra d = new AdvancedDijkstra(state);
        assert(d.shortestPath(1,63,RED).size() == 3);
        assert(d.shortestPath(102,55,RED).size() == 3);
        assert(d.shortestPath(164,138,RED).size() == 4);
    }

    @Test
    public void impossibleMoveTest() {
        Board.GameState state = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, defaultMrXTickets(),2),
                new Player(RED, TestBase.makeTickets(1,0,0,0,0),1),
                new Player(BLUE, TestBase.makeTickets(1,0,0,0,0),74),
                new Player(YELLOW, TestBase.makeTickets(2,1,0,0,0),15),
                new Player(GREEN, TestBase.makeTickets(0,1,1,0,0), 133));
        AdvancedDijkstra d = new AdvancedDijkstra(state);
        assert(d.shortestPath(1,18,RED).equals(List.of(-1)));
        assert(d.shortestPath(74,94,BLUE).equals(List.of(-1)));
        assert(d.shortestPath(15,87,YELLOW).equals(List.of(-1)));
        assert(d.shortestPath(133,105,GREEN).equals(List.of(-1)));
    }
    //I have purposely set the ticket values to what they are to simulate the detective being low on tickets.
    //none of the amount of tickets I have given will allow the detective to move from a to b.

    @Test
    public void possibleMovesInOneConfigTest() {
        Board.GameState state = stateFactory.build(TestBase.standard24MoveSetup(),
                new Player(MRX, defaultMrXTickets(),2),
                new Player(RED, TestBase.makeTickets(1,1,1,0,0),133),
                new Player(BLUE, TestBase.makeTickets(1,1,0,0,0),123),
                new Player(YELLOW, TestBase.makeTickets(1,0,1,0,0),111));
        AdvancedDijkstra d = new AdvancedDijkstra(state);
        assert(d.shortestPath(133,88,RED).equals(Arrays.asList(133,140,89)));
        //this checks that node pairs with multiple transport types still result in valid moves if possible
        //this is a valid move if the detective uses bus->underground->taxi, but not if the detective uses a taxi ticket first
        assert(d.shortestPath(123,95,BLUE).equals(Arrays.asList(123,122)));
        //this is possible if: bus->taxi but not taxi->bus
        assert(d.shortestPath(111,98,YELLOW).equals(Arrays.asList(111,79)));
        //this is possible only if we use the underground ticket first, then a taxi, not using a taxi first
    }

}
