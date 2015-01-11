
public class Solver {
	private int[][][] gameData; // the first space is the visible number, the next nine are the logical possibilities the last nine are for logic vectors, if any
	private boolean solutionIsValid;
	private boolean[][] protectedSpaces;
	private boolean allowGuessing;
	private int iterationsToSolve;
	public Solver(boolean allowGuessing) { // creates a new solver and allows guessing or not
		this.allowGuessing = allowGuessing;
		this.iterationsToSolve = 0;
	}
	public int[][] Solve(int[][] inPuzzle) {		
		this.gameData = new int[9][9][10];
		this.protectedSpaces = new boolean[9][9];		
		for (int row=0;row<9;row++) {
			for (int column=0;column<9;column++) {
				this.gameData[row][column][0] = inPuzzle[row][column];
				if (inPuzzle[row][column] != 0) {
					this.protectedSpaces[row][column] = true;
				}
				else {
					this.protectedSpaces[row][column] = false;
				}
			}
		}
		this.initializeLogicalPossibilities();
		int iterations = this.trySolution();
		if (this.countMissing() != 0 && this.allowGuessing) {
			iterations = iterations + this.guess();
		}
		//return the following
		int[][] outArray = new int[9][9];
		for (int row=0;row<9;row++) {
			for (int column=0;column<9;column++) {
				outArray[row][column] = this.gameData[row][column][0];
			}
		}
		System.out.println();
		if (this.checkSolution()) {
			System.out.println("Solution so far is valid");
		}
		else {
			System.out.println("solution so far is NOT valid");
		}
		int missingCount = this.countMissing();
		System.out.println("mising " + missingCount + " values.");
		System.out.println("This took " + iterations + " iterations.");
		this.solutionIsValid = this.checkSolution();
		this.iterationsToSolve = iterations;
		return outArray;
	}
	public boolean isSolutionValid() {
		return this.checkSolution();
	}
	public int getIterations() {
		return this.iterationsToSolve;
	}
	public boolean isSolutionValidAndComplete() {
		boolean complete = true;
		CHECKCOMPLETENESS:
		for (int row=0;row<9;row++) {
			for (int column=0;column<9;column++) {
				if (this.gameData[row][column][0] == 0) {
					complete = false;
					break CHECKCOMPLETENESS;
				}
			}
		}
		return (this.checkSolution() && complete);
	}
	private void removeProtectionForSpace(int row,int column) {
		this.protectedSpaces[row][column] = false;
	}	
	private int[] findCoordsWithMinPossibility(int[][] ignoreCoords) {
		int minNumberOfPossibilities = 200;
		int[] coordsOfMinNumber = new int[] {-1,-1};
			for (int row=0;row<9;row++) {
				for (int column=0;column<9;column++) {
					if (ignoreCoords != null) {
						boolean toBeIgnored = false;
						for (int i=0;i<ignoreCoords.length;i++) {
							if (ignoreCoords[i][0] == row && ignoreCoords[i][1] == column) {
								toBeIgnored = true;
								break;
							}
						}
						if (toBeIgnored) {
							continue;
						}
					}
					int possCount = 0;			
					for (int possibility=1;possibility<10;possibility++) {
						if (this.gameData[row][column][possibility] == possibility) {
							possCount++;
						}
					}
					if (possCount < minNumberOfPossibilities && possCount > 0) {
						minNumberOfPossibilities = possCount;
						coordsOfMinNumber = new int[] {row,column};
					}
				}
			}
		return coordsOfMinNumber;
	}
	private int[][][] cloneGameData(int[][][] inData) {
		int[][][] outData = new int[9][9][10];
		for (int row=0;row<9;row++) {
			for (int column=0;column<9;column++) {
				for (int d=0;d<10;d++) {
					outData[row][column][d] = inData[row][column][d];
				}
			}
		}
		return outData;
	}
	private int[][] removePossibilitiesFromGameData(int[][][] inData) {
		int[][] outData = new int[9][9];
		for (int row=0;row<9;row++) {
			for (int column=0;column<9;column++) {
				outData[row][column] = inData[row][column][0];
			}
		}
		return outData;
	}
	private int guess() {
		System.out.println("guess happened");
		boolean solutionFound = false;
		int[][] coordsToIgnore = null;
		int count = 0 ;
		while (!solutionFound && count < 200000) {	
			count++;
			int[] coordsOfMin = this.findCoordsWithMinPossibility(coordsToIgnore);
			System.out.println("Possibilities at coords of Min are: " );
			this.printLogicalPossibilities(coordsOfMin[0], coordsOfMin[1]);
			for (int possibility = 1; possibility<10;possibility++) {
				if (this.gameData[coordsOfMin[0]][coordsOfMin[1]][possibility] == possibility) {
					int[][][] cloneGameData = this.cloneGameData(this.gameData);
					cloneGameData[coordsOfMin[0]][coordsOfMin[1]][0] = possibility;
					int[][] dataForSolver = this.removePossibilitiesFromGameData(cloneGameData);
					Solver slvr = new Solver(false);
					slvr.Solve(dataForSolver);
					System.out.println("tried to sole: row: " + coordsOfMin[0] + " column: " + coordsOfMin[1] + " with possibility: " + possibility + " Valid and complete result: " + slvr.isSolutionValidAndComplete() + " valid result: " + slvr.isSolutionValid());
					if (slvr.isSolutionValidAndComplete()){
						solutionFound = true;
						this.setValueForSpace(coordsOfMin[0], coordsOfMin[1], possibility);
						count = count + this.trySolution();
						break;
					}
				}
			}
			if (!solutionFound) {
				if (coordsToIgnore == null) {
					coordsToIgnore = new int[0][2];
				}
				int[][] temp = new int[coordsToIgnore.length+1][2];
				for (int i=0;i<coordsToIgnore.length;i++) {
					temp[i] = new int[] {coordsToIgnore[i][0],coordsToIgnore[i][1]};
				}
				temp[temp.length-1] = new int[] {coordsOfMin[0],coordsOfMin[1]};
				coordsToIgnore = temp;
			}			
		}
		return count;
	}
	private int trySolution() {
		int iterations = 0;
		for (int count = 0 ;count < 20000; count++) {
			this.updatePossibilitiesFromKnownValues();
			this.trySetValues();
			this.removePossibilitiesBasedOnLogicVectors();
			this.trySetValues();
			this.updateLogicalPossibilitiesBasedOnSingleOccurence();
			int missingBefore = this.countMissing();
			this.trySetValues();
			int missingAfter = this.countMissing();
			iterations = count;
			if (missingBefore == missingAfter) {
				break;
			}
		}
		return iterations;
	}
	private int countMissing() {
		int missingCount = 0;
		for (int row=0;row<9;row++) {
			for (int column=0;column<9;column++) {
				if (this.gameData[row][column][0] == 0) {
					missingCount++;
				}
			}
		}
		return missingCount;
	}
	private void printLogicalPossibilities(int row,int column) {
		//System.out.println();
		System.out.print("row: " + row + " ; column " + column + ": ");
		for (int i=1;i<10;i++) {
			if (i!=9) {
				System.out.print(this.gameData[row][column][i] + " , ");
			}
			else {
				System.out.print(this.gameData[row][column][i] + "\n");
			}
		}
	}
	private void initializeLogicalPossibilities() {	
		for (int row=0;row<9;row++) {
			for (int column=0;column<9;column++) {
				if (this.gameData[row][column][0] == 0) {
					for (int i=1;i<10;i++) {
						this.gameData[row][column][i] = i;
					}
				}
				else {
					for (int i=1;i<10;i++) {
						this.gameData[row][column][i] = 0;
					}
				}
			}
		}
	}
	private boolean checkSolution() {
		for (int row=0;row<9;row++) {
			int[] valid = new int[] {1,2,3,4,5,6,7,8,9};
			for (int column=0;column<9;column++) {
				if (this.gameData[row][column][0] != 0) {
					if (valid[this.gameData[row][column][0]-1] == 0) {
						System.out.println("row: " + row + " | column: " + column);
						return false;
					}
					valid[this.gameData[row][column][0]-1] = 0;
				}				
			}
		}
		for (int column=0;column<9;column++) {
			int[] valid = new int[] {1,2,3,4,5,6,7,8,9};
			for (int row=0;row<9;row++) {
				if (this.gameData[row][column][0] != 0) {
					if (valid[this.gameData[row][column][0]-1] == 0) {
						System.out.println("row: " + row + " | column: " + column);
						return false;
					}
					valid[this.gameData[row][column][0]-1] = 0;
				}				
			}
		}
		return true;
	}

	private void removeAllPossibilitiesForSpace(int row,int column) {
		for (int i=1;i<10;i++) {
			this.gameData[row][column][i] = 0;
		}
	}
	private boolean setValueForSpace(int row,int column, int value) { // returns true if successfull
		if (this.protectedSpaces[row][column]) {
			return false;
		}
		else {
			this.gameData[row][column][0] = value;
			for (int possibility=1;possibility<10;possibility++) {
				this.gameData[row][column][possibility] = 0;
			}
			this.protectedSpaces[row][column] = true;
		}
		return true;
	}
	private boolean trySetValues() {
		boolean changeHappened = false;	
		for (int row=0;row<9;row++) {
			for (int column=0;column<9;column++) {
				int possibilityCount = 0;
				int possibilityFound = -1;
				for (int possibility=1;possibility<10;possibility++) {
					if (this.gameData[row][column][possibility] == possibility) {
						possibilityCount++;
						possibilityFound = possibility;
					}
				}
				if (possibilityCount == 1) {
					changeHappened = this.setValueForSpace(row, column, possibilityFound);
				}
			}
		}
		if (changeHappened) {
			this.updatePossibilitiesFromKnownValues();
			return this.trySetValues();
		}
		return changeHappened;
	}
	private void removePossibility(int row,int column,int possibility) {
		if (!this.protectedSpaces[row][column]) {
			this.gameData[row][column][possibility] = 0;
		}
	}
	private void updatePossibilitiesFromKnownValues() {
		for (int row=0;row<9;row++) {
			for (int column=0;column<9;column++) {
				if (this.gameData[row][column][0] != 0) {
					for (int subRow=0;subRow<9;subRow++) {
						this.removePossibility(subRow, column, this.gameData[row][column][0]);
					}
					for (int subColumn=0;subColumn<9;subColumn++) {
						this.removePossibility(row, subColumn, this.gameData[row][column][0]);
					}
					int regionRow = row / 3;
					int regionColumn = column / 3;
					for (int subRow=regionRow*3;subRow<regionRow*3+3;subRow++) {
						for (int subColumn=regionColumn*3;subColumn<regionColumn*3+3;subColumn++) {
							this.removePossibility(subRow, subColumn, this.gameData[row][column][0]);
						}
					}
				}
			}
		}
	}
	private void updateLogicalPossibilitiesBasedOnSingleOccurence() {
		for (int regionRow=0;regionRow<3;regionRow++) {
			for (int regionColumn=0;regionColumn<3;regionColumn++) {
				for (int possibility=1;possibility<10;possibility++) {
					int possibilityCount = 0;
					for (int subRow=regionRow*3;subRow<regionRow*3+3;subRow++) {
						for (int subColumn = regionColumn*3;subColumn<regionColumn*3+3;subColumn++) {
							if (this.gameData[subRow][subColumn][possibility] == possibility) {
								possibilityCount++;
							}
						}
					}
					if (possibilityCount == 1) {
						for (int subRow=regionRow*3;subRow<regionRow*3+3;subRow++) {
							for (int subColumn = regionColumn*3;subColumn<regionColumn*3+3;subColumn++) {
								if (this.gameData[subRow][subColumn][possibility] == possibility) {
									this.removeAllPossibilitiesForSpace(subRow, subColumn);
									this.gameData[subRow][subColumn][possibility] = possibility;
								}
							}
						}
					}
				}
			}
		}
	}
	private void removePossibilitiesBasedOnLogicVectors() {
		for (int regionRow=0;regionRow<3;regionRow++) {
			for (int regionColumn=0;regionColumn<3;regionColumn++) {
				for (int possibility=1;possibility<10;possibility++) {
					int[][] possibilitySpaces = new int[][] {{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1},{-1,-1}};
					boolean atLEastOneFound = false;
					for (int subRow=regionRow*3;subRow<regionRow*3+3;subRow++) {
						for (int subColumn = regionColumn*3;subColumn<regionColumn*3+3;subColumn++) {
							if (this.gameData[subRow][subColumn][possibility] == possibility) {
								atLEastOneFound = true;
								FINDEMPTYSPACEINLIST:
								for (int i=0;i<possibilitySpaces.length;i++) {
									if (possibilitySpaces[i][0] == -1) {
										possibilitySpaces[i] = new int[] {subRow,subColumn};
										break FINDEMPTYSPACEINLIST;
									}
								}
							}
						}
					}
					if (atLEastOneFound) {
						int firstRow = possibilitySpaces[0][0];
						int firstColumn = possibilitySpaces[0][1];
						boolean horizontalValid = true;
						boolean verticalValid = true;	
						CHECKHORIZONTAL:					
							for (int i=1;i<possibilitySpaces.length;i++) {
								if (possibilitySpaces[i][0] != firstRow && possibilitySpaces[i][0] != -1) {
									horizontalValid = false;
									break CHECKHORIZONTAL;
								}
							}
						CHECKVERTICAL:
							for (int i=1;i<possibilitySpaces.length;i++) {
								if (possibilitySpaces[i][1] != firstColumn && possibilitySpaces[i][0] != -1) {
									verticalValid = false;
									break CHECKVERTICAL;
								}
							}
						if ((!horizontalValid&&verticalValid) || (horizontalValid&&!verticalValid)) {
							for (int i=0;i<possibilitySpaces.length;i++) {
								if (possibilitySpaces[i][0] != -1) {
									this.protectedSpaces[possibilitySpaces[i][0]][possibilitySpaces[i][1]] = true;
								}
							}
							if (horizontalValid) {
								for (int column=0;column<9;column++) {
									this.removePossibility(possibilitySpaces[0][0], column, possibility);
								}
							}
							else if (verticalValid) {
								for (int row=0;row<9;row++) {
									this.removePossibility(row, possibilitySpaces[0][1], possibility);
								}
							}
							for (int i=0;i<possibilitySpaces.length;i++) {
								if (possibilitySpaces[i][0] != -1) {
									this.protectedSpaces[possibilitySpaces[i][0]][possibilitySpaces[i][1]] = false;
								}
							}
						}
					}
				}
			}
		}
	}
}
