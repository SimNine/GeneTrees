package framework;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import simulation.GeneTree;
import simulation.Simulation;

@SuppressWarnings("serial")
public class GeneTreesPanel extends JPanel {
	private ArrayList<GeneTree> trees = new ArrayList<GeneTree>();
	private ArrayDeque<Integer> notDoneTrees = new ArrayDeque<Integer>();
	private HashSet<Integer> doneTrees = new HashSet<Integer>();
	private int generation = 0;
	
	public int xMouse = 0;
	public int yMouse = 0;
	
	private long sysTime;
	
	private Simulation currSim;
	private ArrayList<Simulation> sims = new ArrayList<Simulation>();
	private final int maxSimulations;
	private final int simCheckingDelay;
	private boolean isMultithreading = false;
	
	public GeneTreesPanel(int width, int height, int maxThreads, int threadCheckDelay) {
		super();
		setFocusable(true);
		requestFocusInWindow();
		this.setSize(width, height);
		
		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_P:
					GeneTrees.ticking = !GeneTrees.ticking;
					break;
				case KeyEvent.VK_O:
					finishNumGens(1);
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
		
		this.maxSimulations = maxThreads;
		this.simCheckingDelay = threadCheckDelay;
	}
	
	public void init() {
		sysTime = System.currentTimeMillis();
		
		// populate the list of trees and the list of trees not yet simulated
		for (int i = 0; i < 10000; i++) {
			trees.add(new GeneTree());
			notDoneTrees.add(i);
		}
		
		// get one of the trees not yet simulated
		int treeIndex = notDoneTrees.pop();
		currSim = new Simulation(trees.get(treeIndex), treeIndex, this.getWidth(), this.getHeight());
		
		GeneTrees.time.start();
	}
	
	/*
	 * resets the simulation.
	 * used when loading a generation from file
	 */
	public void reset(ArrayList<GeneTree> trees, int gen) {
		this.trees = trees;
		this.generation = gen;
		sysTime = System.currentTimeMillis();
		isMultithreading = false;
		
		notDoneTrees.clear();
		for (int i = 0; i < trees.size(); i++) {
			notDoneTrees.add(i);
		}
		doneTrees.clear();
		
		int treeIndex = notDoneTrees.pop();
		currSim = new Simulation(trees.get(treeIndex), treeIndex, this.getWidth(), this.getHeight());
	}
	
	private void continuousGenAndSave() {
		GeneTrees.time.stop();
		beginMultithreading();
		
		int saveDelay = 2; // interval of generations by which to save
        String fileName = JOptionPane.showInputDialog(this, "name this generation stream", null);
		System.out.println("computing generations, saving every " + saveDelay + " gens");
		
		while (true) {
			// the first tick of each marked generation, save the generation
			if (generation % saveDelay == 0 &&
				doneTrees.isEmpty()) {
				Loader.saveGame(fileName + "_gen" + generation);
			}
			
			tick(); // ad infinitum
		}
	}
	
	private void finishNumGens(int num) {
		GeneTrees.time.stop();
		System.out.println("computing " + num + " generations");
		beginMultithreading();
		
		int currGen = generation;
		while (generation < currGen + num) {
			tick();
			
			// pause between ticks
			try {
				Thread.sleep(simCheckingDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		endMultithreading();
		System.out.println("done computing " + num + " generations");
		GeneTrees.time.start();
	}
	
	// finishes this individual's simulation
	private void finishInd() {
		GeneTrees.time.stop();
		
		// if the current simulation isn't done
		while (!currSim.isDone()) {
			tick();
		}
		
		// check if all trees are done simulating; if so, new gen
		boolean allDone = true;
		for (GeneTree t : trees) {
			if (!t.isDone()) {
				allDone = false;
				break;
			}
		}
		if (allDone) {
			nextGen();
		}
		
		GeneTrees.time.start();
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
		
		// reset the lists of done and not done trees
		for (int i = 0; i < numTrees; i++) {
			notDoneTrees.add(i);
		}
		doneTrees.clear();
	}
	
	// begins multithreading configuration
	private void beginMultithreading() {
		isMultithreading = true;
		currSim.getTree().resetFitness();
		notDoneTrees.add(currSim.getTreeIndex());
	}
	
	// ends multithreading configuration
	private void endMultithreading() {
		isMultithreading = false;
		int treeIndex = notDoneTrees.pop();
		currSim = new Simulation(trees.get(treeIndex), treeIndex, this.getWidth(), this.getHeight());
	}
	
	void tick() {
		if (isMultithreading) {
			// check if any sims are done
			ArrayList<Simulation> simsToRemove = new ArrayList<Simulation>();
			for (Simulation s : sims) { // for each sim
				if (s.isDone()) { // if this sim is done
					doneTrees.add(s.getTreeIndex());
					simsToRemove.add(s);
				}
			}
			sims.removeAll(simsToRemove);
			
			// if more simulations can be started
			while (sims.size() < maxSimulations && !notDoneTrees.isEmpty()) {
				int index = notDoneTrees.pop();
				Simulation newSim = new Simulation(trees.get(index), index, this.getWidth(), this.getHeight());
				newSim.start();
				sims.add(newSim);
			}
			
			// if all the trees are done
			if (doneTrees.size() == trees.size()) {
				nextGen();
			}
			
			if (GeneTrees.debug) {
				System.out.println("num trees done: " + doneTrees.size());
			}
		} else {
			if (currSim.isDone()) {
				// record this tree as being done
				doneTrees.add(currSim.getTreeIndex());
				
				// if all trees are now done
				if (doneTrees.size() == trees.size()) {
					nextGen();
				}
				
				// new simulation with the next tree
				int treeIndex = notDoneTrees.pop();
				currSim = new Simulation(trees.get(treeIndex), treeIndex, this.getWidth(), this.getHeight());
			} else {
				currSim.tick();
			}
		}
	}

	public void paintComponent(Graphics g) {
		if (isMultithreading) {
			// this shouldn't be possible
			throw new IllegalStateException("trying to paint while multithreading");
		}
		
		currSim.draw(g);
		
		int fh = 15; // fontHeight
		int ln = 1; // lineNum
		g.setColor(Color.WHITE);
		g.drawString("Tick: " + currSim.getCurrTick() + " of " + currSim.getMaxTick(), 0, fh*ln++);
		g.drawString("Tick Speed: " + GeneTrees.tickSpeed, 0, fh*ln++);
		g.drawString("Pause simulation: P", 0, fh*ln++);
		ln++;
		g.drawString("Individual number: " + currSim.getTreeIndex() + " of " + trees.size(), 0, fh*ln++);
		g.drawString("This individual's sunlight: " + currSim.getTree().getSunlight(), 0, fh*ln++);
		g.drawString("This individual's nutrients: " + currSim.getTree().getNutrients(), 0, fh*ln++);
		g.drawString("This individual's fitness: " + currSim.getTree().getFitness(), 0, fh*ln++);
		g.drawString("Number of this individual's nodes: " + currSim.getTree().getNumNodes(), 0, fh*ln++);
		g.drawString("This individual created in generation #: " + currSim.getTree().getOrigin(), 0, fh*ln++);
		g.drawString("This individual's mutational displacement from gen0: " + currSim.getTree().getAge(), 0, fh*ln++);
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
	
	public int getCurrGen() {
		return this.generation;
	}
	
	public ArrayList<GeneTree> getTrees() {
		return trees;
	}
	
	public GeneTree getTreeIndex(int i) {
		return trees.get(i);
	}
}