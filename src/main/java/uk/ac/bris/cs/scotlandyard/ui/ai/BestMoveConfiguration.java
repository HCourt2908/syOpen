package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.UNDERGROUND;

public class BestMoveConfiguration {

    public static Move bestMoveConfiguration(Board.GameState currentState, Move exampleConfig) {
        return exampleConfig.accept(new Move.Visitor<Move>() {
            @Override
            public Move visit(Move.SingleMove move) {
                return bestSingleMoveConfiguration(currentState, (Move.SingleMove) exampleConfig);
            }

            @Override
            public Move visit(Move.DoubleMove move) {
                return bestDoubleMoveConfiguration(currentState, (Move.DoubleMove) exampleConfig);
            }
        });
    }

    private static Move bestSingleMoveConfiguration(Board.GameState currentState, Move.SingleMove exampleConfig) {
        List<Move.SingleMove> singleMoveConfigurations = makeSingleMoveConfigurations(currentState, exampleConfig);
        Board.TicketBoard mrXTickets = currentState.getPlayerTickets(MRX).get();
        if (mrXTickets.getCount(SECRET) > 0 && currentState.getSetup().moves.get(max(0,(currentState.getMrXTravelLog().size()-1)))) {
            //the max function is there to avoid doing a query for an index -1
            return new Move.SingleMove(MRX, exampleConfig.source(), SECRET, exampleConfig.destination);
            //if mr x has just made a reveal turn, I want to reveal as little info as possible about where I am going next
        } else {
            return singleMoveConfigurations.get(idealIndex(singleMoveConfigurations.stream().map(x->x.ticket).toList()));
        }
        //returns the best ticket choice for a single move
    }

    private static List<Move.SingleMove> makeSingleMoveConfigurations(Board.GameState currentState, Move.SingleMove exampleConfig) {
        Board.TicketBoard mrXTickets = currentState.getPlayerTickets(MRX).get();
        List<Move.SingleMove> possibleMoveConfigurations = new ArrayList<>();
        List<ScotlandYard.Ticket> usableTickets = new ArrayList<>(currentState.getSetup().graph.edgeValue(exampleConfig.source(), exampleConfig.destination)
                .get().stream().map(ScotlandYard.Transport::requiredTicket).toList());
        if (!usableTickets.contains(SECRET)) usableTickets.add(SECRET);
        for(ScotlandYard.Ticket ticket : usableTickets) {
            if (mrXTickets.getCount(ticket) > 0) {
                possibleMoveConfigurations.add(new Move.SingleMove(MRX, exampleConfig.source(), ticket, exampleConfig.destination));
            }
        }
        return possibleMoveConfigurations;
        //generates all possible ticket combinations that mr X could use to make this specific move
    }

    private static Move bestDoubleMoveConfiguration(Board.GameState currentState, Move.DoubleMove exampleConfig) {
        Board.TicketBoard mrXTickets = currentState.getPlayerTickets(MRX).get();
        List<Move.DoubleMove> doubleMoveConfigurations = makeDoubleMoveConfigurations(currentState, exampleConfig);

        if (currentState.getSetup().moves.get(max(0,currentState.getMrXTravelLog().size()-1))) {
            List<Move.DoubleMove> secretTicketFirst = doubleMoveConfigurations.stream().filter(x -> x.ticket1==SECRET).toList();
            if(!secretTicketFirst.isEmpty()) {
                return secretTicketFirst.get(idealIndex(secretTicketFirst.stream().map(x->x.ticket2).toList()));
            }
        }
        if (currentState.getSetup().moves.get(max(0,currentState.getMrXTravelLog().size()))) {
            List<Move.DoubleMove> secretTicketSecond = doubleMoveConfigurations.stream().filter(x -> x.ticket2==SECRET).toList();
            if(!secretTicketSecond.isEmpty()) {
                return secretTicketSecond.get(idealIndex(secretTicketSecond.stream().map(x->x.ticket1).toList()));
            }
        }

        List<ScotlandYard.Ticket> firstTickets = doubleMoveConfigurations.stream().map(x->x.ticket1).toList();
        ScotlandYard.Ticket idealFirstTicket = firstTickets.get(idealIndex(firstTickets));
        //decides the best ticket type to use for the first ticket
        List<Move.DoubleMove> movesUsingIdealFirst = doubleMoveConfigurations.stream().filter(x->x.ticket1==idealFirstTicket).toList();
        //filters the list of possible configurations to contain only those with the ideal first ticket
        return movesUsingIdealFirst.get(idealIndex(movesUsingIdealFirst.stream().map(x->x.ticket2).toList()));
        //picks the move in the list of ideal first ticket moves with the ideal second ticket
    }

    private static List<Move.DoubleMove> makeDoubleMoveConfigurations(Board.GameState currentState, Move.DoubleMove exampleConfig) {
        Board.TicketBoard mrXTickets = currentState.getPlayerTickets(MRX).get();
        List<Move.DoubleMove> possibleMoveConfigurations = new ArrayList<>();
        List<ScotlandYard.Ticket> usableFirstTickets = new ArrayList<>(currentState.getSetup().graph.edgeValue(exampleConfig.source(), exampleConfig.destination1)
                .get().stream().map(ScotlandYard.Transport::requiredTicket).toList());
        if(!usableFirstTickets.contains(SECRET)) usableFirstTickets.add(SECRET);
        List<ScotlandYard.Ticket> usableSecondTickets = new ArrayList<>(currentState.getSetup().graph.edgeValue(exampleConfig.destination1, exampleConfig.destination2)
                .get().stream().map(ScotlandYard.Transport::requiredTicket).toList());
        if(!usableSecondTickets.contains(SECRET)) usableSecondTickets.add(SECRET);

        for(ScotlandYard.Ticket firstTicket : usableFirstTickets) {
            for(ScotlandYard.Ticket secondTicket : usableSecondTickets) {
                if (firstTicket.equals(secondTicket)) {
                    if (mrXTickets.getCount(firstTicket)>1) {
                        possibleMoveConfigurations.add(new Move.DoubleMove(MRX, exampleConfig.source(), firstTicket, exampleConfig.destination1, secondTicket, exampleConfig.destination2));
                    }
                } else {
                    if (mrXTickets.getCount(firstTicket)>0 && mrXTickets.getCount(secondTicket)>0) {
                        possibleMoveConfigurations.add(new Move.DoubleMove(MRX, exampleConfig.source(), firstTicket, exampleConfig.destination1, secondTicket, exampleConfig.destination2));
                    }
                }
            }
        }
        //generates all ticket combinations for a specific move that mr x has the tickets to make
        return possibleMoveConfigurations;
    }

    private static int idealIndex(List<ScotlandYard.Ticket> ticketList) {
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
