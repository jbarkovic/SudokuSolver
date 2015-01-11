# SudokuSolver
A Java based Sudoku solving algorithm and demo interface.
Algorithm created in 2012

Files:
  - Solver.java: The file containing the solving algorithm.
  - SudokuBoard.java: A swing based GUI for testing the algorithm. Contains the main method.
  - SudokuSave: Save file from the GUI. Usefull when testing algorithm changes against the same puzzle.
  
Algorithm:
  - The algorithm in Solver.java is of my own design and was one of my fist java programs, so the syntax and structure is not a perfect representation of my curent coding abilities.
  - The interface has been updated to make it more efficient and easier to read, but the Solver.java algorithm has remained largely unchanged since it was first written as an example of my early Java experience.
  - Currently the algorithm stores the puzzle and manipulates it in matrix form. It uses the same logic I use when solving a puzzle by hand, this is probably not the most efficient implementation, but this was one of the first "complex" programs I had ever done and at the time I had not seen alot of graph theory yet. 
  - Occasionally the algorithm will hit a dead end and cannot logically see any more values. To get passed this there is a built in guessing feature that will guess one of the empty spaces untill it finds a value that works. This feature can be enabled in the SudokuBoard GUI. These dead ends usually occur after a partial solution has been reached and thus most of the computation has been done and guessing no longer as costly as basic brute force. In any case, when guessing is required, the algorithm will pick a space that has a low number of possible values in order to reduce resulting running time.

Useage:
``` bash
javac Solver.java SudokuBoard.java
java SudokuBoard
```
