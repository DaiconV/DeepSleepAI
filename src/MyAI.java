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

public class MyAI extends Agent
{
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

	public MyAI ( )
	{
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================
		//@TODO initialize the map data structure
		
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
