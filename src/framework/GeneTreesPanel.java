package framework;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import simulation.GeneTree;
import simulation.SunSpeck;

@SuppressWarnings("serial")
public class GeneTreesPanel extends JPanel {
	private ArrayList<GeneTree> trees = new ArrayList<GeneTree>();
	private HashSet<SunSpeck> sun = new HashSet<SunSpeck>();
	private int groundLevel = 400;
	
	private int tickSpeed = 2;
	private int tickNum = 0;
	private int maxTick = 10000;
	private int treeIndex = 0;
	private int generation = 0;
	
	private boolean ticking = true;
	
	public int xMouse = 0;
	public int yMouse = 0;
	
	private Timer time = new Timer(tickSpeed, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	if (ticking) {
        		tick();
        	}
            repaint();
        }
	});
	private long sysTime;
	
	public GeneTreesPanel(int width, int height) {
		super();
		setFocusable(true);
		requestFocusInWindow();
		this.setSize(width, height);
		
		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_P:
					ticking = !ticking;
					break;
				case KeyEvent.VK_O:
					finishGen();
					break;
				case KeyEvent.VK_I:
					finishInd();
					break;
				case KeyEvent.VK_B:
					finishNumGens(10);
					break;
				case KeyEvent.VK_N:
					finishNumGens(50);
					break;
				case KeyEvent.VK_M:
					finishNumGens(100);
					break;
				case KeyEvent.VK_D:
					GeneTrees.debug = !GeneTrees.debug;
					break;
				case KeyEvent.VK_F1:
					Loader.saveGame();
					break;
				case KeyEvent.VK_F2:
					Loader.loadGame();
					break;
				case KeyEvent.VK_R:
					continuousGenAndSave();
					break;
				}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		
		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {}
			public void mouseMoved(MouseEvent e) {
				xMouse = e.getX();
				yMouse = e.getY();
			}
		});
	}
	
	public void init() {
		// populate the list of trees
		for (int i = 0; i < 10000; i++) {
			trees.add(new GeneTree());
		}
		
		sysTime = System.currentTimeMillis();
		time.start();
	}
	
	/*
	 * resets the simulation.
	 * used when loading a generation from file
	 */
	public void reset(ArrayList<GeneTree> trees, int gen) {
		tickNum = 0;
		treeIndex = 0;
		this.trees = trees;
		this.generation = gen;
		sysTime = System.currentTimeMillis();
		sun.clear();
	}
	
	private void continuousGenAndSave() {
		time.stop();
		
		int saveDelay = 2; // interval of generations by which to save
        String fileName = JOptionPane.showInputDialog(this, "name this generation stream", null);
		
		System.out.println("computing generations, saving every " + saveDelay + " gens");
		while (true) {
			// the first tick of each marked generation, save the generation
			if (generation % saveDelay == 0 &&
				treeIndex == 0 &&
				tickNum == 0) {
				Loader.saveGame(fileName + "_gen" + generation);
			}
			
			tick(); // ad infinitum
		}
	}
	
	private void finishNumGens(int num) {
		time.stop();
		System.out.println("computing " + num + " generations");
		int currGen = generation;
		while (generation < currGen + num) {
			tick();
		}
		System.out.println("done computing " + num + " generations");
		time.start();
	}
	
	private void nextGen() {
		long newTime = System.currentTimeMillis();
		long timeDiff = (newTime - sysTime)/1000;
		sysTime = newTime;
		System.out.println("done simulating gen " + generation + " in " + timeDiff + " seconds");
		
		generation++;
		
		Collections.sort(trees); // sort from worst adapted to best adapted
		Collections.reverse(trees); // reverse list - it is now from best to worst
		
		int numTrees = trees.size();
		for (int i = 0; i < numTrees/2; i++) {
			trees.get(i).resetFitness(); // reset this tree's fitness
			
			// replace one of the lesser half of trees with a mutated copy of one of the better half of trees
			trees.set(numTrees/2 + i, new GeneTree(trees.get(i)));
		}
		
		// not significant
//		newTime = System.currentTimeMillis();
//		timeDiff = (newTime - sysTime)/1000;
//		sysTime = newTime;
//		System.out.println("children mutated in " + timeDiff + " seconds");
		
		treeIndex = 0;
	}
	
	private void finishGen() {
		time.stop();
		
		int currGen = generation;
		while (currGen == generation) {
			tick();
		}
		
		time.start();
	}
	
	// resets the sandbox for the next individual
	private void nextInd() {
		tickNum = 0;
		sun.clear();
		treeIndex++;
		
		if (treeIndex == trees.size()) {
			nextGen();
		}
		
		if (treeIndex % (trees.size()/4) == 0) {
			System.out.print(25*(treeIndex / (trees.size()/4)) + "%...");
		}
	}
	
	// finishes this individual's simulation
	private void finishInd() {
		time.stop();
		
		int currTree = treeIndex;
		while (currTree == treeIndex) {
			tick();
		}
		
		time.start();
	}
	
	private void tick() {
		tickNum++; // advance the tick number
		
		// check for this being the last tick for this individual
		if (tickNum == maxTick) {
			nextInd();
		}
		
		if (Math.random() < 0.10) { // 10% chance of adding a new sunspeck
			sun.add(new SunSpeck((int)(Math.random()*this.getWidth()), 0));
		}
		
		HashSet<SunSpeck> rem = new HashSet<SunSpeck>();
		for (SunSpeck s : sun) { // for each sunspeck
			s.tick(); // tick each sunspeck
			if (s.getYPos() > groundLevel) { // check if sunspeck has hit ground
				rem.add(s);
			}
		}
		sun.removeAll(rem); // remove all specks that have hit the ground
		
		trees.get(treeIndex).tick(sun); // tick the tree
	}

	public void paintComponent(Graphics g) {
		g.setColor(new Color(146, 184, 244));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(new Color(183, 85, 23));
		g.fillRect(0, groundLevel, this.getWidth(), this.getHeight()-groundLevel);
		
		trees.get(treeIndex).draw(g);
		
		for (SunSpeck s : sun) {
			s.draw(g);
		}
		
		int fh = 15; // fontHeight
		int ln = 1; // lineNum
		g.setColor(Color.WHITE);
		g.drawString("Tick: " + tickNum + " of " + maxTick, 0, fh*ln++);
		g.drawString("Tick Speed: " + tickSpeed, 0, fh*ln++);
		g.drawString("Pause simulation: P", 0, fh*ln++);
		ln++;
		g.drawString("Individual number: " + (treeIndex+1), 0, fh*ln++);
		g.drawString("This individual's sunlight: " + trees.get(treeIndex).getSunlight(), 0, fh*ln++);
		g.drawString("This individual's nutrients: " + trees.get(treeIndex).getNutrients(), 0, fh*ln++);
		g.drawString("This individual's fitness: " + trees.get(treeIndex).getFitness(), 0, fh*ln++);
		g.drawString("Number of this individual's nodes: " + trees.get(treeIndex).getNumNodes(), 0, fh*ln++);
		g.drawString("This individual created in generation #: " + trees.get(treeIndex).getOrigin(), 0, fh*ln++);
		g.drawString("This individual's mutational displacement from gen0: " + trees.get(treeIndex).getAge(), 0, fh*ln++);
		g.drawString("Skip this individual: I", 0, fh*ln++);
		ln++;
		g.drawString("Generation number: " + generation, 0, fh*ln++);
		g.drawString("Skip this generation: O", 0, fh*ln++);
		g.drawString("Skip ten generations: B", 0, fh*ln++);
		g.drawString("Skip fifty generations: N", 0, fh*ln++);
		g.drawString("Skip one hundred generations: M", 0, fh*ln++);
		ln++;
		g.drawString("Save current generation: F1", 0, fh*ln++);
		g.drawString("Load a saved generation: F2", 0, fh*ln++);
		g.drawString("Run continuously, saving every 10 generations: R", 0, fh*ln++);
	}
	
	public int getGroundLevel() {
		return groundLevel;
	}
	
	public int getCurrGen() {
		return this.generation;
	}
	
	public void stopTime() {
		time.stop();
	}
	
	public void startTime() {
		time.start();
	}
	
	public ArrayList<GeneTree> getTrees() {
		return trees;
	}
	
	public GeneTree getTreeIndex(int i) {
		return trees.get(i);
	}
}