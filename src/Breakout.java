import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class Breakout extends JPanel implements ActionListener {

	public static Graphics2D g2d = null;
	static boolean gameIsOn = true;
	static int win = 0;
	
	Ball ball;
	Paddle paddle;
	Brick brick;
	Clock clock;
	Clock tempclock;
	JButton replay, undo, pause, start;
	static Stack<Ball> ballObjects;
	static Stack<Paddle> paddleObjects;
	static Stack<Clock> clockObjects;
	static Stack<Brick> brickObjects;
	static int breakLoop = 1; 
	int play = 0, pauseChecker = 0, startChecker = 0;
	
	
	Breakout()
	{
				
	}
	Breakout(Ball ball, Paddle paddle, Brick brick, Clock clock)
	{
		this.ball = ball;
		this.paddle = paddle;
		this.brick = brick;
		this.clock = clock;
		this.addKeyListener(paddle);
		setFocusable(true);
		replay = new JButton("Replay");
		replay.setFocusable(false);
		undo = new JButton("undo");
		undo.setFocusable(false);
		pause = new JButton("Pause");
		pause.setFocusable(false);
		start = new JButton("Start");
		start.setFocusable(false);
		pause.addActionListener(this);
		start.addActionListener(this);
		undo.addActionListener(this);
		replay.addActionListener(this);
		this.add(replay);
		this.add(undo);
		this.add(pause);
		this.add(start);
		ballObjects = new Stack<Ball>();
		paddleObjects = new Stack<Paddle>();
		clockObjects = new Stack<Clock>();
		brickObjects = new Stack<Brick>();
		
		this.ball.registerBall();
		this.clock.registerClock();
		
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g2d = (Graphics2D) g;
		
		//drawing paddle
		paddle.draw(g2d);	
		//drawing ball
		ball.draw(g2d);
		
		g.setFont(new Font("TimesRoman", Font.BOLD, 20));
		clock.draw(g2d);
		
		
		g2d.setColor(Color.ORANGE);
		brick.draw(g2d);
		
		if(!gameIsOn && win != 1)
		{
			g2d.setColor(Color.RED);
			g2d.drawString("GAME OVER!", Constants.GAMEOVER_POS_X, Constants.GAMEOVER_POS_Y);
		}
		if(win == 1)
		{
			g2d.setColor(Color.RED);
			g2d.drawString("You are Victorious!", Constants.WIN_POS_X, Constants.WIN_POS_Y);
		}
	}
	
	public void startGame()
	{		
		while(true) { 
			System.out.print("");
		
			if(breakLoop == 0)
			{
				while(gameIsOn)
				{
					if(breakLoop == 1)
					{
						break;	// break the while(gameIsOn) loop?
					}
					ball.moveBall();
			
					brick.brickCollide(ball);
					storeInstance(ball, paddle, clock, brick);	//clone objects before calling this
					BreakoutObservable observable = new BreakoutObservable(paddle);
					observable.notifyObservers();
			
					if(checkWin(brick))
					{
						gameIsOn = false;
						win = 1;
					}
			
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					repaint();

				}	// End of while loop
			}	// End of if loop
			
			if(breakLoop != 1)
			{
				ball.unregisterBall();
				clock.unregisterClock();
			}
		}
		
		
	}
	
	

	public boolean checkWin(Brick brick)
	{
		for(int i = 0; i < Constants.BRICK_ROW; i++)
		{
			for(int j = 0; j < Constants.BRICK_COLUMN; j++)
			{
				if(brick.getBricksX()[i][j] != -1)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	
	public void storeInstance(Ball ball, Paddle paddle, Clock clock, Brick brick)
	{
		Ball cloneBall = null;
		Paddle clonePaddle = null;
		Brick cloneBrick = null;
		Clock cloneClock = null;
		try {
			cloneBall = (Ball) ball.clone();
			clonePaddle = (Paddle) paddle.clone();
			cloneBrick = (Brick) brick.clone();
			cloneClock = (Clock) clock.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ballObjects.push(cloneBall);
		paddleObjects.push(clonePaddle);
		clockObjects.push(cloneClock);
		brickObjects.push(cloneBrick);
	}
		
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if(e.getSource() == pause)
		{
			pauseChecker++;
			if(pauseChecker % 2 == 0)
			{
				breakLoop = 0;
				pause.setText("Pause");
				clock.pauseFlag = 0;
			}
			else
			{
				breakLoop = 1;
				pause.setText("Resume");
				clock.pauseFlag = 1;
			}
		}
		
		else if(e.getSource() == start)
		{
			startChecker++;
			if(startChecker == 1)
			{
				breakLoop = 0;
				start.setText("Restart");
			}
			else
			{
				ball.setBx(Constants.BALL_POS_X);
				ball.setBy(Constants.BALL_POS_Y);
				ball.setMoveX(Constants.BALL_VEL_X);
				ball.setMoveY(Constants.BALL_VEL_Y);
				paddle.setPx(Constants.PADDLE_POS_X);
				paddle.setPy(Constants.PADDLE_POS_Y);
				this.brick = new Brick();
				this.clock = new Clock();
				ball.registerBall();
				clock.registerClock();
				breakLoop = 0;
				gameIsOn = true;
				win = 0;
				//set initial values then startGame()
			}
		}
		
		else if(e.getSource() == undo)
		{
			breakLoop = 1;
			if(!ballObjects.isEmpty())
				this.ball = ballObjects.pop();
			if(!paddleObjects.isEmpty())
				this.paddle = paddleObjects.pop();
			if(!brickObjects.isEmpty())
				this.brick = brickObjects.pop();
			if(!clockObjects.isEmpty()){
				tempclock = clockObjects.pop();
				this.clock.clockMinutes = tempclock.clockMinutes;
				this.clock.clockSeconds = tempclock.clockSeconds;
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException a) {
				// TODO Auto-generated catch block
				a.printStackTrace();
			}
			
			breakLoop = 0;
			this.repaint();
		}
		
		else if(e.getSource() == replay)
		{
			
		}

	}	
}
