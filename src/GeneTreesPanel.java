import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

public class GeneTreesPanel extends JPanel {
	private ArrayList<GeneTree> trees = new ArrayList<GeneTree>();
	private ArrayList<SunSpeck> light = new ArrayList<SunSpeck>();
	
	private int stepSpeed = 200;
	private int step = 0;
	private int maxStep = 10000;
	private int genNum = 0;
	
	private Timer time = new Timer(stepSpeed, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            tick();
        }
	});
	
	public GeneTreesPanel(int width, int height) {
		super();
		setFocusable(true);
		requestFocusInWindow();
		this.setSize(width, height);
		
		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_P:
					if (time.isRunning()) {
						time.stop();
					} else {
						time.start();
					}
					break;
				case KeyEvent.VK_L:
					skipGen();
					break;
				}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		
		time.start();
	}
	
	private void skipGen() {
		time.stop();
		while (step < maxStep) {
			step();
		}
		//checkNewGen();
		time.start();
	}
	
	private void tick() {
		step();
		repaint();
		//checkNewGen();
	}
	
	private void step() {
		
		
		step++;
	}
	
	private void checkNewGen() {
		// quit method if step limit isn't reached
		if (step != maxStep) {
			return;
		}
	}

	public void paintComponent(Graphics g) {
		g.setColor(new Color(146, 184, 244));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(new Color(183, 85, 23));
		g.fillRect(0, this.getHeight()-100, this.getWidth(), 100);
		
		for (GeneTree t : trees) {
			t.draw(g);
		}
		for (SunSpeck s : light) {
			s.draw(g);
		}
		
		g.setColor(Color.WHITE);
		g.drawString("Timestep: " + step + " of " + maxStep, 0, 15);
		g.drawString("Speed: " + stepSpeed + "steps/sec", 0, 30);
		g.drawString("Generation: " + genNum, 0, 45);
		//g.drawString("Fitness highscore: " + maxFitness, 0, 60);
		
		g.drawString("Pause simulation: P", 0, 90);
		g.drawString("Skip this generation: L", 0, 105);
	}
}