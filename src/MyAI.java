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
	public class Cell
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

	public Cell[][] agentMap;
	int curRow;
	int curCol;

	public MyAI ( )
	{
        //Test
		// ======================================================================
		// YOUR CODE BEGINS
		// ======================================================================
		//@TODO initialize the map data structure
		agentMap = new Cell[4][10];

		for(int row = 0; row < 4; ++row)
			for(int col = 0; col < 10; ++col)
				agentMap[row][col] = new Cell();

		agentMap[0][0].setPitProb(0);	
		agentMap[0][0].setWumpProb(0);	

		curRow = 0;
		curCol = 0;
		// ======================================================================
		// YOUR CODE ENDS
		// ======================================================================
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

		for(int row = 3; row >=0; --row)
		{
			for(int col = 0; col < 10; ++col)
			{
				System.out.printf("%.2f,%.2f\t", agentMap[row][col].getPitProb(),agentMap[row][col].getWumpProb());
			}
			System.out.println();
		}

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
