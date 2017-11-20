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
import java.util.LinkedList;
import java.util.Queue;

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

	}

	private Cell[][] agentMap;
	private int mapRows;
	private int mapCols;

	private int curRow;
	private int curCol;
	private int curDir;
	private boolean wumpusFound;
	
	public MyAI ( )
	{
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================
		
		mapRows = 10;
		mapCols = 10;
		
		agentMap = new Cell[mapRows][mapCols];

		for(int row = 0; row < mapRows; ++row)
			for(int col = 0; col < mapCols; ++col)
				agentMap[row][col] = new Cell();

		curRow = 0;
		curCol = 0;
		curDir = 0;
		wumpusFound = false;

		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}

	private int oscillationFunction(int n)
	// This function oscillates in the form of 1, 0, -1, 0, 1, ....
	{
		return (int) (Math.cos(Math.PI / 2 * n));
	}

/*
*******************Wumpus Detection Logic*******************
*/

	private void wumpusConfirmed(int row, int col)
	{
		if(agentMap[row][col].wumpProb != 1)
			return;

		wumpusFound = true;
		for(int i = 0; i < mapRows; ++i)
			for(int j = 0; j < mapCols; ++j)
			{
				if(i == row && j == col)
					continue;
				agentMap[i][j].wumpProb = 0;
			}
	}
	
	private void noWumpusConfirmed(int row, int col)
	//Should only call on cells that have been confirmed as non-pits.
	//This function performs some logical consistency checks on this world.
	{
//		System.out.printf("Cell: row %d; col %d\n", row, col);		
		if(agentMap[row][col].wumpProb != 0)
			return;

		int adjRow;
		int adjCol;
		for (int n = 0; n < 4; ++n)
		{
			if(wumpusFound)
				return;

			adjRow = row + oscillationFunction(n);
			adjCol = col + oscillationFunction(n + 1);
			if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
				continue;
			
//			System.out.printf("First Cells: row %d; col %d\n", adjRow1, adjCol1);

			checkStenchConsistency(adjRow, adjCol);
		}
	}

	private void checkStenchConsistency(int row, int col)
	{
		int potentialWumpusRow = -1;
		int potentialWumpusCol = -1;
		int potentialWumpusCount = 0;
		
		if(!agentMap[row][col].stench)
			return;
				
		int adjRow;
		int adjCol;
		for (int n = 0; n < 4; ++n)
		{
			adjRow = row + oscillationFunction(n);
			adjCol = col + oscillationFunction(n + 1);

			if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
				continue;

			if(agentMap[adjRow][adjCol].wumpProb != 0)
			{
				potentialWumpusRow = adjRow;
				potentialWumpusCol = adjCol;
				++potentialWumpusCount;
			}

		}
		if(potentialWumpusCount == 1)
		{
			agentMap[potentialWumpusRow][potentialWumpusCol].wumpProb = 1;
			wumpusConfirmed(potentialWumpusRow, potentialWumpusCol);
		}
	}

	private void updateWumpusProbability(int row, int col)
	{
		int adjCellCount = 0;
		int stenchCount = 0;

		if(agentMap[row][col].wumpProb == 0)
			return;

		int adjRow;
		int adjCol;
		for (int n = 0; n < 4; ++n)
		{
			adjRow = row + oscillationFunction(n);
			adjCol = col + oscillationFunction(n + 1);

			if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
				continue;

			++adjCellCount;			
			if(agentMap[adjRow][adjCol].stench)
				++stenchCount;

			if(agentMap[adjRow][adjCol].explored && !agentMap[adjRow][adjCol].stench)
			{
				agentMap[row][col].wumpProb = 0;
				noWumpusConfirmed(row, col);
				return;
			}
//			System.out.printf("Cell: %d, %d\n", adjRow, adjCol);
		}

		if(stenchCount == adjCellCount)
		{
			agentMap[row][col].wumpProb = 1;
			wumpusConfirmed(row, col);
			return;
		}

		agentMap[row][col].wumpProb = ((double) stenchCount) / adjCellCount;
	}

/*
********************Pit Detection Logic********************
*/

	private void pitConfirmed(int row, int col)
	{
		if(agentMap[row][col].pitProb != 1)
			return;

		int adjRow;
		int adjCol;
		for(int n = 0; n < 4; ++n)
		{
			adjRow = row + oscillationFunction(n);
			adjCol = col + oscillationFunction(n + 1);
			
			if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
				continue;
			
			agentMap[adjRow][adjCol].breeze = true;
		}
		
		int adjRow1;
		int adjCol1;
		for(int m = 0; m < 4; ++m)
		{
			adjRow1 = row + oscillationFunction(m);
			adjCol1 = col + oscillationFunction(m + 1);
			
			if(adjRow1 < 0 || adjRow1 >= mapRows || adjCol1 < 0 || adjCol1 >= mapCols)
				continue;

			int adjRow2;
			int adjCol2;
			for(int l = 0; l < 2; ++l)
			{
				adjRow2 = adjRow1 + oscillationFunction(m + l);
				adjCol2 = adjCol1 + oscillationFunction(m + l + 1);
		
				if(adjRow2 < 0 || adjRow2 >= mapRows || adjCol2 < 0 || adjCol2 >= mapCols)
					continue;
			
				updatePitProbability(adjRow2, adjCol2);
			}
		}
	}

	private void noPitConfirmed(int row, int col)
	//Should only call on cells that have been confirmed as non-pits.
	//This function performs some logical consistency checks on this world.
	{
//		System.out.printf("Cell: row %d; col %d\n", row, col);		
		if(agentMap[row][col].pitProb != 0)
			return;

		int adjRow;
		int adjCol;
		for (int n = 0; n < 4; ++n)
		{
			adjRow = row + oscillationFunction(n);
			adjCol = col + oscillationFunction(n + 1);
			if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
				continue;
			
//			System.out.printf("First Cells: row %d; col %d\n", adjRow1, adjCol1);

			checkBreezeConsistency(adjRow, adjCol);
		}
	}

	private void checkBreezeConsistency(int row, int col)
	{
		int potentialPitRow = -1;
		int potentialPitCol = -1;
		int potentialPitCount = 0;
		
		if(!agentMap[row][col].breeze)
			return;
				
		int adjRow;
		int adjCol;
		for (int n = 0; n < 4; ++n)
		{
			adjRow = row + oscillationFunction(n);
			adjCol = col + oscillationFunction(n + 1);

			if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
				continue;

			if(agentMap[adjRow][adjCol].pitProb != 0)
			{
				potentialPitRow = adjRow;
				potentialPitCol = adjCol;
				++potentialPitCount;
			}

		}
		if(potentialPitCount == 1)
		{
			agentMap[potentialPitRow][potentialPitCol].pitProb = 1;
			pitConfirmed(potentialPitRow, potentialPitCol);		
		}
	}

	private void updatePitProbability(int row, int col)
	{
		int adjCellCount = 0;
		int breezeCount = 0;

		if(agentMap[row][col].pitProb == 0 || agentMap[row][col].pitProb == 1)
			return;

		int adjRow;
		int adjCol;
		for (int n = 0; n < 4; ++n)
		{
			adjRow = row + oscillationFunction(n);
			adjCol = col + oscillationFunction(n + 1);

			if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
				continue;

			++adjCellCount;			
			if(agentMap[adjRow][adjCol].breeze)
				++breezeCount;

			if(agentMap[adjRow][adjCol].explored && !agentMap[adjRow][adjCol].breeze)
			{
				agentMap[row][col].pitProb = 0;
				noPitConfirmed(row, col);
				return;
			}
//			System.out.printf("Cell: %d, %d\n", adjRow, adjCol);
		}

		if(breezeCount == adjCellCount)
		{
			agentMap[row][col].pitProb = 1;
			pitConfirmed(row, col);
			return;
		}

		agentMap[row][col].pitProb = ((double) breezeCount) / adjCellCount;
	}

	public void updateMap
	(
		boolean stench,
		boolean breeze,
		boolean bump
	)
	{
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
				for(int row = 0; row < mapRows; ++row)
				{
					checkBreezeConsistency(row, curCol);
					if(agentMap[row][curCol].pitProb > 0 && agentMap[row][curCol].pitProb < 1)
						updatePitProbability(row, curCol);

					if(!wumpusFound)
					{
						checkStenchConsistency(row, curCol);
						if(agentMap[row][curCol].wumpProb > 0 && agentMap[row][curCol].wumpProb < 1)
							updateWumpusProbability(row, curCol);
					}
				}
			}
			else if(curDir == 3)
			{
				mapRows = curRow + 1;
				for(int col = 0; col < mapCols; ++col)
				{
					checkBreezeConsistency(curRow, col);
					if(agentMap[curRow][col].pitProb > 0 && agentMap[curRow][col].pitProb < 1)
						updatePitProbability(curRow, col);

					if(!wumpusFound)
					{
						checkStenchConsistency(curRow, col);
						if(agentMap[curRow][col].wumpProb > 0 && agentMap[curRow][col].wumpProb < 1)
							updateWumpusProbability(curRow, col);
					}
				}
			}
			return;
		}

		// If previously explored
		if(agentMap[curRow][curCol].explored)
			return;
		
		agentMap[curRow][curCol].explored = true;


		if(breeze)
		{
			int adjRow;
			int adjCol;
			for (int n = 0; n < 4; ++n)
			{
				adjRow = curRow + oscillationFunction(n);
				adjCol = curCol + oscillationFunction(n + 1);

				if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
					continue;

				updatePitProbability(adjRow, adjCol);
			}
		}
		else
		{
			int adjRow;
			int adjCol;
			for (int n = 0; n < 4; ++n)
			{
				adjRow = curRow + oscillationFunction(n);
				adjCol = curCol + oscillationFunction(n + 1);

				if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
					continue;

				agentMap[adjRow][adjCol].pitProb = 0;
			}
			for (int m = 0; m < 4; ++m)
			{
				adjRow = curRow + oscillationFunction(m);
				adjCol = curCol + oscillationFunction(m + 1);

				if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
					continue;

				noPitConfirmed(adjRow, adjCol);
			}
		}

		if(!wumpusFound)
		{
			if(stench)
			{
				int adjRow;
				int adjCol;
				for (int n = 0; n < 4; ++n)
				{
					adjRow = curRow + oscillationFunction(n);
					adjCol = curCol + oscillationFunction(n + 1);

					if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
						continue;

					updateWumpusProbability(adjRow, adjCol);
				}
			}
			else
			{
				int adjRow;
				int adjCol;
				for (int n = 0; n < 4; ++n)
				{
					adjRow = curRow + oscillationFunction(n);
					adjCol = curCol + oscillationFunction(n + 1);

					if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
						continue;

					agentMap[adjRow][adjCol].wumpProb = 0;
				}
				for (int m = 0; m < 4; ++m)
				{
					adjRow = curRow + oscillationFunction(m);
					adjCol = curCol + oscillationFunction(m + 1);

					if(adjRow < 0 || adjRow >= mapRows || adjCol < 0 || adjCol >= mapCols)
						continue;

					noWumpusConfirmed(adjRow, adjCol);
				}
			}
		}
		noPitConfirmed(curRow, curCol);
		noWumpusConfirmed(curRow, curCol);
	}

	public void printAgentMap()
	{
		for(int row = mapRows - 1; row >=0; --row)
		{
			for(int col = 0; col < mapCols; ++col)
			{
				System.out.printf("%7.2f%5.2f", agentMap[row][col].pitProb, agentMap[row][col].wumpProb);
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
/*		if((breeze || stench) && curCol == 0 && curRow == 0)
			return Action.CLIMB;
		else
		{
			curCol++;
			return moves.remove();
		}
		
*/
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
