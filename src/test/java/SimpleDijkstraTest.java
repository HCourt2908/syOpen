import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.SimpleDijkstra;

import java.io.IOException;
import java.util.Arrays;

public class SimpleDijkstraTest {

    @Test
    public void shortestPathAdjacentNodes() throws IOException {
        SimpleDijkstra d = new SimpleDijkstra(ScotlandYard.standardGraph());
        assert (d.shortestPath(1,1).equals(Arrays.asList()));
        //a path between a node and itself should be empty
        assert (d.shortestPath(1,8).equals(Arrays.asList(1)));
        //path between 1 and 8 should just contain '1', since the shortest path is: 1->8
        assert (d.shortestPath(148,149).equals(Arrays.asList(148)));
        //path between 148 and 149 should just contain '148' since the shortest path is: 148->149
    }

    @Test
    public void shortestPathSeparationOfTwo() throws IOException {
        SimpleDijkstra d = new SimpleDijkstra(ScotlandYard.standardGraph());
        assert (d.shortestPath(1,18).equals(Arrays.asList(1,8)));
        //the shortest path between 1 and 18 is: 1->8->18
        assert (d.shortestPath(2,33).equals(Arrays.asList(2,20)));
        //the shortest path between 2 and 33 is: 2->20->33
        assert (d.shortestPath(61,48).equals(Arrays.asList(61,62)));
        //the shortest path between 61 and 48 is: 61->62->48
    }

    @Test
    public void shortestPathMultipleTransportMethods() throws IOException {
        SimpleDijkstra d = new SimpleDijkstra(ScotlandYard.standardGraph());
        assert(d.shortestPath(1,46).equals(Arrays.asList(1)));
        //to get from 1 to 46, you could do 1->9->46 by taxi, but you can also do 1->46 by underground
        assert(d.shortestPath(34,63).equals(Arrays.asList(34)));
        //to get from 34 to 63, you could do 34->48->63 by taxi, but you can also do 34->63 by bus
        assert(!d.shortestPath(194,157).equals(Arrays.asList(194)));
        //I am choosing not to count ferry tickets as valid transport methods, since this implementation of dijkstra
        //will be for calculating the path a detective has to take to get to mr X. Detectives can't use the ferry.
    }

    @Test
    public void randomShortestPathTests() throws IOException {
        //a collection of shortest path calculations between two fairly arbitrarily chosen nodes
        //important to note that there can be multiple ways to get from a to b in the same number of shortest moves
        SimpleDijkstra d = new SimpleDijkstra(ScotlandYard.standardGraph());
        assert(d.shortestPath(1,63).size() == 3);
        //potential shortest path from 1 to 63: 1->46->79 via underground then 79->63 by taxi or bus
        assert(d.shortestPath(102,55).size() == 3);
        //potential shortest path from 102 to 55: 102->67 via bus/taxi,67->89 via underground, 89->55 via bus
        assert(d.shortestPath(164,138).size() == 4);
        //potential shortest paths from 164->138:
        //164->163->153->152->138, 164->148->149->150->138, 164->165->123->124->138
    }


}
