import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import org.junit.BeforeClass;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.STANDARD24MOVES;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.readGraph;



public final class TestBase {

    private static ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> defaultGraph;
    @BeforeClass
    public static void setUp() {
        try {
            defaultGraph = readGraph(Resources.toString(Resources.getResource(
                            "graph.txt"),
                    StandardCharsets.UTF_8));
        } catch (IOException e) { throw new RuntimeException("Unable to read game graph", e); }
    }
    @Nonnull
    static ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> standardGraph() { return defaultGraph;}

    @Nonnull static GameSetup standard24MoveSetup() {
        return new GameSetup(defaultGraph, STANDARD24MOVES);
    }

    @Nonnull static ImmutableMap<ScotlandYard.Ticket, Integer> makeTickets(
            int taxi, int bus, int underground, int x2, int secret) {
        return ImmutableMap.of(
                TAXI, taxi,
                BUS, bus,
                UNDERGROUND, underground,
                ScotlandYard.Ticket.DOUBLE, x2,
                ScotlandYard.Ticket.SECRET, secret);
    }

    public TestBase() {
        try {
        defaultGraph = readGraph(Resources.toString(Resources.getResource(
                        "graph.txt"),
                StandardCharsets.UTF_8));
    } catch (IOException e) { throw new RuntimeException("Unable to read game graph", e); }}

    //THIS CODE ABOVE IS HERE TO ALLOW ME TO MAKE A STATE

}
