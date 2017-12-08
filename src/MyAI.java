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
import java.util.Iterator;
import java.util.Scanner;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Stack;

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

	public class SimpleCell
	{
		public int row;
		public int col;

		public SimpleCell()
		{
			row = -1;
			col = -1;
		}

		public SimpleCell(int r, int c)
		{
			row = r;
			col = c;
		}

		@Override
		public int hashCode()
		{
			return row*100 + col;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null || obj.getClass() != this.getClass())
				return false;
			
			SimpleCell cell = (SimpleCell) obj;
			
			return	(row == cell.row) && 
					(col == cell.col);
		}

		public String toString()
		{
			return String.format("Cell at: %d, %d", row, col);
		}
	}

	public class State extends SimpleCell
	{
		public int dir;
		
		public State()
		{
			super();
			dir = -1;
		}

		public State(int r, int c, int d)
		{
			super(r, c);
			dir = d;
		}
		@Override
		public int hashCode()
		{
			return dir*10000 + row*100 + col;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			
			State state = (State) obj;

			if(obj.getClass() == this.getClass())
				return super.equals(state) && (dir == state.dir);
/*			else if(obj.getClass() == super.getClass())
				return super.equals(state);
*/
			return false;
		}
		@Override
		public String toString()
		{
			return String.format("State: %d, %d, %d", row, col, dir);
		}
	}

	private int mapRows;
	private int mapCols;
	private Cell[][] agentMap;
	private ArrayDeque<Action> commandQueue;
	private HashSet<SimpleCell> goalCells;
	private HashSet<SimpleCell> goalCellsStorage;
	private HashSet<State> tempExploredStates;	//used inside iterDeep()
	private Stack<State> path;

	private int curRow;
	private int curCol;
	private int curDir;
	private boolean wumpusFound;
	private SimpleCell wumpusLocation;
	private boolean wumpusDead;
	private boolean exiting;

	public MyAI ( )
	{
		mapRows = 10;
		mapCols = 10;
		
		agentMap = new Cell[mapRows][mapCols];
		commandQueue = new ArrayDeque<Action>();
		goalCells = new HashSet<SimpleCell>();
		tempExploredStates = new HashSet<State>();
		
//		path = new Stack<State>();
//		currentProcess = new Stack<Action>();

		for(int row = 0; row < mapRows; ++row)
			for(int col = 0; col < mapCols; ++col)
				agentMap[row][col] = new Cell();

		curRow = 0;
		curCol = 0;
		curDir = 0;
		wumpusFound = false;
		wumpusLocation = new SimpleCell();
		wumpusDead = false;
		exiting = false;
	}

	private int oscillationFunction(int n)
	// This function oscillates in the form of 1, 0, -1, 0, 1, ...
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
		wumpusLocation = new SimpleCell(row, col);

		for(int i = 0; i < mapRows; ++i)
			for(int j = 0; j < mapCols; ++j)
			{
				if(i == row && j == col)
					continue;
				agentMap[i][j].wumpProb = 0;

				if(agentMap[i][j].pitProb == 0 && !agentMap[i][j].explored && !exiting)
					goalCells.add(new SimpleCell(i, j));		
			}
	}
	
	private void noWumpusConfirmed(int row, int col)
	//Should only call on cells that have been confirmed to not have the Wumpus.
	//This function performs some logical consistency checks on this world.
	{
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

				if(agentMap[row][col].pitProb == 0 && !agentMap[row][col].explored && !exiting)
					goalCells.add(new SimpleCell(row, col));

				noWumpusConfirmed(row, col);
				return;
			}
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

				if(agentMap[row][col].wumpProb == 0 && !agentMap[row][col].explored && !exiting)
					goalCells.add(new SimpleCell(row, col));

				noPitConfirmed(row, col);
				return;
			}
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
				Iterator<SimpleCell> iter = goalCells.iterator();
				while(iter.hasNext())
				{
					SimpleCell cell = iter.next();
					if(cell.col >= mapCols)
						iter.remove();
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
				Iterator<SimpleCell> iter = goalCells.iterator();
				while(iter.hasNext())
				{
					SimpleCell cell = iter.next();
					if(cell.row >= mapRows)
						iter.remove();
				}
			}
			return;
		}

	
		if(breeze)
		{
			// If not previously explored
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
			// If not previously explored
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
					if(agentMap[adjRow][adjCol].wumpProb == 0 && !agentMap[adjRow][adjCol].explored && !exiting)
						goalCells.add(new SimpleCell(adjRow, adjCol));
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
				// If not previously explored
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
				// If not previously explored
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
						if(agentMap[adjRow][adjCol].pitProb == 0 && !agentMap[adjRow][adjCol].explored && !exiting)
							goalCells.add(new SimpleCell(adjRow, adjCol));
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

	private State forward(State curState)
	{	
		switch(curState.dir)
		{
			case 0:
				return new State(curState.row, curState.col + 1, curState.dir);
			case 1:
				return new State(curState.row - 1, curState.col, curState.dir);
			case 2:
				return new State(curState.row, curState.col - 1, curState.dir);
			case 3:
				return new State(curState.row + 1, curState.col, curState.dir);
			default:
				return new State();
		}
		
	}
	private State turnLeft(State curState)
	{
		return new State(curState.row, curState.col, ((4 + curState.dir - 1) % 4));
	}
	private State turnRight(State curState)
	{
		return new State(curState.row, curState.col, (4 + curState.dir + 1) % 4);
	}

	private Stack<State> recursiveSearch(State curState, int depth)
	{
		Stack<State> tempStack;
//		System.out.println(curState.toString());
		if(goalCells.contains(new SimpleCell(curState.row, curState.col)))
		{
			tempStack = new Stack<State>();
			tempStack.add(curState);
			return tempStack;
		}	

		if(	depth == 0 || 
//			tempExploredStates.contains(curState) ||
			curState.row < 0 || 
			curState.col < 0 || 
			curState.row >= mapRows || 
			curState.col >= mapCols || 
			agentMap[curState.row][curState.col].pitProb != 0 || 
			agentMap[curState.row][curState.col].wumpProb != 0)
			return new Stack<State>();
//		tempExploredStates.add(curState);

		tempStack = recursiveSearch(forward(curState), depth - 1);
		if(!tempStack.empty())
		{
			tempStack.add(curState);
			return tempStack;
		}
		tempStack = recursiveSearch(turnRight(curState), depth - 1);
		if(!tempStack.empty())
		{
			tempStack.add(curState);
			return tempStack;
		}
		tempStack = recursiveSearch(turnLeft(curState), depth - 1);
		if(!tempStack.empty())
		{
			tempStack.add(curState);
			return tempStack;
		}
		
		return new Stack<State>();
	}

	private Stack<State> iterDeep(int row, int col, int dir, int maxDepth)
	{
		Stack<State> tempStack = new Stack<State>();
//		System.out.println(goalCells);
		for(int depth = 0; depth < maxDepth; ++depth)
		{
//			tempExploredStates.clear();	//used inside iterDeep()
			tempStack = recursiveSearch(new State(row, col, dir), depth);
			if(!tempStack.empty())
				return tempStack;
		}
			
		return new Stack<State>();
	}

	private boolean wumpping = false;
	private void goToGoalCell(boolean exit)
	{
		Stack<State> tempStack;
		commandQueue.clear();

		if(((wumpping && !wumpusDead && !exit) || (goalCells.isEmpty() && wumpusFound && !wumpusDead && !exit)) && agentMap[wumpusLocation.row][wumpusLocation.col].pitProb == 0)
		{
			wumpping = true;
//			System.out.println("GOING AFTER WUMPUS");
//			System.out.println("GOING AFTER WUMPUS");
//			System.out.println("GOING AFTER WUMPUS");
			goalCells.add(wumpusLocation);
			tempStack = iterDeep(curRow, curCol, curDir, 80);
//			System.out.println(tempStack.toString());
//			while(wumpusFound);
			if(!tempStack.empty())
			{
				commandQueue = stateStackToActionQueue(tempStack);
				
				commandQueue.removeLast();
				commandQueue.add(Action.SHOOT);
				commandQueue.add(Action.FORWARD);

				return;
			}
			else
			{
				wumpusDead = true;
				goalCells.clear();
			}
		}
		if(exit || goalCells.isEmpty())
		{
			goalCells.clear();
			goalCells.add(new SimpleCell(0, 0));
		}
		
		tempStack = iterDeep(curRow, curCol, curDir, 80);
//		System.out.println(tempStack.toString());
		
		commandQueue = stateStackToActionQueue(tempStack);
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

	public void printSUC()
	{
		for(SimpleCell cell : goalCells)
		{
			System.out.println(cell.toString());
		}
	}

	private ArrayDeque<Action> stateStackToActionQueue(Stack<State> states)
	{
		ArrayDeque<Action> returnQueue = new ArrayDeque<Action>();
		State previousState;
		State currentState;

		if(states.size() < 2)
			return returnQueue;

		previousState = states.pop();

		do
		{
			currentState = states.pop();

			if(previousState.row != currentState.row || previousState.col != currentState.col)
			{
				returnQueue.add(Action.FORWARD);
			}
			else
			{
				if(((currentState.dir + 4 - previousState.dir) % 4) == 3)
				{
					returnQueue.add(Action.TURN_LEFT);
				}
				else
				{
					returnQueue.add(Action.TURN_RIGHT);
				}
			}

			previousState = currentState;
		} while(!states.empty());

		return returnQueue;
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
		Action currentCommand;

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
//		printAgentMap();
		goalCells.remove(new SimpleCell(curRow, curCol));

		if(scream)
			wumpusDead = true;
		
		if(glitter)
		{
			goToGoalCell(glitter);
			exiting = true;

			return Action.GRAB;
		}

//		if()
		goToGoalCell(exiting);


		if(!commandQueue.isEmpty())
		{
			currentCommand = commandQueue.pop();
		
			if(currentCommand == Action.FORWARD)
			{
				if(curDir == 0)
					++curCol;
				else if(curDir == 1)
					--curRow;
				else if(curDir == 2)
					--curCol;
				else if(curDir == 3)
					++curRow;
			}
			else if(currentCommand == Action.TURN_LEFT)
			{
				curDir = (curDir - 1 + 4) % 4;
			}
			else if(currentCommand == Action.TURN_RIGHT)
			{
				curDir = (curDir + 1) % 4;
			}
			return currentCommand;
		}

		return Action.CLIMB;
	}
}
