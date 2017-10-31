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

	private void wumpusFound(int row, int col)
	{
		for(int i = 0; i < mapRows; ++i)
			for(int j = 0; j < mapCols; ++j)
				agentMap[i][j].wumpProb = 0;
		agentMap[row][col].wumpProb = 1;
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

			if(adjCellRow < 0 || adjCellCol < 0 || adjCellRow >= mapRows || adjCellCol >= mapCols)
				continue;

			++adjCellCount;
			breezeCount += (agentMap[adjCellRow][adjCellCol].breeze) ? 1: 0;
		}
		agentMap[row][col].pitProb = ((double)breezeCount) / adjCellCount;
	}

	public void updateWumpProb(int row, int col)
	{
		int adjCellRow;
		int adjCellCol;

		int adjCellCount = 0;
		int stenchCount = 0;
		
		if(agentMap[row][col].wumpProb == 0 || agentMap[row][col].wumpProb == 1)
			return;	

		for(int n = 0; n < 4; ++n)
		{
			adjCellRow = row + (int) Math.sin(Math.PI / 2 * n);
			adjCellCol = col + (int) Math.cos(Math.PI / 2 * n);

			if(adjCellRow < 0 || adjCellCol < 0 || adjCellRow >= mapRows || adjCellCol >= mapCols)
				continue;

			++adjCellCount;
			stenchCount += (agentMap[adjCellRow][adjCellCol].stench) ? 1: 0;
		}

		agentMap[row][col].wumpProb = ((double)stenchCount) / adjCellCount;
		
	}

	private void checkPitLocalConsistency(int row, int col)
	{
		// I know... really gross, but it's helpful.
		int adjCellRow1;
		int adjCellCol1;
		int possiblePitCount;
		int adjCellRow2;
		int adjCellCol2;

		// Second loop iterates through cells adjacent to the diagonals and checks to see if they have a breeze
		if(agentMap[row][col].pitProb == 0)
			return;			

		for(int n = 0; n < 4; ++n)
		{
			adjCellRow1 = row + (int) Math.sin(Math.PI / 2 * n);
			adjCellCol1 = col + (int) Math.cos(Math.PI / 2 * n);

			if(adjCellRow1 < 0 || adjCellCol1 < 0 || adjCellRow1 >= mapRows || adjCellCol1 >= mapCols || !agentMap[adjCellRow1][adjCellCol1].breeze)
				continue;

			possiblePitCount = 0;
			// Last loop iterates around the cells with breezes and counts the number of possible pits around it
			for(int m = 0; m < 4; ++m)
			{
				adjCellRow2 = adjCellRow1 + (int) Math.sin(Math.PI / 2 * m);
				adjCellCol2 = adjCellCol1 + (int) Math.cos(Math.PI / 2 * m);
				if(adjCellRow2 < 0 || adjCellCol2 < 0 || adjCellRow2 >= mapRows || adjCellCol2 >= mapCols)
					continue;

				if(agentMap[adjCellRow2][adjCellCol2].pitProb != 0)
				{
//					System.out.printf("row: %d, col: %d\n\trow1: %d, col1: %d\n\t\trow2: %d, col2: %d\n", row, col, adjCellRow1, adjCellCol1, adjCellRow2, adjCellCol2);
					++possiblePitCount;
				}
			
			}

			// If only 1 possible pit, then update that pit probability to 1
			if(possiblePitCount == 1)
			{
				agentMap[row][col].pitProb = 1;
				for(int l = 0; l < 4; ++l)
				{
					adjCellRow1 = row + (int) Math.sin(Math.PI / 2 * l);
					adjCellCol1 = col + (int) Math.cos(Math.PI / 2 * l);
					if(adjCellRow1 < 0 || adjCellCol1 < 0 || adjCellRow1 >= mapRows || adjCellCol1 >= mapCols)
						continue;

					agentMap[adjCellRow1][adjCellCol1].breeze = true;

					if(!agentMap[adjCellRow1][adjCellCol1].explored)
					{
						for(int k = 0; k < 4; ++k)
						{
							adjCellRow2 = adjCellRow1 + (int) Math.sin(Math.PI / 2 * k);
							adjCellCol2 = adjCellCol1 + (int) Math.cos(Math.PI / 2 * k);
							if(adjCellRow2 < 0 || adjCellCol2 < 0 || adjCellRow2 >= mapRows || adjCellCol2 >= mapCols)
								continue;
							updatePitProb(adjCellRow2, adjCellCol2);
						}
					}
				}
				return;
			}	
		}
	}
	

	private void checkWumpLocalConsistency(int row, int col)
	{
		// I know... really gross, but it's helpful.
		int adjCellRow1;
		int adjCellCol1;
		int possibleWumpCount;
		int adjCellRow2;
		int adjCellCol2;

		// Second loop iterates through cells adjacent to the diagonals and checks to see if they have a breeze
		if(agentMap[row][col].wumpProb == 0)
			return;			

		for(int n = 0; n < 4; ++n)
		{
			adjCellRow1 = row + (int) Math.sin(Math.PI / 2 * n);
			adjCellCol1 = col + (int) Math.cos(Math.PI / 2 * n);

			if(adjCellRow1 < 0 || adjCellCol1 < 0 || adjCellRow1 >= mapRows || adjCellCol1 >= mapCols || !agentMap[adjCellRow1][adjCellCol1].stench)
				continue;

			possibleWumpCount = 0;
			// Last loop iterates around the cells with breezes and counts the number of possible pits around it
			for(int m = 0; m < 4; ++m)
			{
				adjCellRow2 = adjCellRow1 + (int) Math.sin(Math.PI / 2 * m);
				adjCellCol2 = adjCellCol1 + (int) Math.cos(Math.PI / 2 * m);
				if(adjCellRow2 < 0 || adjCellCol2 < 0 || adjCellRow2 >= mapRows || adjCellCol2 >= mapCols)
					continue;

				if(agentMap[adjCellRow2][adjCellCol2].wumpProb != 0)
				{
//					System.out.printf("row: %d, col: %d\n\trow1: %d, col1: %d\n\t\trow2: %d, col2: %d\n", row, col, adjCellRow1, adjCellCol1, adjCellRow2, adjCellCol2);
					++possibleWumpCount;
				}
			
			}

			// If only 1 possible pit, then update that pit probability to 1
			if(possibleWumpCount == 1)
			{
				wumpusFound(row, col);
/*				agentMap[row][col].wumpProb = 1;
				for(int l = 0; l < 4; ++l)
				{
					adjCellRow1 = row + (int) Math.sin(Math.PI / 2 * l);
					adjCellCol1 = col + (int) Math.cos(Math.PI / 2 * l);
					if(adjCellRow1 < 0 || adjCellCol1 < 0 || adjCellRow1 >= mapRows || adjCellCol1 >= mapCols)
						continue;

					agentMap[adjCellRow1][adjCellCol1].stench = true;

					if(!agentMap[adjCellRow1][adjCellCol1].explored)
					{
						for(int k = 0; k < 4; ++k)
						{
							adjCellRow2 = adjCellRow1 + (int) Math.sin(Math.PI / 2 * k);
							adjCellCol2 = adjCellCol1 + (int) Math.cos(Math.PI / 2 * k);
							if(adjCellRow2 < 0 || adjCellCol2 < 0 || adjCellRow2 >= mapRows || adjCellCol2 >= mapCols)
								continue;
							updateWumpProb(adjCellRow2, adjCellCol2);
						}
					}
				}*/
				return;
			}	
		}
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
		int diagCellRow;
		int diagCellCol;
		
		agentMap[curRow][curCol].pitProb = 0;
		agentMap[curRow][curCol].wumpProb = 0;
		agentMap[curRow][curCol].stench = stench;
		agentMap[curRow][curCol].breeze = breeze;

		// bump tells us that we hit a wall, record this number
		if(bump)
		{
			if(curDir == 0)
			{
				mapCols = curCol + 1;
				for(int i = 0; i < mapRows; ++i)
					if(agentMap[i][curCol].pitProb > 0 &&  agentMap[i][curCol].pitProb < 1)
						updatePitProb(i, curCol);
			}
			else if(curDir == 3)
			{
				mapRows = curRow + 1;
				for(int j = 0; j < mapCols; ++j)
					if(agentMap[curRow][j].pitProb > 0 &&  agentMap[curRow][j].pitProb < 1)
						updatePitProb(curRow, j);
			}
			return;
		}

		// If previously explored
		if(agentMap[curRow][curCol].explored)
			return;
		
		agentMap[curRow][curCol].explored = true;
		
		// If stench or breeze, try and figure out where the wumpus or pit is
		for(int n = 0; n < 4; ++n)
		{
			adjCellRow = curRow + (int) Math.sin(Math.PI / 2 * n);
			adjCellCol = curCol + (int) Math.cos(Math.PI / 2 * n);

			if (adjCellRow < 0 || adjCellCol < 0 || adjCellRow >= mapRows || adjCellCol >= mapCols)
				continue;
		

//			agentMap[adjCellRow][adjCellCol].pitProb = 0;
//			agentMap[adjCellRow][adjCellCol].wumpProb = 0;
			

			if(breeze)
			{
				checkPitLocalConsistency(adjCellRow, adjCellCol);
				updatePitProb(adjCellRow, adjCellCol);
			}
			else
			{	
				if(agentMap[adjCellRow][adjCellCol].pitProb > 0)
				{
					agentMap[adjCellRow][adjCellCol].pitProb = 0;

					for(int m = 0; m < 4; ++m)
					{
							diagCellRow = adjCellRow + (int) (Math.cos(Math.PI / 2 * m)) + (int) (Math.sin(Math.PI / 2 * m));
							diagCellCol = adjCellCol + (int) (Math.cos(Math.PI / 2 * (m + 1))) + (int) (Math.sin(Math.PI / 2 * (m + 1)));

							if(diagCellRow < 0 || diagCellCol < 0 || diagCellRow >= mapRows || diagCellCol >= mapCols || agentMap[diagCellRow][diagCellCol].pitProb <= 0)
								continue;
							checkPitLocalConsistency(diagCellRow, diagCellCol);
					}
				}
				agentMap[adjCellRow][adjCellCol].pitProb = 0;
			}

			if(stench)
			{
				checkWumpLocalConsistency(adjCellRow, adjCellCol);
				updateWumpProb(adjCellRow, adjCellCol);
			}
			else
			{	
				if(agentMap[adjCellRow][adjCellCol].wumpProb > 0)
				{
					agentMap[adjCellRow][adjCellCol].wumpProb = 0;

					for(int m = 0; m < 4; ++m)
					{
							diagCellRow = adjCellRow + (int) (Math.cos(Math.PI / 2 * m)) + (int) (Math.sin(Math.PI / 2 * m));
							diagCellCol = adjCellCol + (int) (Math.cos(Math.PI / 2 * (m + 1))) + (int) (Math.sin(Math.PI / 2 * (m + 1)));

							if(diagCellRow < 0 || diagCellCol < 0 || diagCellRow >= mapRows || diagCellCol >= mapCols || agentMap[diagCellRow][diagCellCol].wumpProb <= 0)
								continue;
							checkWumpLocalConsistency(diagCellRow, diagCellCol);
					}
				}
				agentMap[adjCellRow][adjCellCol].wumpProb = 0;
			}

		}

		for(int m = 0; m < 4; ++m)
		{
				diagCellRow = curRow + (int) (Math.cos(Math.PI / 2 * m)) + (int) (Math.sin(Math.PI / 2 * m));
				diagCellCol = curCol + (int) (Math.cos(Math.PI / 2 * (m + 1))) + (int) (Math.sin(Math.PI / 2 * (m + 1)));

				if(diagCellRow < 0 || diagCellCol < 0 || diagCellRow >= mapRows || diagCellCol >= mapCols || agentMap[diagCellRow][diagCellCol].pitProb <= 0)
					continue;
				checkPitLocalConsistency(diagCellRow, diagCellCol);
		}

	}

	public void printAgentMap()
	{
		for(int row = mapRows - 1; row >=0; --row)
		{
			for(int col = 0; col < mapCols; ++col)
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

		if(bump)		
			if (curDir == 0)
				--curCol;
			else if (curDir == 1)
				++curRow;
			else if (curDir == 2)
				++curCol;
			else
				--curRow;

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
