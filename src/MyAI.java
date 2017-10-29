// ======================================================================
// FILE:        MyAI.java
//
// AUTHOR:      Abdullah Younis
//              Marcos Antonio Avila
//              Kevin Michael Li
//
// DESCRIPTION: This file contains your agent class, which you will
//              implement. You are responsible for implementing the
//              'getAction' function and any helper methods you feel you
//              need.
//
// NOTES:       - If you are having trouble understanding how the shell
//                works, look at the other parts of the code, as well as
//                the documentation.
//
//              - You are only allowed to make changes to this portion of
//                the code. Any changes to other portions of the code will
//                be lost when the tournament runs your code.
// ======================================================================


import java.util.ArrayList;
import java.util.Stack;


public class MyAI extends Agent
{
	//Start KL 10/29
	
	
			
			
			
	//End KL 10/29
	private class Cell
	{
		private double pitProb;
		private double wumpProb;

		public Cell()
		{
			pitProb = -1;
			wumpProb = -1;
		}

		public void setPitProb(double pProb)
		{
			pitProb = pProb;
		}

		public void setWumpProb(double wProb)
		{
			wumpProb = wProb;
		}

		public double getPitProb()
		{
			return pitProb;
		}

		public double getWumpProb()
		{
			return wumpProb;
		}

	}

	private enum Direction
	{
		UP, RIGHT, DOWN, LEFT
	}

	private Cell[][] agentMap;
	private int mapRows;
	private int mapCols;

	private int curRow;
	private int curCol;
	private Direction curDir;
	
	//Start KL 10/29
	private ArrayList<Action> moves;
	private Stack<Action> backtrackForward = new Stack<Action>();
	private boolean exit = false;
	private boolean shotAlready = false;
	private boolean grabbedGold = false;
	//End KL 10/29

	public MyAI ( )
	{
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================
		//@TODO initialize the map data structure
		
		//Start KL 10/29
		
		
		
		
		
		//End KL 10/29
	
		mapRows = 4;
		mapCols = 10;
		
		agentMap = new Cell[mapRows][mapCols];

		for(int row = 0; row < mapRows; ++row)
			for(int col = 0; col < mapCols; ++col)
				agentMap[row][col] = new Cell();

		agentMap[0][0].setPitProb(0);	
		agentMap[0][0].setWumpProb(0);	

		curRow = 0;
		curCol = 0;
		curDir = Direction.RIGHT; 
		
		//Begin KL 10/29
		
		//How do I see the current precepts?
		//How do I call different moves?
		//I just want to move around and not die and also track the locations
		
		resetBacktrackForward();
		
		//End KL 10/29
		
		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}

/*	public Cell[] getAdjacentCells(int r, int c)
	{
		new
	}
*/
	public void updateMap
	(
		boolean stench,
		boolean breeze,
		boolean bump
	)
	{
		if(bump)
			if(curDir == Direction.RIGHT)
				mapCols = curCol + 1;
			else if(curDir == Direction.UP)
				mapRows = curRow + 1;
		//for()
	}
	
	public void printAgentMap()
	{
		for(int row = 3; row >=0; --row)
		{
			for(int col = 0; col < 10; ++col)
			{
				System.out.printf("%6.1f,%-4.1f", agentMap[row][col].getPitProb(),agentMap[row][col].getWumpProb());
			}
			System.out.println();
		}
	}
	
	private void resetBacktrackForward()
	{
		backtrackForward.push(Action.TURN_RIGHT);
		backtrackForward.push(Action.TURN_RIGHT);
		backtrackForward.push(Action.FORWARD);
		backtrackForward.push(Action.TURN_RIGHT);
		backtrackForward.push(Action.TURN_RIGHT);
		
	}
	
	public Action reverse(Action move)
	{
		if(move == Action.TURN_LEFT)
			return Action.TURN_RIGHT;
		else if(move == Action.TURN_RIGHT)
			return Action.TURN_LEFT;
		else if(move == Action.FORWARD)
		{	//REVERSE ONESELF AND THEN GO FORWARD ONCE
			//HOWEVER STILL NEED TO REVERSE BACK
			if(backtrackForward.empty())
				resetBacktrackForward();
			return backtrackForward.pop();
		}
		else if(move == Action.TURN_RIGHT)
			return Action.TURN_LEFT;
		else if(move == Action.CLIMB)
			return Action.CLIMB;
		else if(move == Action.SHOOT){
			moves.remove(moves.size()-1);
			return reverse(moves.get(moves.size()-1));
		}
		else if(move == Action.GRAB){
			moves.remove(moves.size()-1);
			return reverse(moves.get(moves.size()-1));
		}
		else
			System.out.println("Reverse should not go to else");
			return Action.GRAB;
	}
	public Action getOut()
	{
		//This part after if block needs optimizing
		if(moves.get(moves.size()-1) 
				== Action.FORWARD)
		{
			int forwardCount = 1;
			for(int i = (moves.size()-2); (moves.get(i) != Action.FORWARD); i--)
			{
				forwardCount++;
			}
		}
		//Now I need to do something about the movement
		//Trying to optimize the backtracking
		
		Action latestAction = moves.get(moves.size()-1);
		moves.remove(moves.size() -1);
			
		return reverse(latestAction);
	}

	public Action getAction
	(
		boolean stench,
		boolean breeze,
		boolean glitter,
		boolean bump,
		boolean scream
	)
	

	{
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================

		//KL 10/29 Starts
		
		if(exit){
			getOut();
		}
		
		else if ( glitter && !grabbedGold)
		{
			exit = true;
			return Action.GRAB;
		}
		
		else if(stench && !shotAlready)
			return Action.SHOOT;
		
		else if(stench && shotAlready)
			//Go somewhere else somehow
			
		
			
	
		
		
		
		//KL 10/29 Ends
		printAgentMap();
		return Action.CLIMB;
		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}
	
	// ======================================================================
	// YOUR CODE BEGINS
	// ======================================================================
	
	
	// ======================================================================
	// YOUR CODE ENDS
	// ======================================================================
}
