package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

import static java.lang.Math.max;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

public class MrXAINoLookahead implements Ai {

	private static final nodeMultipliers nodeMultipliersMapper = new nodeMultipliers();
	private static final Map<Integer,Double> nodeMultipliers = nodeMultipliersMapper.nodeMultipliers;


	@Nonnull @Override public String name() { return "Mr X AI - No Lookahead"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
		Board.GameState gameState = (Board.GameState) board;

		var immutableMoves = gameState.getAvailableMoves();
		List<Move> moves = immutableMoves.asList();
		//copies over all possible moves that mr X could make

		Move initialChoice = calculateMoves(gameState, moves);

		return initialChoice;
	}

	private Move calculateMoves(Board.GameState state, List<Move> moves) {

		Move.Visitor<Integer> visitor = new Move.Visitor<>() {
			@Override
			public Integer visit(Move.SingleMove move) {
				return move.destination;
			}

			@Override
			public Integer visit(Move.DoubleMove move) {
				return move.destination2;
			}
		};

		AdvancedDijkstra distanceFinder = new AdvancedDijkstra(state);
		//initialises the Dijkstra shortest path class
		List<Integer>closestDetectiveDistances = new ArrayList<>();
		List<Double>weightedNodeScores = new ArrayList<>();
		//creates a list to contain the distance of the closest detective to this move's destination
		List<Move> possibleMoves = new ArrayList<>();
		//this list will contain moves that I deem to be acceptable choices. If a move results in a detective being one move away it is not an acceptable choice.
		List<Piece> detectivePieces  = new ArrayList<>(state.getPlayers().stream().toList());
		detectivePieces.remove(MRX);
		//gets all detective pieces from an immutableSet to a list. had to use stream because otherwise it wouldn't have been a mutable list.

		boolean possibleFlag = true;
		//boolean flag that will dictate whether a move is a good choice or not
		int overallFurthestClosestIndex = 0;
		int overallFurthestClosestDistance = 0;
		//keeps track of the index with the largest average. this will only ever be used if every move has a detective within one move away. it's a last resort.

		for(int i = 0; i < moves.size(); i++) {

			int moveDestination = moves.get(i).accept(visitor);
			//returns the destination of move. this is either destination for a single move or destination2 for a double move

			List<Integer> detectiveDistances = new ArrayList<>();
			//a list that holds the distance from each detective to the move destination

			for (Piece piece : detectivePieces) {
				ArrayList<Integer> detectivePath = distanceFinder.shortestPath(moveDestination, state.getDetectiveLocation((Piece.Detective) piece).get(), piece);
				//finds distance

				if (!detectivePath.equals(List.of(-1))) {
					//if the destination is actually reachable by the detective

					int detectiveDistance = detectivePath.size();
					detectiveDistances.add(detectiveDistance);
					//adds the detective's distance to the list

					if (detectiveDistance == 1) {
						possibleFlag = false;
						//if a detective is one move away from me, absolutely do not pick this move!
					}

				}


			}

			int closestDetectiveDistance = -1;
			for(int detectiveDistance : detectiveDistances) {
				if (detectiveDistance > closestDetectiveDistance) closestDetectiveDistance = detectiveDistance;
			}

			if (closestDetectiveDistance == -1) {
				closestDetectiveDistance = Integer.MAX_VALUE;
			}
			if (closestDetectiveDistance > overallFurthestClosestDistance) {
				overallFurthestClosestDistance = closestDetectiveDistance;
				overallFurthestClosestIndex = i;
			}
			//updates to store the index of the largest average


			if (possibleFlag) {
				closestDetectiveDistances.add(closestDetectiveDistance);
				weightedNodeScores.add(closestDetectiveDistance * nodeMultipliers.get(moveDestination));
				//the weighted score changes depending on how close the move is to the middle of the board (moves closer to middle are better)
				possibleMoves.add(moves.get(i));


			}
			possibleFlag = true;

		}

		if (possibleMoves.isEmpty()) {
			return moves.get(overallFurthestClosestIndex);
			//last resort. attempts to make the best move of a bad bunch
		} else {
			return pickBestMove(weightedNodeScores, possibleMoves, state);
		}

	}

	private Move pickBestMove(List<Double> weightedNodeScores, List<Move> possibleMoves, Board.GameState state) {

		List<Pair<Move.SingleMove, Double>> possibleSingles = new ArrayList<>();
		List<Pair<Move.DoubleMove, Double>> possibleDoubles = new ArrayList<>();

		for(int i = 0; i < possibleMoves.size(); i++) {
			if (possibleMoves.get(i) instanceof Move.SingleMove) {
				possibleSingles.add(new Pair<>((Move.SingleMove) possibleMoves.get(i), weightedNodeScores.get(i)));
			} else if (possibleMoves.get(i) instanceof Move.DoubleMove) {
				possibleDoubles.add(new Pair<>((Move.DoubleMove) possibleMoves.get(i), weightedNodeScores.get(i)));
			}
		}
		//separates moves based on whether they are single or double. I want to prioritise the single moves before even considering a double move

		if (!possibleSingles.isEmpty()) {
			List<Move.SingleMove> bestSingles = furthestSingleMoves(possibleSingles);
			return pickBestSingleMove(bestSingles, state);
		} else {
			//if there are no single moves that have me >1 away from a detective, consult the double moves
			List<Move.DoubleMove> bestDoubles = furthestDoubleMoves(possibleDoubles);
			return pickBestDoubleMove(bestDoubles, state);
		}

	}

	private List<Move.SingleMove> furthestSingleMoves(List<Pair<Move.SingleMove,Double>> moves) {
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

	private Move.SingleMove pickBestSingleMove(List<Move.SingleMove> moves, Board.GameState state) {

		List<ScotlandYard.Ticket> tickets = moves.stream().map(move -> move.ticket).toList();
		//takes the move list and transforms it into a list of tickets using streams.

		if (tickets.contains(SECRET) && state.getSetup().moves.get(max(0,(state.getMrXTravelLog().size()-1)))) {
			//the max function is there to avoid doing a query for an index -1
			return moves.get(tickets.indexOf(SECRET));
			//if mr x has just made a reveal turn, I want to reveal as little info as possible about where I am going next
		} else {
			return moves.get(idealIndex(tickets));
		}

	}

	private List<Move.DoubleMove> furthestDoubleMoves(List<Pair<Move.DoubleMove,Double>> moves) {
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

	private Move.DoubleMove pickBestDoubleMove(List<Move.DoubleMove> moves, Board.GameState state) {

		List<Pair<ScotlandYard.Ticket, ScotlandYard.Ticket>> tickets =
				moves.stream().map(move -> new Pair<ScotlandYard.Ticket, ScotlandYard.Ticket>(move.ticket1,move.ticket2)).toList();
		//takes the move list and transforms it into a list of tickets using streams.

		if (state.getSetup().moves.get(max(0,state.getMrXTravelLog().size()-1))) {
			List<ScotlandYard.Ticket> secretTicketFirst = tickets.stream().filter(x -> x.left()==SECRET).map(Pair::right).toList();
			//transforms a list of ticket pairs into a list of ticket2's where ticket1 is a secret
			if (!secretTicketFirst.isEmpty()) {
				int bestSecondaryTicket = idealIndex(secretTicketFirst);
				Pair<ScotlandYard.Ticket, ScotlandYard.Ticket> idealTicketCombo = new Pair<>(SECRET, secretTicketFirst.get(bestSecondaryTicket));
				return moves.get(tickets.indexOf(idealTicketCombo));
				//decides the best move, where the first ticket is a secret one
			}
		}
		if (state.getSetup().moves.get(max(0,state.getMrXTravelLog().size()))) {
			List<ScotlandYard.Ticket> secretTicketSecond = tickets.stream().filter(x -> x.right()==SECRET).map(Pair::left).toList();
			//transforms a list of ticket pairs into a list of ticket1's where ticket2 is a secret
			if(!secretTicketSecond.isEmpty()) {
				int bestPrimaryTicket = idealIndex(secretTicketSecond);
				Pair<ScotlandYard.Ticket, ScotlandYard.Ticket> idealTicketCombo = new Pair<>(secretTicketSecond.get(bestPrimaryTicket), SECRET);
				return moves.get(tickets.indexOf(idealTicketCombo));
				//decides the best move, where the second ticket is a secret one
			}
		}

		//if the program reaches here then it must mean that we will not interact at all with a reveal turn
		//Or that we don't have any secret tickets for the reveal turn
		List<ScotlandYard.Ticket> firstTickets = tickets.stream().map(Pair::left).toList();
		int idealFirstIndex = idealIndex(firstTickets);
		//now I know the best ticket to use for the initial move
		List<ScotlandYard.Ticket> secondTickets = tickets.stream()
				.filter(x -> x.left()== firstTickets.get(idealFirstIndex))
				.map(Pair::right).toList();
		//creates a list of ticket2's where the first ticket is the ideal ticket we just found
		int idealSecondIndex = idealIndex(secondTickets);
		//now I know the best ticket pair to use
		Pair<ScotlandYard.Ticket, ScotlandYard.Ticket> idealTicketCombo = new Pair<>(firstTickets.get(idealFirstIndex), secondTickets.get(idealSecondIndex));
		return moves.get(tickets.indexOf(idealTicketCombo));

	}

	private int idealIndex(List<ScotlandYard.Ticket> ticketList) {
		if (ticketList.contains(TAXI)) return ticketList.indexOf(TAXI);
		else if (ticketList.contains(SECRET)) return ticketList.indexOf(SECRET);
		else if (ticketList.contains(BUS)) return ticketList.indexOf(BUS);
		else if (ticketList.contains(UNDERGROUND)) return ticketList.indexOf(UNDERGROUND);
		else return 0;
		//this line should never be reached, but it felt wrong to put an 'else' at the end instead of an 'else if underground'

		//I chose this order due to order of usefulness:
		//a taxi ticket reveals little and mr x has many, a secret ticket reveals nothing but mr x has few, a bus ticket reveals more, underground reveals most
	}

}
