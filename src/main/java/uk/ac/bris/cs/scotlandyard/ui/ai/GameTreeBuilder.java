package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.primitives.Ints.min;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;

public class GameTreeBuilder {

    public static Move.Visitor<Integer> destinationVisitor = new Move.Visitor<Integer>() {
        @Override
        public Integer visit(Move.SingleMove move) {
            return move.destination;
        }

        @Override
        public Integer visit(Move.DoubleMove move) {
            return move.destination2;
        }
    };
    //this visitor will return the final destination of a move regardless of its class

    public static List<Move> goodMoveFilter(ImmutableSet<Move> availableMoves, Board.GameState state) {
        List<Move> goodSingleMoves = new ArrayList<>();
        List<Move> goodDoubleMoves = new ArrayList<>();
        //creates two lists to store the single and double moves I deem to be 'good'
        List<Pair<Move,Integer>> okaySingleMoves = new ArrayList<>();
        List<Pair<Move,Integer>> okayDoubleMoves = new ArrayList<>();
        //creates two lists to store the single and double moves I deem to be 'okay'.
        //this set will only be returned if every single move has at least one detective one move away from the destination
        AdvancedDijkstra distanceFinder = new AdvancedDijkstra(state);
        //initialises the distance finder class
        List<Piece> detectivePieces  = new ArrayList<>(state.getPlayers().stream().toList());
        detectivePieces.remove(MRX);
        //creates a list of detective pieces. I will iterate over this to find all their distances to the destination of the move

        for(Move move : availableMoves) {
            int badFlag = 0;
            int moveDestination = move.accept(destinationVisitor);
            for (Piece piece : detectivePieces) {
                int detectiveLocation = state.getDetectiveLocation((Piece.Detective) piece).get();
                ArrayList<Integer> detectiveDestinationRoute = distanceFinder.shortestPath(detectiveLocation,moveDestination,piece);
                //gets the shortest route the detective can take to get to the move destination
                //if it contains -1, it means the detective cannot reach this destination with the tickets they have

                if (!detectiveDestinationRoute.contains(-1)) {
                    int detectiveDestinationDistance = detectiveDestinationRoute.size();
                    if (detectiveDestinationDistance == 1) {
                        badFlag++;
                    }
                }
            }

            if (move instanceof Move.SingleMove) {

                if(badFlag==0) {
                    goodSingleMoves.add(move);
                } else if (okaySingleMoves.isEmpty()) {
                    okaySingleMoves.add(new Pair<>(move,badFlag));
                } else if (okaySingleMoves.get(0).right() > badFlag) {
                    okaySingleMoves.clear();
                    okaySingleMoves.add(new Pair<>(move,badFlag));
                } else {
                    okaySingleMoves.add(new Pair<>(move,badFlag));
                }

            } else if (move instanceof Move.DoubleMove) {

                if (badFlag==0) {
                    goodDoubleMoves.add(move);
                } else if (okayDoubleMoves.isEmpty()) {
                    okayDoubleMoves.add(new Pair<>(move,badFlag));
                } else if (okayDoubleMoves.get(0).right() > badFlag) {
                    okayDoubleMoves.clear();
                    okayDoubleMoves.add(new Pair<>(move,badFlag));
                } else {
                    okayDoubleMoves.add(new Pair<>(move,badFlag));
                }

            }
        }

        if (goodSingleMoves.isEmpty()) {
            if (!goodDoubleMoves.isEmpty()) {
                return goodDoubleMoves;
            } else if (okayDoubleMoves.isEmpty() && !okaySingleMoves.isEmpty()) {
                return okaySingleMoves.stream().map(Pair::left).toList();
            } else if (!okayDoubleMoves.isEmpty() && okaySingleMoves.isEmpty()) {
                return okayDoubleMoves.stream().map(Pair::left).toList();
            } else if (okaySingleMoves.stream().toList().get(0).right() < okayDoubleMoves.stream().toList().get(0).right()) {
                return okaySingleMoves.stream().map(Pair::left).toList();
            } else return okayDoubleMoves.stream().map(Pair::left).toList();
        } else return goodSingleMoves;
        //priority: goodSingle -> goodDouble -> okaySingle (if double empty) -> okayDouble (if single empty) -> the okayMove with the lower badFlag rating
    }

    private static List<Move> removeSimilarMoves(List<Move> moves) {
        //List -> contains only one configuration of a possible move: e.g. [SingleMove(src, dest, SECRET), SingleMove(src, dest, TAXI)] -> [SingleMove(src, dest, SECRET)]
        List<Move> uniqueMoves = new ArrayList<>();

        for(Move move : moves) {
            int destination = move.accept(destinationVisitor);
            List<Integer> destinations = uniqueMoves.stream().map(x -> x.accept(destinationVisitor)).toList();
            if (!destinations.contains(destination)) {
                uniqueMoves.add(move);
            }
        }

        return uniqueMoves;
    }

    public static void expandTree(GameNode rootNode, int depth) {

        Board.GameState currentState = rootNode.getCurrentState();

        if (depth>=0) {
            List<Move> moveList = goodMoveFilter(currentState.getAvailableMoves(), currentState);
            moveList = removeSimilarMoves(moveList);
            //gets all the moves that I want to make a child node of

            for(Move move : moveList) {
                Board.GameState newState = currentState.advance(move);

                //this advances the game state until mr x is to move again
                while(true) {
                    ImmutableSet<Move> stateMoves = newState.getAvailableMoves();
                    if (!stateMoves.isEmpty()) {
                        if (stateMoves.stream().toList().get(0).commencedBy().isMrX()) {
                            GameNode newNode = new GameNode(newState, move.accept(destinationVisitor), Optional.of(move), false, rootNode);
                            rootNode.addChildNode(newNode);
                            break;
                        } else {
                            Move bestDetectiveMove = bestDetectiveMove(stateMoves, newState, rootNode.getMrXLocation());
                            //aims to choose the best move the detectives could make -> assuming the detectives always play optimally
                            newState = newState.advance(bestDetectiveMove);
                        }
                    } else {
                        break;
                    }
                }

            }
            //this adds all possible child nodes for mr x

            ArrayList<GameNode> childNodes = rootNode.getChildNodes();
            for(GameNode childNode : childNodes) {
                if (!childNode.getCurrentState().getAvailableMoves().isEmpty()) {
                    expandTree(childNode, depth-1);
                    //expands the tree downwards for all compatible child nodes
                }
            }

        }
    }

    private static Move bestDetectiveMove(ImmutableSet<Move> possibleMoves, Board.GameState currentState, int mrXLocation) {

        AdvancedDijkstra distanceFinder = new AdvancedDijkstra(currentState);
        int closestDistance = Integer.MAX_VALUE;
        Move closestMove = possibleMoves.asList().get(0);

        for (Move move : possibleMoves) {
            if (move instanceof Move.SingleMove singleMove) {
                ArrayList<Integer> distanceToMrX = distanceFinder.shortestPath(singleMove.destination, mrXLocation, move.commencedBy());
                if (!distanceToMrX.contains(-1) && distanceToMrX.size() < closestDistance) {
                    closestDistance = distanceToMrX.size();
                    closestMove = move;
                }
            }
        }

        if (closestDistance == Integer.MAX_VALUE) {
            return possibleMoves.asList().get(0);
        } else return closestMove;
    }

}
