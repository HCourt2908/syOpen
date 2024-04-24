import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.Arrays;
import java.util.List;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.UNDERGROUND;

public class IdealIndexTest {

    public static int idealIndex(List<ScotlandYard.Ticket> ticketList) {
        if (ticketList.contains(TAXI)) return ticketList.indexOf(TAXI);
        else if (ticketList.contains(SECRET)) return ticketList.indexOf(SECRET);
        else if (ticketList.contains(BUS)) return ticketList.indexOf(BUS);
        else if (ticketList.contains(UNDERGROUND)) return ticketList.indexOf(UNDERGROUND);
        else return 0;
        //this line should never be reached, but it felt wrong to put an 'else' at the end instead of an 'else if underground'

        //I chose this order due to order of usefulness:
        //a taxi ticket reveals little and mr x has many, a secret ticket reveals nothing but mr x has few, a bus ticket reveals more, underground reveals most
    }

    @Test
    public void testIdealIndex() {
        List<ScotlandYard.Ticket> ticketList1 = Arrays.asList(TAXI, SECRET, BUS, UNDERGROUND);
        assert(idealIndex(ticketList1) == 0);
        //should return index of taxi
        List<ScotlandYard.Ticket> ticketList2 = Arrays.asList(SECRET, BUS, UNDERGROUND);
        assert(idealIndex(ticketList2) == 0);
        List<ScotlandYard.Ticket> ticketList3 = Arrays.asList(BUS, UNDERGROUND);
        assert(idealIndex(ticketList3) == 0);
        List<ScotlandYard.Ticket> ticketList4 = Arrays.asList(UNDERGROUND);
        assert(idealIndex(ticketList4) == 0);
    }
}
