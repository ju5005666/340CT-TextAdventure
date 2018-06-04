package WordScrambler;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Main class for starting an instance of <code>WordGameUI</code>.
 */
public class UIMain
{
	/**
	 * Entry point.
	 */

	static boolean windowClose = false;
	private static boolean scramblerComplete;
	static JFrame frame;
	volatile static boolean stopThread; 

	static Thread checkingThread = new Thread(){	
		public void run(){
			while (!stopThread){
				while(windowClose != true) {
					try {
						//System.out.println("Entered checking thread");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						System.out.println(e);
						setScramblerStatus(false);
					}
				}
			}
		}
	};

	public static boolean main(boolean scramberSuccess)
	{
		setScramblerStatus(false);
		// Edit here to use a different word file
		final String filename = "words2.txt";

		// Edit here to change the seed for the WordScrambler's
		// random number generator.  For unpredictable behavior,
		// use the system time as a seed:
		final int seed = (int) System.currentTimeMillis();
		//final int seed = 42;

		// Boilerplate code for safely starting up a Swing application
		// on the event-handling thread.
		Runnable r = new Runnable()
		{
			public void run()
			{
				createAndShow(filename, seed);
			}
		};

		SwingUtilities.invokeLater(r);

		return getScramblerStatus();
	}

	/**
	 * Helper method for creating and starting the UI.
	 * @param filename
	 *   filename for word list
	 * @param seed
	 *   seed for random number generator in the WordScrambler
	 */
	private static void createAndShow(String filename, int seed)
	{
		// the main window for this application
		frame = new JFrame("Com S 227 Word Scramble");
		//used for programmatically closing the JFrame later
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		frame.setUndecorated(true);
		frame.setLocationRelativeTo(null);

		try
		{
			// this may throw FileNotFoundException
			Words wordList = new Words(filename);   

			// scoring based on 
			//    5 seconds per letter
			//    5 second penalty for a letter hint
			//    1/10 second penalty for rescrambling
			//    1-second penalty for submitting an incorrect guess
			ScoreCalculator calc = new ScoreCalculator(5000, 5000, 100, 1000);
			Random rand = new Random(seed);
			PermutationGenerator pg = new PermutationGenerator(rand);

			// construct the main game panel and add it to the frame
			JPanel mainPanel = new WordGameUI(calc, wordList, rand, pg);
			frame.getContentPane().add(mainPanel);

			// size the frame based on the preferred size of the mainPanel
			frame.pack();

			// make sure it closes when you click the close button on the window
			frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);

			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent event) {
					windowClose = true;
				}
			});

			System.out.println("Ready to rock and roll");

			// rock and roll...
			stopThread = false;
			new Thread(checkingThread).start();

			frame.setVisible(true);
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e);
			JOptionPane.showMessageDialog(frame, "Unable to open word file: " + e.getMessage());
			frame.dispose();
			windowClose = true;
		}
	}

	public static void setFrameStatus(boolean frameClose){
		windowClose = frameClose;
	}

	public static void setScramblerStatus(boolean scramblerWin){
		scramblerComplete = scramblerWin;
	}

	public static boolean getScramblerStatus(){
		return scramblerComplete;
	}
}