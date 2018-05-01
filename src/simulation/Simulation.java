package simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;

public class Simulation extends Thread {
	private HashSet<SunSpeck> sun = new HashSet<SunSpeck>();
	private static int groundLevel = 400;
	
	private int width, height;
	
	private int tickNum = 0;
	private int maxTick = 10000;
	
	private GeneTree tree;
	private final int treeIndex;
	
	public Simulation(GeneTree t, int index, int width, int height) {
		this.tree = t;
		this.treeIndex = index;
		this.width = width;
		this.height = height;
	}
	
	public void run() {
		while (!tree.isDone()) {
			tick();
		}
	}
	
	public void tick() {
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
		
		tree.tick(sun); // tick the tree
		
		// if the tick limit has been reached
		if (tickNum == maxTick) {
			tree.setDone(true);
		}
	}
	
	public void draw(Graphics g) {
		g.setColor(new Color(146, 184, 244));
		g.fillRect(0, 0, width, height);
		g.setColor(new Color(183, 85, 23));
		g.fillRect(0, groundLevel, width, height - groundLevel);
		
		tree.draw(g);
		
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
	
	public boolean isDone() {
		return tree.isDone();
	}
	
	public static int getGroundLevel() {
		return groundLevel;
	}
	
	public int getTreeIndex() {
		return treeIndex;
	}
	
	public GeneTree getTree() {
		return tree;
	}
}