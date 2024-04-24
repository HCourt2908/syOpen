package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.ArrayList;
import java.util.Optional;

import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;

public class GameNode {

    private ArrayList<GameNode> childNodes = new ArrayList<>();
    //this arrayList contains all the child nodes of a node
    //this is to emulate the tree structure
    private final Board.GameState currentState;
    private final int mrXLocation;
    private final Optional<Move> moveChosen;
    //this is an optional because the root node won't have a move chosen yet -> that's what we are trying to find!
    private final boolean isRoot;
    //this will only be true for the root node we create in MrXAiLookahead
    private GameNode parent;

    public GameNode(Board.GameState currentState, int MrXLocation, Optional<Move> moveChosen, boolean isRoot, GameNode parent){
        this.currentState = currentState;
        this.mrXLocation = MrXLocation;
        this.moveChosen = moveChosen;
        this.isRoot = isRoot;
        this.parent = parent;
    }

    public GameNode getParent() {
        return parent;
    }

    public ArrayList<GameNode> getChildNodes() {
        return childNodes;
    }

    public void addChildNode(GameNode childNode) {
        childNodes.add(childNode);
    }


    public Board.GameState getCurrentState() {
        return currentState;
    }

    public int getMrXLocation() {
        return mrXLocation;
    }

    public boolean MrXLost() {
        ImmutableSet<Piece> pieces = currentState.getPlayers();
        for (Piece piece : pieces) {
            if (piece.isDetective()) {
                return (currentState.getWinner().contains(piece));
            }
        }
        return false;
    }
    //returns true if mr x has lost in this GameState

    public boolean MrXToMove() {
        return currentState.getAvailableMoves().asList().get(0).commencedBy()==MRX;
    }
    //if one of the moves in getAvailableMoves is commenced by mr X, then all of them are,
    //and therefore it is his turn

    public boolean isRoot() {
        return isRoot;
    }

    public Optional<Move> getMoveChosen() {
        return moveChosen;
    }
}
