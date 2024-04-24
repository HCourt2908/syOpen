import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AdvancedDijkstraTest.class,
        IdealIndexTest.class,
        SimpleDijkstraTest.class,
        PickBestSingleMoveTest.class,
        FurthestSingleMovesTest.class,
        PickBestDoubleMoveTest.class,
        FurthestDoubleMovesTest.class
})


public class AllTest { }
