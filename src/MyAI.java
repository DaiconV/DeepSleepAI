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
		//Start MA 10/30
		public boolean stench;
		public boolean breeze;
		public boolean explored;
		//end MA 10/30
		

		public Cell()
		{
			pitProb = -1;
			wumpProb = -1;
			//Start MA 10/30
			stench = false;
			breeze = false;
			explored = false;
			//End MA 10/30
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
	private int curDir; // The direction the agent is facing: 0 - right, 1 - down, 2 - left, 3 - up
	private boolean wumpusFound;
	
	//Start KL 10/29
	private ArrayList<Action> moves;
	private ArrayList<int[]> route;
	private Stack<Action> backtrackForward;
	private boolean exit = false;
	private boolean shotAlready = false;
	private boolean grabbedGold = false;
	private boolean wumpusDead = false;
	//this one is just to test
	private int count = 0;
	private Stack<Action> currentProcess;
	//End KL 10/29

	public MyAI ( )
	{
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================
		//Start MA 10/30
		
		
		
		
		
		//End MA 10/30
	
		mapRows = 10; //Changed KL 11/16
		mapCols = 10;
		
		agentMap = new Cell[mapRows][mapCols];

		for(int row = 0; row < mapRows; ++row)
			for(int col = 0; col < mapCols; ++col)
				agentMap[row][col] = new Cell();

		
		curRow = 0;
		curCol = 0;
		curDir = 0; 
		wumpusFound = false;
		
		
		//Begin KL 10/29
		
		//How do I see the current precepts?
		//How do I call different moves?
		//I just want to move around and not die and also track the locations
		moves = new ArrayList<Action>(5);
		backtrackForward = new Stack<Action>();
		resetBacktrackForward();
		//route is an arraylist of the path taken by the agent
		//each element is an int[2] array (x, y) 
		route = new ArrayList<int[]>(5);
		currentProcess = new Stack<Action>();
		//End KL 10/29
		
		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}
	//Start add MA 10/30


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
	
		if(breeze)
		{

			if(!agentMap[curRow][curCol].explored)
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
			checkBreezeConsistency(curRow, curCol);
		}
		else
		{
			if(!agentMap[curRow][curCol].explored)
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
		}
		if(!wumpusFound)
		{
			if(stench)
			{
				if(!agentMap[curRow][curCol].explored)
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
				checkStenchConsistency(curRow, curCol);
			}
		
			else
			{
				if(!agentMap[curRow][curCol].explored)
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
		}

		agentMap[curRow][curCol].explored = true;
		noPitConfirmed(curRow, curCol);
		noWumpusConfirmed(curRow, curCol);
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
	
		if(breeze)
		{

			if(!agentMap[curRow][curCol].explored)
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
			checkBreezeConsistency(curRow, curCol);
		}
		else
		{
			if(!agentMap[curRow][curCol].explored)
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
		}
		if(!wumpusFound)
		{
			if(stench)
			{
				if(!agentMap[curRow][curCol].explored)
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
				checkStenchConsistency(curRow, curCol);
			}
		
			else
			{
				if(!agentMap[curRow][curCol].explored)
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
		}

		agentMap[curRow][curCol].explored = true;
		noPitConfirmed(curRow, curCol);
		noWumpusConfirmed(curRow, curCol);
	}

	//End Replace MA 10/30
	public void printAgentMap()
	{
		for(int row = mapRows-1; row >=0; --row)
		{
			for(int col = 0; col < mapCols; ++col)
			{
				System.out.printf("%7.2f,%-5.2f", agentMap[row][col].getPitProb(),agentMap[row][col].getWumpProb());
			}
			System.out.println();
		}
	}
	
	
	
	private Stack<Action> goUp(){
		//THIS FUNCTION DOES NOT CHECK FOR BOUNDS
		curRow++;
		Stack<Action> returnStack = new Stack<Action>();
		//if already up
		if(curDir == 3) // The direction the agent is facing: 0 - right, 1 - down, 2 - left, 3 - up
			returnStack.push(goForward());
		//else if right
		else if(curDir == 0){
			returnStack.push(goForward());
			returnStack.push(turnLeft());
			
		}
		//facing down
		else if(curDir == 1){
			returnStack.push(goForward());
			returnStack.push(turnLeft());
			returnStack.push(turnLeft());
		}
		//facing left
		else if(curDir == 2){
			returnStack.push(goForward());
			returnStack.push(turnRight());
		}
		
		return returnStack;
		
	}
	
	private Stack<Action> goDown(){
		//THIS FUNCTION DOES NOT CHECK FOR BOUNDS
		curRow--;
		Stack<Action> returnStack = new Stack<Action>();
		//if already down
		if(curDir == 1)
			returnStack.push(goForward());
		//else if right
		else if(curDir == 0){
			returnStack.push(goForward());
			returnStack.push(turnRight());
			
		}
		//facing up
		else if(curDir == 3){
			returnStack.push(goForward());
			returnStack.push(turnLeft());
			returnStack.push(turnLeft());
		}
		//facing left
		else if(curDir == 2){
			returnStack.push(goForward());
			returnStack.push(turnLeft());
		}
		
		return returnStack;
		
	}
	
	private Stack<Action> goLeft(){
		//THIS FUNCTION DOES NOT CHECK FOR BOUNDS
		curCol--;
		Stack<Action> returnStack = new Stack<Action>();
		//if already up
		if(curDir == 3) // The direction the agent is facing: 0 - right, 1 - down, 2 - left, 3 - up
		{
			returnStack.push(goForward());
			returnStack.push(turnLeft());
		}
		//else if right
		else if(curDir == 0){
			returnStack.push(goForward());
			returnStack.push(turnLeft());
			returnStack.push(turnLeft());
			
		}
		//facing down
		else if(curDir == 1){
			returnStack.push(goForward());
			returnStack.push(turnRight());
		}
		//facing left
		else if(curDir == 2){
			returnStack.push(goForward());
		}
		
		return returnStack;
		
	}
	
	private Stack<Action> goRight(){
		//THIS FUNCTION DOES NOT CHECK FOR BOUNDS
		curCol++; //goForward used to handle curCol and curRow updating
		Stack<Action> returnStack = new Stack<Action>();
		//if already up
		if(curDir == 3) // The direction the agent is facing: 0 - right, 1 - down, 2 - left, 3 - up
		{
			returnStack.push(goForward());
			returnStack.push(turnRight());
		}
		//else if right
		else if(curDir == 0){
			returnStack.push(goForward());
			
		}
		//facing down
		else if(curDir == 1){
			returnStack.push(goForward());
			returnStack.push(turnLeft());
		}
		//facing left
		else if(curDir == 2){
			returnStack.push(goForward());
			returnStack.push(turnLeft());
			returnStack.push(turnLeft());
		}
		
		return returnStack;
		
	}
	
	private void resetBacktrackForward()
	{
		//maybe I don't need this anymore
		backtrackForward.push(Action.FORWARD);
		backtrackForward.push(Action.TURN_RIGHT);
		backtrackForward.push(Action.TURN_RIGHT);
		
		//backtrackForward.push(Action.TURN_RIGHT);
		//backtrackForward.push(Action.TURN_RIGHT);
		//maybe use this instead
		/*
		backtrackForward.push(goForward());
		backtrackForward.push(turnRight());
		backtrackForward.push(turnRight());
		 * 
		 */
	}
	
	public Action reverse(Action move)
	{
		if(move == Action.TURN_LEFT)
			return turnRight();
		else if(move == Action.TURN_RIGHT)
			return turnLeft();
		else if(move == Action.FORWARD)
		{	//REVERSE ONESELF AND THEN GO FORWARD ONCE
			//HOWEVER STILL NEED TO REVERSE BACK
			//Reversing forward now
			System.out.println("backtrackForward "+backtrackForward);
			if(backtrackForward.empty()) {
				if(moves.size() > 0 && moves.get(moves.size()-1) == Action.FORWARD) {
					moves.remove(moves.size() -1);
				}
				System.out.println("going forward, backTrackForward is empty");
				return goForward();
				//return Action.FORWARD;
				//resetBacktrackForward();
			}
			System.out.println("printing out the pop"+ backtrackForward.peek());
			if(backtrackForward.peek() == Action.TURN_RIGHT) {
				turnRight();
				return backtrackForward.pop();
			}
			else if(backtrackForward.peek() == Action.FORWARD) {
				
				return backtrackForward.pop();
			}	
			 
			
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
		
		System.out.println("Get Out: Moves = " + moves);
		Action latestAction = moves.get(moves.size()-1);
		//moves.remove(moves.size() -1);
			
		return reverse(latestAction);
	}
	
	private Action goForward() // The direction the agent is facing: 0 - right, 1 - down, 2 - left, 3 - up
	{
		System.out.println("now I'm at goForward");
		/*
		if(curDir == 0) { //facing right
			curCol++;
		}
		else if(curDir == 3) { //facing up
			curRow++;
		}
		else if(curDir == 2) { //facing left
			curCol--;
		}
		else if(curDir == 1) { //facing down
			curRow--;
		}
		*/
		moves.add(Action.FORWARD);
		return Action.FORWARD;
	}
	
	private Action turnRight() // The direction the agent is facing: 0 - right, 1 - down, 2 - left, 3 - up
	{
		switch(curDir)
		{
			case 0 : //right
				curDir = 1; //down
				break;
			case 1 : //down
				curDir = 2; //left
				break;
			case 2 : //left
				curDir = 3; // up
				break;
			case 3 : //up
				curDir = 0; //right
				break;
			default :
				System.out.println("Is there a negative direction when turning left?");
				break;		
		}
		moves.add(Action.TURN_RIGHT);
		return Action.TURN_RIGHT;
	}
	
	private Action turnLeft() // The direction the agent is facing: 0 - right, 1 - down, 2 - left, 3 - up
	{
		switch(curDir)
		{
			case 0 : //right
				curDir = 3; //up
				break;
			case 1 : //down
				curDir = 0; //right
				break;
			case 2 : //left
				curDir = 1; // down
				break;
			case 3 :
				curDir = 2; //left
				break;
			default :
				System.out.println("Is there a negative direction when turning left?");
				break;		
		}
		moves.add(Action.TURN_LEFT);
		return Action.TURN_LEFT;
	}
	
	private ArrayList<int[]> findPath(int[] startCoord, int[] destCoord)
	{
		int deltaX = startCoord[0] - destCoord[0];
		int deltaY = startCoord[1] - destCoord[1];
		
		agentMap[curRow][curCol].getPitProb();
		//return down and right
		//recurse right
		findPath(new int[] {startCoord[0]+1, startCoord[1]} , destCoord);
		
		//recurse down
		findPath(new int[] {startCoord[0]+1, startCoord[1]} , destCoord);
		
		//I need to find all the paths first and then return the shortest one so the one with shortest
		//path.length() Can't think of one now but I'll get there
		//Maybe we can use A* to find it.
		
		//filler stuff to allow compile which I'll remove or add to
		ArrayList<int[]> returnArrList = new ArrayList<int[]>();
		return returnArrList;
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
		System.out.println(exit);
		
		System.out.println("stench "+ stench);
		System.out.println("scream "+ scream);
		System.out.println("bump "+ bump);
		System.out.println("glitter "+ glitter);
		System.out.println("scream "+ scream);
		System.out.println("facing: 0 - right, 1 - down, 2 - left, 3 - up");
		System.out.println("curDir "+ curDir);
		System.out.println("curRow "+ curRow);
		System.out.println("curCol "+ curCol);
		System.out.println("count "+ count);
		System.out.println("currentProcess " + currentProcess.toString());
		//return Action.FORWARD;
		
		while(count <7)
		{
			if(!currentProcess.empty())
				return currentProcess.pop();
			switch(count)
			{
	        	case 0 :
	        		currentProcess = goUp(); 
	        		break;
	        	case 1 :
	        		System.out.println("I should be going up now at case 1");
	        		currentProcess = goUp(); 
	        		break;
	        	case 2 :
	        		currentProcess = goDown();
	        		break;
	        	case 3 :
	        		currentProcess = goDown();
	        		break;
	        	case 4 :
	        		currentProcess = goRight();
	        		break;
	        	case 5 :
	        		currentProcess = goLeft();
	        		break;
	        	case 6 :
	        		return Action.CLIMB;
	        	default :
	        		System.out.println("I am at default case now");
	        		
	        		count = 7;
	        		break;
			}
			count++;
			
		}
		
		if(exit){
			if(curRow == 0 && curCol == 0)
				return Action.CLIMB;
			return getOut();
		}
		
		if(scream) {
			wumpusDead = true;
		}
		
		else if ( glitter)
		{
			if(grabbedGold)
			{
				System.out.println("It should not be going here because exit should be true");
				exit = true;
				return getOut();
			}
			else
			{
				exit = true;
				//moves.add(Action.GRAB);
				return Action.GRAB;
			}
		}
		
		else if(bump){
			System.out.println(curDir);
			if (curDir == 0) //facing right
			{
				mapRows = curCol;
				exit = true;
				return getOut();
				
			//
			}
			else if (curDir == 1) //facing up
			{
				assert curRow == 0: "bump at line 574, facing up, curRow should be 0";
				//++curRow;
				if(curCol != mapCols) //if not in upper right corner
				{
					//something kinda complicated
				}
				
					
					
			}
			else if (curDir == 2) //facing left
			{
				assert curCol == 0: "bump at line 579, facing left, curCol should be 0";
				//++curCol;
			}
			else if (curDir == 3)					//facing down
			{
				assert mapRows <= curRow : "bump at line 587, facing down, maybe this board is not square";
				mapRows = curRow;
				if(curRow != mapRows)
				{
					//I should check my precepts and act accordingly but for now maybe I should just skidaddle
					//however I should not go forward
				}
				//--curRow;
			}
			else {
				System.out.println("This shouldn't be happening");
			}
		}
		
		
		
		else if(stench && !shotAlready)
		{
			//moves.add(Action.SHOOT);
			shotAlready = true;
			return Action.SHOOT;
		}
		else if(stench && shotAlready && !bump){
			//Go somewhere else somehow
			if(!wumpusDead)
				return reverse(moves.get(moves.size()));
			else
			{
				return goForward();
			}
		}
		
		else{
			//Nothing going on
			//Anything wrong with this?
			return goForward();
		}
		
			
			
	
		
		
		
		//KL 10/29 Ends
		printAgentMap();
		return Action.CLIMB;
		
		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
	}
	
	//-rdf "path where all your worlds folder is" in program arguments
	// ======================================================================
	// YOUR CODE BEGINS
	// ======================================================================
	
	
	// ======================================================================
	// YOUR CODE ENDS
	// ======================================================================
}
