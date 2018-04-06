package student_player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import coordinates.Coordinates.CoordinateDoesNotExistException;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {
    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("xxxxxxxxx");
    }
    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    private Move bestMove;
    private int initialDepth = 3;
    
    public Move chooseMove(TablutBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
        //MyTools.getSomething();
    	
    	bestMove =  boardState.getRandomMove();
    	if(player_id == 1 && kingCanReachCorner(boardState)){
    		return bestMove;
    	}
    	
        miniMax(boardState, initialDepth,Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
        
        
        return bestMove;
    }
    
    public boolean kingCanReachCorner(TablutBoardState boardState){
    	Coord kingPos =  boardState.getKingPosition();
    	for(Coord c : Coordinates.getCorners()){
    		TablutMove move = new TablutMove(kingPos,c,1);
    		if(boardState.isLegal(move)){
    			bestMove = move;
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean canReachCorner(TablutBoardState boardState, Coord kingPos){
    	for(Coord c : Coordinates.getCorners()){
    		if(isLegalMove(boardState, kingPos, c)){
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Code from TablutBoardState isLegalMove
     * @param boardState
     * @param start
     * @param end
     * @return
     */
    public boolean isLegalMove(TablutBoardState boardState, Coord start, Coord end) {
        // Next, make sure move doesn't end on a piece.
        if (!boardState.coordIsEmpty(end))
            return false;

        // Next, make sure the move is actually a move.
        int coordDiff = start.maxDifference(end);
        if (coordDiff == 0)
            return false;

        // Now for the actual game logic. First we make sure it is moving like a rook.
        if (!(start.x == end.x || start.y == end.y))
            return false;

        // Now we make sure it isn't moving through any other pieces.
        for (Coord throughCoordinate : start.getCoordsBetween(end)) {
            if (!boardState.coordIsEmpty(throughCoordinate))
                return false;
        }
        return true;
    	
    }
    
    public boolean kingCanReachCornerInTwoSteps(TablutBoardState boardState, Coord kingPos) {
    	List<Coord> intermediates = new ArrayList<Coord>(Arrays.asList(Coordinates.get(0, kingPos.y),Coordinates.get(8, kingPos.y),Coordinates.get(kingPos.x, 0),Coordinates.get(kingPos.x,8)));
    	for(Coord c: intermediates) {
    		if(isLegalMove(boardState,kingPos,c)&& !canKillKing(boardState, c) && canReachCorner(boardState,c)) {
    			return true;
    		}; 
    	}
    	return false;
    }
    
    public boolean canKillKing(TablutBoardState bs, Coord kingPosition){
    	Coord killKingPosition = null;
    	List<Coord> neighbors = Coordinates.getNeighbors(kingPosition);
    	for(Coord pos: neighbors) {
    		if(Coordinates.isCorner(pos) || bs.getPieceAt(pos) == TablutBoardState.Piece.BLACK) {
				try {
					killKingPosition = Coordinates.getSandwichCoord(pos, kingPosition);
					if(killKingPosition != null) {
						HashSet<Coord> pieces;
						if(bs.getTurnPlayer() == 0){
							pieces = bs.getPlayerPieceCoordinates();
						}
						else{
							pieces = bs.getOpponentPieceCoordinates();
						}
				    	for(Coord c: pieces) {
				    		TablutMove move = new TablutMove(c, killKingPosition, 0);
				    		if (bs.isLegal(move)){
				    			return true;
				    		}
				    	}
	    				break;
	    			}
				} catch (CoordinateDoesNotExistException e) {
				}
    		}
    	}
    	return false;
    }
    
    public double kingStepsToCorner(TablutBoardState boardState) {
    	Coord kingPos =  boardState.getKingPosition();
    	if(canReachCorner(boardState, kingPos)) {
    		return 1;
    	}
    	else if(kingCanReachCornerInTwoSteps(boardState, kingPos)) {
    		return 2;
    	}
    	
    	double value = 3;
//    	double manhattonDist = Coordinates.distanceToClosestCorner(kingPos);
//    	if(manhattonDist > value) {
//    		value = manhattonDist;
//    	}
    	return value;
    }
    
    public double miniMax(TablutBoardState boardState, int depth, double alpha, double beta) {
    	if(boardState.gameOver() || depth == 0) {
    		return evaluate(boardState);
    	}
    	List<TablutMove> options = boardState.getAllLegalMoves();
    	Collections.shuffle(options);
    	if(boardState.getTurnPlayer() == player_id) {//max
    		double highestSeenValue = Double.NEGATIVE_INFINITY;
    		for(TablutMove move : options){
        		TablutBoardState cloneBS = (TablutBoardState) boardState.clone();
        		cloneBS.processMove(move);
        		double currentValue = miniMax(cloneBS, depth-1, alpha, beta);
        		if(currentValue>highestSeenValue){
        			highestSeenValue = currentValue;
        			
        		}
        		if(currentValue > alpha){
        			alpha = currentValue;
        			if(depth == initialDepth){
        				bestMove = move;
        			}
        		}
        		if(alpha >= beta){
        			break;//it is not a path that we will consider.
        		}
        	}
    		return highestSeenValue;
    	}
    	else {
    		double lowestSeenValue = Double.MAX_VALUE;
        	for(TablutMove move : options){
        		TablutBoardState cloneBS = (TablutBoardState) boardState.clone();
        		cloneBS.processMove(move);
        		double currentValue = miniMax(cloneBS, depth-1, alpha, beta);
        		if(currentValue<lowestSeenValue){
        			lowestSeenValue = currentValue;
        		}
        		if(currentValue < beta){
        			beta = currentValue;
        		}
        		if(alpha >= beta){
        			break;//it is not a path that we will consider.
        		}
        	}
        	return lowestSeenValue;
    	}
    }
    
    public double evaluate(TablutBoardState boardState){
		if(player_id == boardState.getWinner()){
			return Double.MAX_VALUE;
		}
		else if(1-player_id == boardState.getWinner()){
			return Double.NEGATIVE_INFINITY;
		}
    	int playerPieceNumber = boardState.getNumberPlayerPieces(player_id);
        int opponentPieceNumber = boardState.getNumberPlayerPieces(1-player_id);
        
        double kingStepsToCorner = kingStepsToCorner(boardState);
        if(player_id == 1){//white
        	return playerPieceNumber-opponentPieceNumber*0.7-kingStepsToCorner*2;
        }
        else{//black
        	
        	if(playerPieceNumber-opponentPieceNumber > 10){
        		return playerPieceNumber-opponentPieceNumber*0.4+kingStepsToCorner*2;
        	}
        	return playerPieceNumber-opponentPieceNumber*0.7+kingStepsToCorner*2;
        }
        
    }
    
    
}
