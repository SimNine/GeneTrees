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

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import simulation.GeneTree;
import simulation.Simulation;

@SuppressWarnings("serial")
public class GeneTreesPanel extends JPanel {
	private ArrayList<GeneTree> unsimulatedTrees = new ArrayList<GeneTree>();
	private ArrayList<GeneTree> simulatedTrees = new ArrayList<GeneTree>();
	private int generation = 0;
	private final int genSize = 10000;
	
	private long avgFitness = 0;
	private long minFitness = Long.MAX_VALUE;
	private long maxFitness = Long.MIN_VALUE;
	
	public int xMouse = 0;
	public int yMouse = 0;
	
	private long sysTime;
	
	private Simulation currSim;
	private ArrayList<Simulation> sims = new ArrayList<Simulation>();
	private final int maxSimulations;
	
	public GeneTreesPanel(int width, int height, int maxThreads) {
		super();
		setFocusable(true);
		requestFocusInWindow();
		this.setSize(width, height);
		//Collections.synchronizedCollection(sims);
		
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
	}
	
	public void init() {
		sysTime = System.currentTimeMillis();
		
		// generate trees
		for (int i = 0; i < genSize; i++) {
			unsimulatedTrees.add(new GeneTree());
		}
		
		GeneTrees.time.start();
	}
	
	/*
	 * resets the simulation.
	 * used when loading a generation from file
	 */
	public void reset(ArrayList<GeneTree> trees, int gen) {
		this.unsimulatedTrees = trees;
		this.generation = gen;
		sysTime = System.currentTimeMillis();
		
		simulatedTrees.clear();
		sims.clear();
		currSim = null;
	}
	
	private void continuousGenAndSave() {
		GeneTrees.time.stop();
		
		int saveDelay = 2; // interval of generations by which to save
        String fileName = JOptionPane.showInputDialog(this, "name this generation stream", null);
		System.out.println("computing generations, saving every " + saveDelay + " gens");
		
		while (true) {
			// save a generation every interval
			if (generation % saveDelay == 0) {
				Loader.saveGame(fileName + "_gen" + generation);
			}
			
			simulateGen(); // ad infinitum
		}
	}
	
	private void finishNumGens(int num) {
		GeneTrees.time.stop();
		System.out.println("computing " + num + " generations");
		
		int currGen = generation;
		while (generation < currGen + num) {
			simulateGen();
			tick();
		}
		
		System.out.println("done computing " + num + " generations");
		GeneTrees.time.start();
	}
	
	// finishes this individual's simulation
	private void finishInd() {
		GeneTrees.time.stop();
		
		// if the current simulation isn't done
		if (currSim != null) {
			currSim.run();
			currSim = null;
		}
		
		GeneTrees.time.start();
	}
	
	// generates the next generation of geneTrees
	private void nextGen() {
		long newTime = System.currentTimeMillis();
		long timeDiff = (newTime - sysTime)/1000;
		sysTime = newTime;
		System.out.println("done simulating gen " + generation + " in " + timeDiff + " seconds");
		generation++;
		
		Collections.sort(simulatedTrees); // sort from worst adapted to best adapted
		Collections.reverse(simulatedTrees); // reverse list - it is now from best to worst
		
		if (simulatedTrees.get(0).getFitness() > maxFitness)
			maxFitness = simulatedTrees.get(0).getFitness();
		if (simulatedTrees.get(simulatedTrees.size() - 1).getFitness() < minFitness)
			minFitness = simulatedTrees.get(simulatedTrees.size() - 1).getFitness();
		
		GeneTrees.gPanel.addPoint(0, generation, simulatedTrees.get(0).getFitness());
		GeneTrees.gPanel.addPoint(2, generation, simulatedTrees.get(simulatedTrees.size() - 1).getFitness());
		
		avgFitness = 0;
		int numTrees = simulatedTrees.size();
		for (int i = 0; i < numTrees/2; i++) {
			avgFitness += simulatedTrees.get(i).getFitness();
			simulatedTrees.get(i).resetFitness(); // reset this tree's fitness
			
			// replace one of the lesser half of trees with a mutated copy of one of the better half of trees
			simulatedTrees.set(numTrees/2 + i, new GeneTree(simulatedTrees.get(i)));
		}
		avgFitness /= numTrees;
		GeneTrees.gPanel.addPoint(1, generation, avgFitness);
		GeneTrees.gPanel.setXBounds(-1, generation);
		GeneTrees.gPanel.setYBounds(minFitness, maxFitness);
		GeneTrees.gPanel.repaint();
		
		// copy all trees to unsimulated
		unsimulatedTrees.addAll(simulatedTrees);
		simulatedTrees.clear();
	}
	
	private void simulateGen() {
		finishInd();
		
		int treesPerSim = unsimulatedTrees.size()/maxSimulations - 1;
		
		for (int i = 0; i < maxSimulations; i++) {
			ArrayList<GeneTree> simTrees = new ArrayList<GeneTree>();
			for (int j = 0; j < treesPerSim; j++) {
				simTrees.add(unsimulatedTrees.remove(0));
			}
			sims.add(new Simulation(this.getWidth(), this.getHeight(), simTrees));
		}
		
		if (!unsimulatedTrees.isEmpty()) {
			ArrayList<GeneTree> simTrees = new ArrayList<GeneTree>();
			simTrees.addAll(unsimulatedTrees);
			unsimulatedTrees.clear();
			sims.add(new Simulation(this.getWidth(), this.getHeight(), simTrees));
		}
		
		for (Simulation s : sims) {
			s.start();
		}
		
		for (Simulation s : sims) {
			try {
				s.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		sims.clear();
	}
	
	void tick() {
		if (currSim != null) {
			if (currSim.isDone()) {
				currSim.run();
				currSim = null;
			} else {
				currSim.tick();
			}
		} else {
			if (unsimulatedTrees.isEmpty()) {
				nextGen();
			} else {
				ArrayList<GeneTree> singleton = new ArrayList<GeneTree>();
				singleton.add(unsimulatedTrees.remove(0));
				currSim = new Simulation(this.getWidth(), this.getHeight(), singleton);
			}
		}
	}

	public void paintComponent(Graphics g) {
		if (currSim == null || currSim.getCurrTree() == null) {
			return;
		}
		
		currSim.draw(g);
		
		int fh = 15; // fontHeight
		int ln = 1; // lineNum
		g.setColor(Color.WHITE);
		g.drawString("Tick: " + currSim.getCurrTick() + " of " + currSim.getMaxTick(), 0, fh*ln++);
		g.drawString("Tick Speed: " + GeneTrees.tickSpeed, 0, fh*ln++);
		g.drawString("Pause simulation: P", 0, fh*ln++);
		ln++;
		g.drawString("Individual number: " + (genSize - unsimulatedTrees.size()) + " of " + genSize, 0, fh*ln++);
		g.drawString("This individual's sunlight: " + currSim.getCurrTree().getSunlight(), 0, fh*ln++);
		g.drawString("This individual's nutrients: " + currSim.getCurrTree().getNutrients(), 0, fh*ln++);
		g.drawString("This individual's fitness: " + currSim.getCurrTree().getFitness(), 0, fh*ln++);
		g.drawString("Number of this individual's nodes: " + currSim.getCurrTree().getNumNodes(), 0, fh*ln++);
		g.drawString("This individual created in generation #: " + currSim.getCurrTree().getOrigin(), 0, fh*ln++);
		g.drawString("This individual's mutational displacement from gen0: " + currSim.getCurrTree().getAge(), 0, fh*ln++);
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
		return unsimulatedTrees;
	}
	
	public GeneTree getTreeIndex(int i) {
		return unsimulatedTrees.get(i);
	}
	
	synchronized public void addSimulatedTrees(ArrayDeque<GeneTree> tl) {
		simulatedTrees.addAll(tl);
	}
}