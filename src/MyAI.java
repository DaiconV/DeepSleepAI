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
	private int curDir; // The direction the agent is facing: 0 - right, 1 - down, 2 - left, 3 - up
	
	//Start KL 10/29
	private ArrayList<Action> moves;
	private ArrayList<int[]> route;
	private Stack<Action> backtrackForward;
	private boolean exit = false;
	private boolean shotAlready = false;
	private boolean grabbedGold = false;
	private boolean wumpusDead = false;
	//this one is just to test
	int count = 0;
	private Stack<Action> currentProcess;
	//End KL 10/29

	public MyAI ( )
	{
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================
		//@TODO initialize the map data structure
		
		//Start MA 10/30
		
		
		
		
		
		//End MA 10/30
	
		mapRows = 4;
		mapCols = 10;
		
		agentMap = new Cell[mapRows][mapCols];

		for(int row = 0; row < mapRows; ++row)
			for(int col = 0; col < mapCols; ++col)
				agentMap[row][col] = new Cell();

		
		curRow = 0;
		curCol = 0;
		curDir = 0; 
		
		
		
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
	//End Add MA 10/30
	
	//Replaced by MA 10/30
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
		System.out.println("now I'm at forward");
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
		moves.add(Action.FORWARD);
		return Action.FORWARD;
	}
	
	private Action turnRight() // The direction the agent is facing: 0 - right, 1 - down, 2 - left, 3 - up
	{
		curDir++;
		moves.add(Action.TURN_RIGHT);
		return Action.TURN_RIGHT;
	}
	
	private Action turnLeft()
	{
		curDir--;
		moves.add(Action.TURN_LEFT);
		return Action.TURN_LEFT;
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
		System.out.println("I should just go straight to see what happens");
		System.out.println("stench "+ stench);
		System.out.println("scream "+ scream);
		System.out.println("bump "+ bump);
		System.out.println("glitter "+ glitter);
		System.out.println("scream "+ scream);
		System.out.println("curDir "+ curDir);
		System.out.println("curRow "+ curRow);
		System.out.println("curCol "+ curCol);
		//return Action.FORWARD;
		
		while(count <6)
		{
			if(!currentProcess.empty())
				return currentProcess.pop();
			switch(count)
			{
	        	case 0 :
	        		currentProcess = goUp(); 
	        		break;
	        	case 1 :
	        		currentProcess = goUp(); 
	        		break;
	        	case 2 :
	        		currentProcess = goDown();
	        		break;
	        	case 3 :
	        		currentProcess = goDown();
	        		break;
	        	case 4 :
	        		return Action.CLIMB;
	        	default :
	        		System.out.println("I am at default case now");
	        		
	        		count = 6;
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
