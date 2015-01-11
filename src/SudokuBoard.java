import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.border.LineBorder;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JToggleButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JLabel;
import javax.swing.JCheckBox;


public class SudokuBoard extends JFrame {

	private JPanel contentPane;
	private GUISpace [][] gameSpaces;
	private int[][] gameSoFar;
	private boolean SolutionIsvalid;
	private JLabel validityLabel;
	private final File saveFile = new File("SudokuSave");
	private JCheckBox guessCheckBox;
	private GUISpace focusOwner = null;
	private JLabel elapsedTimeLabel;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SudokuBoard frame = new SudokuBoard();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void findFocusOwner () {
		if (focusOwner == null) {
			for (int row=0;row<9;row++) {
				for (int col=0;col<9;col++) {
					if (this.gameSpaces [row][col].isFocusOwner()) {
						focusOwner = this.gameSpaces [row][col];
					}
				}
			}
		}
	}
	private void updateSpace(String newNumber) {
		findFocusOwner ();
		if (newNumber.equals("")) {
			this.gameSoFar[focusOwner.coords[0]][focusOwner.coords[1]] = 0;
			focusOwner.setText(newNumber);
		}
		else {
			int val = Integer.parseInt(newNumber);
			if (val > 0 && val < 10) {
				this.gameSoFar[focusOwner.coords[0]][focusOwner.coords[1]] = val;
				focusOwner.setText(newNumber);
			} else {
				System.out.println("Invalid number");
			}
		}
	}
	private boolean moveFocus (GUISpace.DIRECTION dir) {
		findFocusOwner ();
		return focusOwner.selectNext (dir);
	}
	private void updateSpacesFromData() {
		for (int row=0;row<9;row++) {
			for (int column = 0;column<9;column++) {
				if(this.gameSoFar[row][column] == 0) {
					this.gameSpaces[row][column].setText("");
				}
				else {
					this.gameSpaces[row][column].setText(Integer.toString(this.gameSoFar[row][column]));
				}
			}
		}
	}
	private void solve() {
		long before = System.currentTimeMillis();
		Solver slvr = new Solver(false);
		if (this.guessCheckBox.isSelected()) {
			slvr = new Solver(true);
		}
		this.gameSoFar = slvr.Solve(this.gameSoFar);
		long after = System.currentTimeMillis();
		this.updateSpacesFromData();
		if (slvr.isSolutionValid()) {
			this.validityLabel.setText("Valid");
		}
		else {
			this.validityLabel.setText("NotValid");
		}
		this.elapsedTimeLabel.setText("Took " + Long.toString(after - before) + " miliseconds to solve.");
	}
	private void toggleSelected(boolean selected) {
		if (focusOwner != null) {
			focusOwner.setSelected (selected);
			if (selected) focusOwner.setBorder(new LineBorder(new Color(0, 0, 0), 5));
			else focusOwner.setBorder(new LineBorder(new Color(0, 0, 0), 1));
		}
	}
	private void save() throws IOException {
			if (saveFile.exists()) {
				saveFile.delete();
			}
			saveFile.createNewFile();
			FileWriter fw;
			fw = new FileWriter(saveFile,true);
			for (int row=0;row<this.gameSoFar.length;row++) {
				String line = "|";
				for (int column=0;column<this.gameSoFar[row].length;column++) {
					if (column<this.gameSoFar[row].length -1) {
						line = line + this.gameSoFar[row][column] + "|";
					}
					else {
						line = line + this.gameSoFar[row][column] + "|" + "\n";
					}
				}
				fw.write(line);
			}
			fw.close();
	}
	private void load() throws FileNotFoundException {
		if (saveFile.exists()) {
			int[][] outArray = new int[9][9];
			Scanner arrayScan = new Scanner(saveFile);
			String[] rows = new String[9];
			for (int row=0;row<rows.length;row++) {
				rows[row] = "empty";
			}
			while (arrayScan.hasNextLine()) {
				for (int row=0;row<rows.length;row++) {
					if (rows[row].equals("empty")) {
						rows[row] = arrayScan.nextLine();
						break;
					}
				}
			}
			arrayScan.close();
			for (int row=0;row<rows.length;row++) {
				int count = 0;
				Scanner rowScan = new Scanner(rows[row]);
				rowScan.useDelimiter("|");
				while (rowScan.hasNext()) {
					String next = rowScan.next();
					if (!next.equals("|")) {
						outArray[row][count] = Integer.parseInt(next);
						count++;
					}
				}
				rowScan.close();
			}
			this.gameSoFar = outArray;
			this.updateSpacesFromData();
		}
	}
	private void blank() {
		this.gameSoFar = new int[9][9];
		this.SolutionIsvalid = true;
		this.validityLabel.setText("Valid");
		this.updateSpacesFromData();
	}
	public SudokuBoard() {
		setResizable(false);
		this.setTitle ("Sudoku Solver");

		this.gameSoFar = new int[9][9];
		this.gameSpaces = new GUISpace[9][9];
		for (int row=0;row<9;row++) {
			for (int col=0;col<9;col++) {
				this.gameSpaces [row][col] = new GUISpace ();
			}
		}
		for (int row=0;row<9;row++) {
			for (int col=0;col<9;col++) {
				if (this.gameSpaces [row][col] == null) System.out.println ("Null Array");
				if (col > 0) {
					gameSpaces [row][col].Left = gameSpaces [row][col-1];
				}
				if (col < 8) {
					gameSpaces [row][col].Right = gameSpaces [row][col+1];
				}
				if (row > 0) {
					gameSpaces [row][col].Up = gameSpaces [row-1][col];
				}
				if (row < 8) {
					gameSpaces [row][col].Down = gameSpaces [row+1][col];
				}
			}
		}
		this.SolutionIsvalid = true;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 659, 639);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel gamePanel = new JPanel();
		gamePanel.setBounds(12, 12, 540, 540);
		contentPane.add(gamePanel);
		gamePanel.setLayout(new GridLayout(0, 3, 0, 0));

		JPanel [] regions = new JPanel [9];
		for (int i=0;i<9;i++) {
			regions [i] = new JPanel ();
			regions [i].setLayout(new GridLayout(0,3,0,0));
			regions [i].setBorder(new LineBorder(new Color(0, 0, 0), 3));
			gamePanel.add (regions [i]);
		}
		JButton btnNewButton_1 = new JButton("Solve");
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				solve();
			}
		});
		btnNewButton_1.setBounds(564, 165, 80, 58);
		contentPane.add(btnNewButton_1);

		JLabel lblValid = new JLabel("Valid");
		lblValid.setBounds(570, 130, 87, 23);
		contentPane.add(lblValid);
		this.validityLabel = lblValid;

		JButton btnSave = new JButton("Save");
		btnSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					save();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnSave.setBounds(564, 235, 80, 58);
		contentPane.add(btnSave);

		JButton btnLoad = new JButton("Load");
		btnLoad.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					load();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnLoad.setBounds(564, 305, 80, 58);
		contentPane.add(btnLoad);

		JButton btnBlank = new JButton("Blank");
		btnBlank.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				blank();
			}
		});
		btnBlank.setBounds(564, 375, 80, 58);
		contentPane.add(btnBlank);

		JCheckBox chckbxGuess = new JCheckBox("Guess");
		chckbxGuess.setBounds(560, 458, 129, 23);
		this.guessCheckBox = chckbxGuess;
		contentPane.add(chckbxGuess);

		JLabel lblElapsedTime = new JLabel("");
		lblElapsedTime.setBounds(36, 570, 900, 15);
		contentPane.add(lblElapsedTime);
		this.elapsedTimeLabel = lblElapsedTime;

		for (int row=0;row<9;row++) {
			for (int column=0;column<9;column++) {
				GUISpace btnNewButton = this.gameSpaces [row][column];
				btnNewButton.coords = new int [] {row,column};
				btnNewButton.setFont(new Font("Dialog", Font.BOLD, 27));
				btnNewButton.setBorder(new LineBorder(new Color(0, 0, 0), 1));
				btnNewButton.setBackground(Color.WHITE);
				btnNewButton.setBounds(0, 0, 60, 60);
				regions [(row/3)*3 + (column/3)].add(btnNewButton);

				btnNewButton.addActionListener (new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
						boolean selected = abstractButton.getModel().isSelected();
						if (abstractButton instanceof GUISpace) {
							focusOwner = (GUISpace) abstractButton;
							toggleSelected (false);
							toggleSelected (true);
						}
      				}
				});
				btnNewButton.addFocusListener(new FocusAdapter() {
					@Override
					public void focusGained(FocusEvent e) {
						toggleSelected(false);
						Component comp = e.getComponent ();
						if (comp instanceof GUISpace) {
							focusOwner = (GUISpace) comp;
						}
						toggleSelected(true);
					}
					@Override
					public void focusLost(FocusEvent e) {
						toggleSelected(false);
						focusOwner = null;
					}
				});
				btnNewButton.addKeyListener(new KeyAdapter() {
					@Override
					public void keyTyped(KeyEvent e) {
						char number = e.getKeyChar();
						if (Character.isDigit(number)) {
							String numText = Character.toString(number);
							int val = Integer.parseInt(numText);
							if (val == 0) {
								numText = "";
							}
							if (val>-1 && val <10) {
								updateSpace(numText);
							}
						}
					}
					public void keyReleased(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_DOWN) {
							moveFocus (GUISpace.DIRECTION.DOWN);
						}
						else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
							moveFocus (GUISpace.DIRECTION.RIGHT);
						}
						else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
							moveFocus (GUISpace.DIRECTION.LEFT);
						}
						else if (e.getKeyCode() == KeyEvent.VK_UP) {
							moveFocus (GUISpace.DIRECTION.UP);
						}
					}
				});
			}
		}
		this.updateSpacesFromData();
	}
	private static class GUISpace extends JToggleButton {
		protected GUISpace Left = null;
		protected GUISpace Right = null;
		protected GUISpace Up = null;;
		protected GUISpace Down = null;;

		protected int [] coords;
		static enum DIRECTION {
			UP, DOWN, LEFT, RIGHT
		}
		protected boolean selectNext (DIRECTION dir) { /* returns true if there is a next dpace in the given direction*/
			GUISpace next = null;
			switch (dir) {
				case UP 	: {next = Up; break;}
				case DOWN 	: {next = Down; break;}
				case RIGHT 	: {next = Right; break;}
				case LEFT 	: {next = Left; break;}
			};
			if (next != null) {
				next.requestFocus ();
				return true;
			} else {
				System.out.println ("DIR: " + dir + " null");
			}
			return false;
		}
	}
}
