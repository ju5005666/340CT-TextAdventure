package WordScrambler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;

/**
 * User interface for ScrambledWordGame.  The UI uses a WordPair
 * instance to obtain the letters to draw, to obtain hints, and to check
 * the solutions.  A ScoreCalculator is used to keep a running score that
 * is updated on the UI as it decreases.  A WordScrambler is used to
 * obtain words and randomize them to start each round of the game.
 */
public class WordGameUI extends JPanel
{
  /**
   * Interval in milliseconds for refreshing the score.
   */
  private static final int TIMER_INTERVAL = 1000;
  
  /**
   * Format string for printing scores at top of window.
   */
  private static final String SCORE_FORMAT_STRING = "Time: %-10d Score: %-10d";  
  private static String QUESTION_FORMAT_STRING = "Read the question.";  
  
  /**
   * Serial version number, not used.
   */
  private static final long serialVersionUID = 1L;
  
  // Swing components
  private JButton submitButton;
  private JButton hintButton;
  private JButton rescrambleButton;
  private JButton startButton;
  private JLabel scoreLabel;
  private JLabel questionLabel;
  private Timer timer;
  
  /**
   * Panel where letters are drawn and can be moved with the mouse.
   */
  private WordCanvas wordPanel;
  
  /**
   * WordPair instance for current round.
   */
  private WordPair wordPair;

  /**
   * Score calculator for this UI.
   */
  private ScoreCalculator scorer;
  
  /**
   * Word generator for this UI.
   */
  private Words questionList;
  private Words answerList;
  
  /**
   * Total score obtained since UI was started.
   */
  private int totalScore;
  
  /**
   * Flag indicating whether there is a round in progress.
   */
  private boolean roundOver;
  private int failAttempt = 3;
  /**
   * Records start time of the round for scoring purposes.
   */
  private long startTime;
  private static int timerCounter = 60;
  
  /**
   * Random number generator used for selecting words with the Words object.
   */
  private Random rand;
  
  /**
   * Permutation generator used for scrambling words with the WordScrambler.
   */
  private PermutationGenerator pg;
  
  /**
   * Constructs a new instance of the UI main panel.
   * @param scorer
   *   score calculator
   * @param wordList
   *   Words object to use for generating words
   * @param rand
   *   random number generator for selecting words
   * @param pg
   *   permutation generator for scrambling words
   */
  public WordGameUI(ScoreCalculator scorer, Words questionList, Words answerList, Random rand, PermutationGenerator pg)
  {
    this.scorer = scorer;
    this.questionList = questionList;
    this.answerList = answerList;
    this.rand = rand;
    this.pg = pg;
    roundOver = true;
    UIMain.setFrameStatus(false);
    
    // create swing controls
    // (Start button will be relabeled "Give up" when a round
    // is in progress.)
    submitButton = new JButton("Submit");
    hintButton = new JButton("Hint");
    rescrambleButton = new JButton("Rescramble");
    startButton = new JButton("Start");
    String labelText = String.format(SCORE_FORMAT_STRING, 60, 0, totalScore);
    scoreLabel = new JLabel(labelText);
    String questionText = String.format(QUESTION_FORMAT_STRING);
    questionLabel = new JLabel(questionText);
 
    // create a timer that will fire every TIMER_INTERVAL ms
    // for updating the score
    timer = new Timer(TIMER_INTERVAL, new TimerCallback());

    // running score will be centered in top panel and use larger font
    scoreLabel.setFont(new Font("Serif", Font.PLAIN, 24));
    scoreLabel.setVerticalAlignment(SwingConstants.CENTER);
    scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);

    // panel for buttons and total score
    JPanel btnGameInstructionPanel = new JPanel(new BorderLayout());
    
    JPanel buttonPanel = new JPanel(new FlowLayout());
    buttonPanel.add(startButton);
    buttonPanel.add(hintButton);
    buttonPanel.add(rescrambleButton);
    buttonPanel.add(submitButton);
    
    btnGameInstructionPanel.add(buttonPanel, BorderLayout.NORTH);
    
    //add some game instructions
	JPanel gameRules = new JPanel(new BorderLayout());
	JLabel title = new JLabel("Game Instructions:");
	title.setFont(new Font("Serif", Font.PLAIN, 20));
	title.setHorizontalAlignment(SwingConstants.CENTER);
	
	JTextArea jtxtInfo = new JTextArea();
	Border border = BorderFactory.createEmptyBorder(5,15,5,15);
	jtxtInfo.setBorder(border);
	jtxtInfo.setBackground(new Color(0,0,0,0));
	jtxtInfo.setEditable(false);
	jtxtInfo.setWrapStyleWord(true);
	jtxtInfo.setLineWrap(true);
	jtxtInfo.setHighlighter(null);
	jtxtInfo.getCaret().deinstall(jtxtInfo);
	jtxtInfo.setText("1. Click 'Start' to begin. \n2. Read the question provided and try to guess the answer with the provided alphabets. \n3. Try to form the correct answer by dragging the alphabets to the correct position. \n4. Click on 'Hint' if you need some help. Note that using hints will reduce your score. \n5. Click on 'Rescramble' if you want to randomly arrange the alphabets. Note that rescrambling will reduce your score. \n6. Click on 'Give up' to change the question. Note that you only have 3 chances. Otherwise, no points is rewarded. \n7. Click on 'Submit' if you think you have the right answer. Points will be rewarded for the correct answer. Note that points will be reduced if the answer is incorrect.");
	gameRules.add(title, BorderLayout.NORTH);
	gameRules.add(jtxtInfo, BorderLayout.CENTER);
    
	btnGameInstructionPanel.add(gameRules);
	
    // Question
    questionLabel.setFont(new Font("Serif", Font.PLAIN, 24));
    questionLabel.setVerticalAlignment(SwingConstants.CENTER);
    questionLabel.setHorizontalAlignment(SwingConstants.CENTER);

    // add callbacks for buttons
    ActionListener submitHandler = new SubmitButtonHandler();
    ActionListener hintHandler = new HintButtonHandler();    
    ActionListener rescrambleHandler = new RescrambleButtonHandler();
    ActionListener startHandler = new StartButtonHandler();
    
    submitButton.addActionListener(submitHandler);
    hintButton.addActionListener(hintHandler);
    rescrambleButton.addActionListener(rescrambleHandler);
    startButton.addActionListener(startHandler);

    // initially only the start button is enabled
    hintButton.setEnabled(false);
    rescrambleButton.setEnabled(false);
    submitButton.setEnabled(false);
    
    // custom panel for drawing the letters
    wordPanel = new WordCanvas();
    wordPanel.add(questionLabel);
    
    // score on top, buttons in middle, letters on bottom
    this.setLayout(new BorderLayout());
    this.add(btnGameInstructionPanel, BorderLayout.CENTER);
    this.add(scoreLabel, BorderLayout.NORTH);
    this.add(wordPanel, BorderLayout.SOUTH);
  }
  
  /**
   * Helper method called when current round ends, either by
   * successfully solving the word or by giving up.
   * @param solved
   *   true if the puzzle was solved, false otherwise
   */
  private void endRound(boolean solved)
  {
	if (solved == false)
		failAttempt--;
	
    roundOver = true;
    timer.stop();
    startButton.setText("Start");
    hintButton.setEnabled(false);
    rescrambleButton.setEnabled(false);
    submitButton.setEnabled(false);
    wordPanel.over(solved);

    int elapsedTime = (int) (System.currentTimeMillis() - startTime);
    int currentScore = 0;
    if (solved)
    {
      currentScore = scorer.getPossibleScore();
      UIMain.setScramblerStatus(true);
      UIMain.setFrameStatus(true);
      
      UIMain.stopThread = true;
      TextAdventure.Main.showWindow();
      UIMain.frame.dispose();
      //System.exit(0);
    }
    else
    	UIMain.setScramblerStatus(false);

    totalScore += currentScore;
    String labelText = String.format(SCORE_FORMAT_STRING, timerCounter, currentScore, totalScore);
    scoreLabel.setText(labelText);
    repaint();
    
	if (failAttempt == 2) {
		currentScore = -1; 
	}
	else if (failAttempt == 1) {
		currentScore = -2; 
	}
    
    if(failAttempt == 0) {
    	currentScore = -14;    	
    	UIMain.setScramblerStatus(false);
        UIMain.setFrameStatus(true);
        
        //System.exit(0);
        UIMain.stopThread = true;
        TextAdventure.Main.showWindow();
        UIMain.frame.dispose();
    }
  }
  
  /**
   * Helper method called to start a new round.
   */
  private void startRound()
  {
    try
    {
      // Depending on how the WordScrambler is implemented,
      // this may throw FileNotFoundException...
      int questionNo = questionList.getQuestion(rand);
      
      String question = questionList.getWord(questionNo);
      String answer = answerList.getWord(questionNo);

      String hidden = WordScrambler.scramble(answer, pg);
      
      wordPair = new WordPair(answer, hidden);

      if (wordPair.isSolutionPossible())
      {
    	  int initialScore = TextAdventure.Main.getPlayerScore();
    	  
    	  /*
    	  if (failAttempt == 3)
    		  initialScore = 50;
    	  else if (failAttempt == 2)
    			initialScore = 25;
    	  else if (failAttempt == 1)
    			initialScore = 10;
    	  else
    		  initialScore = 0; */
    	  
        roundOver = false;
        startButton.setText("Give up");
        hintButton.setEnabled(true);
        rescrambleButton.setEnabled(true);
        submitButton.setEnabled(true);
        scorer.start(answer.length(), initialScore);
        initialScore = scorer.getPossibleScore();
        String labelText = String.format(SCORE_FORMAT_STRING, 60, initialScore, totalScore);
        scoreLabel.setText(labelText);
        questionLabel.setText(question);
        wordPanel.startRound(wordPair);
        startTime = System.currentTimeMillis();
        timer.start(); 
      }
      else
      {
        String msg = "Error: isSolutionPossible returns false for '" + answer + "' and '" + hidden + "'";
        JOptionPane.showMessageDialog(this.getParent(), msg);        
      }
    }
    catch (FileNotFoundException e)  // possible FileNotFoundException
    {
      String msg = "Unable to open word file: " + e.getMessage();
      JOptionPane.showMessageDialog(this.getParent(), msg);        
    }
    catch (Exception e)  // possible FileNotFoundException
    {
      String msg = "Exception: " + e;
      JOptionPane.showMessageDialog(this.getParent(), msg);
      
      //System.exit(1);
      UIMain.stopThread = true;
      TextAdventure.Main.showWindow();
      UIMain.frame.dispose();
    }

  }
  
  /**
   * Callback for the start (or give up) button.
   */
  private class StartButtonHandler implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      // This button is labeled "Start" if a round is not in progress,
      // and "Give up" if a round is in progress.
      if (roundOver)
      {
    	timerCounter = 60;
        startRound();
      }
      else
      {
        endRound(false);
      }
      repaint();
    }  
  }
  
  /**
   * Callback for submit button.
   */
  private class SubmitButtonHandler implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      // if the solution is correct, end the round and add the score,
      // otherwise impose penalty for incorrect guess
      if (wordPair.checkSolution())
      {
        endRound(true);
        //To Be Updated with Text-Adventure Source Code
        TextAdventure.Main.setPlayerScore(totalScore);
        
        //System.exit(0);
        UIMain.stopThread = true;
        TextAdventure.Main.showWindow();
        UIMain.frame.dispose();
      }
      else
      {
        scorer.applyIncorrectGuessPenalty();
      }
    }    
  }
  
  /**
   * Callback for hint button.
   */
  private class HintButtonHandler implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      wordPair.doLetterHint();
      scorer.applyHintPenalty();
      wordPanel.reinitialize();
    }   
  }

  /**
   * Callback for rescramble button.
   */
  private class RescrambleButtonHandler implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      wordPair.rescramble(pg);
      if (!wordPair.isSolutionPossible())
      {
        String word = wordPair.getRealWord();
        String hidden = wordPair.getScrambledWord();
        String msg = "Error: isSolutionPossible returns false for '" + word + "' and '" + hidden + "'";
        JOptionPane.showMessageDialog(WordGameUI.this.getParent(), msg); 
        
        UIMain.stopThread = true;
        TextAdventure.Main.showWindow();
        UIMain.frame.dispose();
        //System.exit(1);
      }

      scorer.applyRescramblePenalty(failAttempt);
      wordPanel.reinitialize();
    }   
  }

  /**
   * Timer callback updates the score each time timer fires.
   */
  private class TimerCallback implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      int elapsed = (int) (System.currentTimeMillis() - startTime);
      
      // round to nearest multiple of interval
      int numIntervals = elapsed / TIMER_INTERVAL;
      int left = elapsed % TIMER_INTERVAL;
      if (left >= TIMER_INTERVAL / 2)
      {     
        numIntervals += 1;
      }
      elapsed = numIntervals * TIMER_INTERVAL;
      
      if(timerCounter != 0)
    	  timerCounter--;
      else {
    	  startButton.doClick();
      }
      
      int currentScore = scorer.getPossibleScore();
      String labelText = String.format(SCORE_FORMAT_STRING, timerCounter, currentScore, totalScore);
      
      scoreLabel.setText(labelText);
      repaint();
    }
    
  }
}
