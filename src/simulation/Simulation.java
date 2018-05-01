package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;

import framework.GeneTrees;

public class Simulation extends Thread {
	private List<GeneTree> unsimulatedTrees;
	private ArrayDeque<GeneTree> simulatedTrees = new ArrayDeque<GeneTree>();
	private HashSet<SunSpeck> sun = new HashSet<SunSpeck>();
	private static int groundLevel = 400;
	
	private int width, height;
	
	private int tickNum = 0;
	private int maxTick = 10000;
	
	private GeneTree currTree;
	
	public Simulation(int width, int height, List<GeneTree> unsimulatedTrees) {
		this.width = width;
		this.height = height;
		this.unsimulatedTrees = unsimulatedTrees;
	}
	
	public void run() {
		while (!unsimulatedTrees.isEmpty() || currTree != null) {
			tick();
		}
		
		GeneTrees.panel.addSimulatedTrees(simulatedTrees);
	}
	
	public void tick() {		
		if (currTree == null) {
			currTree = unsimulatedTrees.remove(0);
		}
		
		tickNum++; // advance the tick number
		
		if (Math.random() < 0.10) { // 10% chance of adding a new sunspeck
			sun.add(new SunSpeck((int)(Math.random()*width), 0));
		}
		
		HashSet<SunSpeck> rem = new HashSet<SunSpeck>();
		for (SunSpeck s : sun) { // for each sunspeck
			s.tick(); // tick each sunspeck
			if (s.getYPos() > groundLevel) { // check if sunspeck has hit ground
				rem.add(s);
			}
		}
		sun.removeAll(rem); // remove all specks that have hit the ground
		
		currTree.tick(sun); // tick the tree
		
		// if the tick limit has been reached
		if (tickNum == maxTick) {
			currTree.setDone(true);
			simulatedTrees.add(currTree);	
			currTree = null;
			sun.clear();
			tickNum = 0;
		}
	}
	
	public void draw(Graphics g) {
		g.setColor(new Color(146, 184, 244));
		g.fillRect(0, 0, width, height);
		g.setColor(new Color(183, 85, 23));
		g.fillRect(0, groundLevel, width, height - groundLevel);
		
		if (currTree != null) {
			currTree.draw(g);
		}
		
		for (SunSpeck s : sun) {
			s.draw(g);
		}
	}
	
	public int getCurrTick() {
		return tickNum;
	}
	
	public int getMaxTick() {
		return maxTick;
	}
	
	public static int getGroundLevel() {
		return groundLevel;
	}
	
	public GeneTree getCurrTree() {
		return currTree;
	}
	
	public boolean isDone() {
		return (unsimulatedTrees.isEmpty() && currTree == null);
	}
}