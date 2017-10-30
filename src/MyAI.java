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

import java.lang.Math;
import java.util.Scanner;

public class MyAI extends Agent
{
	private class Cell
	{
		public double pitProb;
		public double wumpProb;
		public boolean stench;
		public boolean breeze;
		public boolean explored;

		public Cell()
		{
			pitProb = -1;
			wumpProb = -1;
			stench = false;
			breeze = false;
			explored = false;
		}

/*		public void setPitProb(double pProb)
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
*/
	}

	private Cell[][] agentMap;
	private int mapRows;
	private int mapCols;

	private int curRow;
	private int curCol;
	private int curDir;

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

		curRow = 0;
		curCol = 0;
		curDir = 0; 
		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}

	private void checkPitLocalConsistency(int row, int col)
	{
		// I know... really gross, but it's helpful.
		int diagCellRow;
		int diagCellCol;
		int adjCellRow1;
		int adjCellCol1;
		int possiblePitCount;
		int adjCellRow2;
		int adjCellCol2;

		// First loop iterates through diagonal cells
		for(int n = 0; n < 4; ++n)
		{
			diagCellRow = row + (int) (Math.cos(Math.PI / 2 * n)) + (int) (Math.sin(Math.PI / 2 * n));
			diagCellCol = col + (int) (Math.cos(Math.PI / 2 * (n + 1))) + (int) (Math.sin(Math.PI / 2 * (n + 1)));
			System.out.printf("%d, %d\n", diagCellRow, diagCellCol);

			if(diagCellRow < 0 || diagCellCol < 0 || diagCellRow > 3 || diagCellCol > 9 || agentMap[diagCellRow][diagCellCol].pitProb <= 0)
				continue;

			// Second loop iterates through cells adjacent to the diagonals and checks to see if they have a breeze
			for(int m = 0; m < 4; ++m)
			{
				adjCellRow1 = diagCellRow + (int) Math.sin(Math.PI / 2 * m);
				adjCellCol1 = diagCellCol + (int) Math.cos(Math.PI / 2 * m);

				if(adjCellRow1 < 0 || adjCellCol1 < 0 || adjCellRow1 > 3 || adjCellCol1 > 9 || !agentMap[adjCellRow1][adjCellCol1].breeze)
					continue;

				possiblePitCount = 0;
				// Last loop iterates around the cells with breezes and counts the number of possible pits around it
				for(int l = 0; l < 4; ++l)
				{
					adjCellRow2 = adjCellRow1 + (int) Math.sin(Math.PI / 2 * l);
					adjCellCol2 = adjCellCol1 + (int) Math.cos(Math.PI / 2 * l);
					if(adjCellRow2 < 0 || adjCellCol2 < 0 || adjCellRow2 > 3 || adjCellCol2 > 9)
						continue;

					possiblePitCount += (agentMap[adjCellRow2][adjCellCol2].pitProb != 0) ? 1 : 0;
					// If only 1 possible pit, then update that pit probability to 1
				}
				
				if(possiblePitCount == 1)
				{
					agentMap[diagCellRow][diagCellCol].pitProb = 1;
					System.out.printf("row: %d, col: %d\n", adjCellRow1, adjCellCol1);
				}

			}
		}
	}
	
	private void updatePitProb(int row, int col)
	{
		int adjCellRow;
		int adjCellCol;

		int adjCellCount = 0;
		int breezeCount = 0;
		
		if(agentMap[row][col].pitProb == 0 || agentMap[row][col].pitProb == 1)
			return;	

		for(int n = 0; n < 4; ++n)
		{
			adjCellRow = row + (int) Math.sin(Math.PI / 2 * n);
			adjCellCol = col + (int) Math.cos(Math.PI / 2 * n);

			if(adjCellRow < 0 || adjCellCol < 0 || adjCellRow > 3 || adjCellCol > 9)
				continue;

			++adjCellCount;
			breezeCount += (agentMap[adjCellRow][adjCellCol].breeze) ? 1: 0;
		}

		agentMap[row][col].pitProb = ((double)breezeCount) / adjCellCount;
		
	}
	public void updateWumpProb(){
	}


	public void updateMap
	(
		boolean stench,
		boolean breeze,
		boolean bump
	)
	{
		int adjCellRow;
		int adjCellCol;		
		
		agentMap[curRow][curCol].pitProb = 0;
		agentMap[curRow][curCol].wumpProb = 0;
		agentMap[curRow][curCol].stench = stench;
		agentMap[curRow][curCol].breeze = breeze;

		// bump tells us that we hit a wall, record this number
		if(bump)
		{
			if(curDir == 0)
				mapCols = curCol + 1;
			else if(curDir == 3)
				mapRows = curRow + 1;
			return;
		}

		// If previously explored
		if(agentMap[curRow][curCol].explored)
			return;

		System.out.println("Oh snaapp it's gonna do the thing...");
		checkPitLocalConsistency(curRow, curCol);
		System.out.println("Oh snaapp it did the thing...");
		
		// If stench or breeze, try and figure out where the wumpus or pit is
		for(int n = 0; n < 4; ++n)
		{
			adjCellRow = curRow + (int) Math.sin(Math.PI / 2 * n);
			adjCellCol = curCol + (int) Math.cos(Math.PI / 2 * n);

			if (adjCellRow < 0 || adjCellCol < 0 || adjCellRow > 3 || adjCellCol > 9)
				continue;

//			agentMap[adjCellRow][adjCellCol].pitProb = 0;
//			agentMap[adjCellRow][adjCellCol].wumpProb = 0;
			

			if(breeze)
				updatePitProb(adjCellRow, adjCellCol);
			else
			{
				if(agentMap[adjCellRow][adjCellCol].pitProb > 0)
				{
					System.out.println("Oh snaapp it's gonna do the thing...");
					checkPitLocalConsistency(adjCellRow, adjCellCol);
					System.out.println("Oh snaapp it did the thing...");
				}
				agentMap[adjCellRow][adjCellCol].pitProb = 0;
			}
/*			if(stench)
			{
//				agentMap[adjCellRow][adjCellCol].wumpProb += 0.25;
			}
*/
		}

		agentMap[curRow][curCol].explored = true;
	}

	public void printAgentMap()
	{
		for(int row = 3; row >=0; --row)
		{
			for(int col = 0; col < 10; ++col)
			{
				System.out.printf("%7.2f,%-5.2f", agentMap[row][col].pitProb, agentMap[row][col].wumpProb);
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
		updateMap(stench, breeze, bump);
		printAgentMap();

		
		System.out.println ( "Press 'w' to Move Forward  'a' to 'Turn Left' 'd' to 'Turn Right'" );
		System.out.println ( "Press 's' to Shoot         'g' to 'Grab'      'c' to 'Climb'" );
		
		Scanner in = new Scanner(System.in);
		// Get Input
		System.out.print ( "Please input: " );
		String userInput = in.next();

		// Return Action Associated with Input
		if ( userInput.charAt(0) == 'w' )
		{
			if ( curDir == 0 )
				++curCol;
			else if ( curDir == 1 )
				--curRow;
			else if ( curDir == 2 )
				--curCol;
			else
				++curRow;
			return Action.FORWARD;
		}

		if ( userInput.charAt(0) == 'a' )
		{
			if (--curDir < 0) curDir = 3;	
			return Action.TURN_LEFT;
		}

		if ( userInput.charAt(0) == 'd' )
		{
			if (++curDir > 3) curDir = 0;
			return Action.TURN_RIGHT;
		}

		if ( userInput.charAt(0) == 's' )
			return Action.SHOOT;

		if ( userInput.charAt(0) == 'g' )
			return Action.GRAB;
		
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
