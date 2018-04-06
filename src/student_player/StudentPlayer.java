package student_player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;
import coordinates.Coord;
import coordinates.Coordinates;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260743097");
    }
    
    private HashSet<Coord> last = null;
    private int turnHere = 0;
    
    public static final class Node {
    	public TablutMove past;
        public float score;
        public TablutBoardState state;
        public Node(TablutMove history, float score, TablutBoardState bs) {
            this.past = history;
            this.score = score;
            this.state = bs;
        }
    }
    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(TablutBoardState bs) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
    	//HashSet<Coord> now = bs.getPlayerPieceCoordinates();
        /*if(bs.getTurnNumber()==0){
        	double timeout = 28.5;
            long deadline = System.currentTimeMillis() + (long)(timeout * 1000);
            while(System.currentTimeMillis()<deadline){
            	Node history = new Node(null, 0,0);
            	Node myNode = max(bs, history, 6, -1000, 1000);
            	TablutMove myMove = myNode.past;
            	return myMove;
            }
        	if (player_id == TablutBoardState.MUSCOVITE){
        		TablutMove myMove = new TablutMove(4,1,1,1,player_id);
        		last = bs.getPlayerPieceCoordinates();
        		return myMove;
        	}else{
        		Node history = new Node(null,0);
        		Node myNode = alphabeta(bs, history, 2, -1000, 1000);
            	TablutMove myMove = myNode.past;
            	last = bs.getPlayerPieceCoordinates();
            	return myMove;
        	}
        }*/
        //Coord[] opponentMove = findMove(last, now);
        /*if(opponentMove.x == 3 && opponentMove.y ==3 && bs.getTurnNumber()==1){
        	last = now;
        	return new TablutMove(2,4,2,3,player_id);
        }*/
    	turnHere++;
    	//System.out.println(turnHere);
        double timeout = 1.9;
        long deadline = System.currentTimeMillis() + (long)(timeout * 1000);
        TablutMove myMove = null;
        Node[] bestNodes = new Node[5];
        Node[] myNode = alphabeta(bs, 2);
    	myMove = myNode[0].past;
    	if(myNode[0].score >= 900) return myMove;
    	for (int i=0; i<5;i++){
    		if(myNode[i].past == null){ System.out.println(i);  break; }
    		bestNodes[i] = myNode[i];
    	}
        // Iterative-deepening, with evolution offspring selection
    	int acc = 0;
        while(System.currentTimeMillis()<deadline){
        	while(acc < 5 && bestNodes[acc].state != null){
            	bestNodes[acc] = ab(bestNodes[acc].state, 2, -1000, 1000);
            	acc++;
            }
        	Node best = bestNodes[0];
        	best.past = myNode[0].past;
        	for(int i=1;i<5;i++){
        		if(bestNodes[i].score > best.score){
        			best = bestNodes[i];
        			best.past = myNode[i].past;
        		}
        	}
        	// This move induces winning, so no need to continue.
        	if(best.score == 900){
        		myMove = best.past;
        		break;
        	}
        	if(best.past != null) myMove = best.past;
        	acc = 0;
        }
        // Is random the best you can do? In case something terrible happens... 
        if(myMove == null){ 
        	System.out.println("Random");
        	return bs.getRandomMove();}
        return myMove;
    }

    public float eval(TablutBoardState bs) {
        // If the game is a draw, return 0.
    	float score = 0;
    	int player = bs.getTurnPlayer(); //0 musc, 1 swede
        if(bs.getWinner()==0 ||bs.getWinner() == 1) {
        	if (bs.getWinner()== player_id) return 900;
        	else if (bs.getWinner()== 1-player_id) return -900;
        	else
        	return score;
        }
        else {
            // Find the shortest distance between the King and any corner of the board.
        	//float result = 400*mc(bs);
        	Coord king = bs.getKingPosition();
        	int pSign = 2*player-1;
        	int route = 0;
        	// If the king has ways to win in 1 or 2 steps, it is favorable for swede.
            for (int i=0; i<4;i++){
            	int a = (i/2)*8;
            	int b = (i%2)*8;
            	// If one step win exists, then it is almost as good as a win.
            	if(bs.isLegal(new TablutMove(king.x,king.y,a,b,player))) return pSign*899;
            	else if(bs.isLegal(new TablutMove(king.x,king.y,king.x,b,player)) 
            		&& bs.isLegal(new TablutMove(king.x,b,a,b,player))) route++;
            	else if(bs.isLegal(new TablutMove(king.x,king.y,a,king.y,player))
            		&& bs.isLegal(new TablutMove(a,king.y,a,b,player))) route++;
            }
            // Incentive for king to move out of middle.
            route = 20*route+(8-Coordinates.distanceToClosestCorner(king))*5;
            score += route*pSign;
            // For swede, it is not good to have king surrounded.
            if(player==1){
            	for(Coord x : Coordinates.getNeighbors(king)){
                	if(bs.isOpponentPieceAt(x)) score -= 6;
                }
            }
        }
     // Start by counting how many pieces each player has.
        int myPieces = bs.getNumberPlayerPieces(player);
        int urPieces = bs.getNumberPlayerPieces(1-player);
        score = score + 10*((player+3)*myPieces - (1-player+3)*urPieces);   
        return score;
    }
    
    // Minmax with ordered list of best moves for immediate node only
    public Node[] alphabeta(TablutBoardState bs, int depth) {
    	if(depth==0 || bs.getWinner() == 0 || bs.getWinner() == 1){
    		Node[] result = new Node[1];
    		result[0] = new Node(null,eval(bs),null);
            return result;
        }
    	else if (depth==2){
    		Node[] max = new Node[5];
    		for(int i=0; i<5; i++){
    			max[i]=new Node(null,-1000,null);
    		}
            for(TablutMove move : bs.getAllLegalMoves()) {
                // Try each step, then it will min's turn.
            	TablutBoardState trial = (TablutBoardState) bs.clone();
            	trial.processMove(move);
                Node[] result = alphabeta(trial, depth-1);
                if(result[0].score > max[4].score){
                	for(int i=0; i<5; i++){
                		if(result[0].score > max[i].score){
                			for(int j=4; j>i;j--){
                				max[j] = max[j-1];
                			}
                			max[i].score = result[0].score;
                			max[i].past = move;
                			max[i].state = result[0].state;
                			break;
                		}
                	}
            	}
            }
            return max;
    	}
    	else{
    		Node min = new Node(null,1000,null);
    		for(TablutMove move : bs.getAllLegalMoves()) {
                // Simulate a step, and then recurse.
            	TablutBoardState trial = (TablutBoardState) bs.clone();
            	trial.processMove(move);
                Node[] result = alphabeta(trial,depth-1);
                if(result[0].score < min.score){
                    min.score = result[0].score;
                    min.state = trial;
                }
                //beta = Math.min(beta, (int)result[0].score);
                //if(beta <= alpha) break;
            }
    		Node[] minlist = new Node[1];
    		minlist[0] = min;
            return minlist;
    	}
    }
    // Alpha beta for detecting future best path assuming A-B popular.
    public Node ab(TablutBoardState bs, int depth, int alpha, int beta) {
    	if(depth==0){
            return new Node(null,eval(bs),null);
        }
    	else if (depth==2){
    		Node max = new Node(null,-1000,null);
            for(TablutMove move : bs.getAllLegalMoves()) {
                // Try each step, then it will min's turn.
            	TablutBoardState trial = (TablutBoardState) bs.clone();
            	trial.processMove(move);
                Node result = ab(trial, depth-1, alpha, beta);
                if(max.score == -1000 || result.score > max.score){
                	max.score = result.score;
        			max.state = result.state;
            	}
                alpha = Math.max(alpha, (int)result.score);
                if(beta <= alpha) break;
            }
            return max;
    	}
    	else{
    		Node min = new Node(null,1000,null);
    		for(TablutMove move : bs.getAllLegalMoves()) {
                // Simulate a step, and then recurse.
            	TablutBoardState trial = (TablutBoardState) bs.clone();
            	trial.processMove(move);
                Node result = ab(trial,depth-1, alpha, beta);
                if(min.score == 1000 || result.score < min.score){
                    min.score = result.score;
                    min.state = trial;
                }
                beta = Math.min(beta, (int)result.score);
                if(beta <= alpha) break;
            }
            return min;
    	}
    }
    
    public Coord[] findMove(HashSet<Coord> pre, HashSet<Coord> now){
    	Coord[] move = new Coord[2];
    	for (Coord i : now){
    		if(!pre.contains(i)){
    			move[1] = i;
    			break;
    		}
    	}
    	for (Coord i : pre){
    		if(!pre.contains(i)){
    			move[0] = i;
    			break;
    		}
    	}
    	return move;
    }
    
    /*public float mc(TablutBoardState bs) {
        // We keep stastics for the depth charges that follow from each action.
        int player = bs.getTurnPlayer();
        final Random rand = new Random(1848);
        double timeout = 2.0;
        long deadline = System.currentTimeMillis() + (long)(timeout * 1000);
        float wins,losses,total;
        wins=losses=total =0;
        while (System.currentTimeMillis() < deadline && total<= 10) {
            //Play the game randomly until the game is over.
        	TablutBoardState trial = (TablutBoardState) bs.clone();
            while (!trial.gameOver()) {
            	List<TablutMove> options = trial.getAllLegalMoves();
            	TablutMove move = options.get(rand.nextInt(options.size()));
                trial.processMove(move);
            }
            if (trial.getWinner()==player) wins++;
            else if (trial.getWinner()==1-player) losses++;
            total++;        
        }
        float winPerc = (wins-losses)/total;
        return winPerc;
    }*/

}