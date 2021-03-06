package simulation;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashSet;

import framework.GeneTrees;

public class GeneTree implements Comparable<GeneTree> {
	private long fitness = 0;
	private long nutrients = 0;
	private long sunlight = 0;
	private boolean done = false;
	
	private TreeNode root;
	private HashSet<TreeNode> nodes;
	private int age; // the number of mutations this tree is from generation 0
	private int origin; // the generation this individual was created in
	
	// creates a new genetree with one root and one other node
	public GeneTree() {
		root = new TreeNode(this, null);
		root.initLocation();
		
		nodes = root.getNodes();
		
		age = 0;
		origin = 0;
	}
	
	// creates a new, blank genetree with a newly-assembled node structure rooted at the given node
	// for I/O purposes
	public GeneTree(TreeNode n) {
		root = n;
		root.initLocation();
		
		nodes = root.getNodes();
	}
	
	// create a new genetree as a child of the given one
	public GeneTree(GeneTree t) {
		root = new TreeNode(t.getRoot(), null, this);
		root.mutate();
		root.initLocation();
		
		nodes = root.getNodes();
		
		age = t.getAge() + 1;
		origin = GeneTrees.panel.getCurrGen();
	}

	public void draw(Graphics g) {
		for (TreeNode n : nodes) {
			// if this isn't the root node, draw its branch to its parent
			if (n.getParent() != null) {
				g.setColor(Color.BLACK);
				g.drawLine(n.getXPos(), n.getYPos(), n.getParent().getXPos(), n.getParent().getYPos());
			}
			
			switch (n.getType()) {
			case 0:
				g.setColor(Color.BLACK);
				break;
			case 1:
				g.setColor(new Color(52, 237, 52));
				break;
			case 2:
				g.setColor(new Color(137, 47, 4));
				break;
			case 3:
				g.setColor(Color.BLUE.brighter().brighter());
				break;
			default:
				throw new IllegalArgumentException("invalid node type");
			}
			
			int xTL = n.getXPos() - n.getSize()/2;
			int yTL = n.getYPos() - n.getSize()/2;
			g.fillOval(xTL, yTL, n.getSize(), n.getSize());
			
			// if debug mode, draw bounding boxes
			if (GeneTrees.debug) {
				// standard bounding box
				g.setColor(Color.WHITE);
				g.drawRect(xTL, yTL, n.getSize(), n.getSize());
				
				// if the mouse is over this node
				if (GeneTrees.panel.xMouse > xTL &&
					GeneTrees.panel.xMouse < xTL + n.getSize() &&
					GeneTrees.panel.yMouse > yTL &&
					GeneTrees.panel.yMouse < yTL + n.getSize()) {
					g.setColor(Color.BLACK); // highlight it
					g.drawRect(xTL, yTL, n.getSize(), n.getSize());
					
					// highlight its parent
					if (n.getParent() != null) {
						int s = n.getParent().getSize() + 2;
						xTL = n.getParent().getXPos() - s/2;
						yTL = n.getParent().getYPos() - s/2;
						g.setColor(Color.CYAN);
						g.drawRect(xTL, yTL, s, s);
					}
					
					// hightlight its children
					for (TreeNode nc : n.getChildren()) {
						int s = nc.getSize() + 2;
						xTL = nc.getXPos() - s/2;
						yTL = nc.getYPos() - s/2;
						g.setColor(Color.MAGENTA);
						g.drawRect(xTL, yTL, s, s);
					}
				}
			}
		}
	}
	
	// checks if any sun spots are intersecting any leaves
	public void tick(HashSet<SunSpeck> sun) { // passed the set of sunspecks
		for (TreeNode n : nodes) {
			HashSet<SunSpeck> rem = new HashSet<SunSpeck>();
			for (SunSpeck ss : sun) {
				int sx = ss.getXPos();
				int sy = ss.getYPos();
				int nx = n.getXPos();
				int ny = n.getYPos();
				int nd = n.getSize()/2;
				
				// if the sunspeck hits this node, remove it
				if ((nx-sx)*(nx-sx) + (ny-sy)*(ny-sy) < nd*nd) {
					rem.add(ss);
					
					// if this node is a leaf, increment its fitness
					if (n.getType() == 1) {
						sunlight += ss.getPower();
					}
				}
			}
			sun.removeAll(rem);
			
			// if this is a root node, gradually increment its fitness
			if (n.getType() == 2 && n.getYPos() > Simulation.getGroundLevel()) {
				nutrients += 2*n.getSize();
			}
			
			// decrement fitness proportional to the size of this node
			fitness -= n.getSize()/2;
		}
		
		// calculate how much fitness has been gained this tick
		long newFitness = Math.min(sunlight, nutrients);
		sunlight -= newFitness;
		nutrients -= newFitness;
		fitness += newFitness;
	}
	
	/*
	 * getters and setters
	 */
	public long getFitness() {
		return fitness;
	}
	
	public TreeNode getRoot() {
		return root;
	}
	
	/*
	 * debugging
	 */
	@SuppressWarnings("unused")
	private void printTree() {
		for (TreeNode n : nodes) {
			System.out.println("x: " + n.getXPos());
			System.out.println("y: " + n.getYPos());
			System.out.println(" ");
		}
	}

	public int compareTo(GeneTree o) {
		if (this.fitness - o.fitness > 1) {
			return 1;
		} else if (this.fitness - o.fitness == 0) {
			return 0;
		} else {
			return -1;
		}
	}
	
	public void resetFitness() {
		sunlight = 0;
		nutrients = 0;
		fitness = 0;
		done = false;
	}
	
	public void mutate() {
		root.mutate();
	}
	
	public int getNumNodes() {
		return nodes.size();
	}
	
	public ArrayList<TreeNode> getAllNodes() {
		return new ArrayList<TreeNode>(nodes);
	}
	
	public int getAge() {
		return age;
	}
	
	public int getOrigin() {
		return origin;
	}
	
	public void setAge(int a) {
		this.age = a;
	}
	
	public void setOrigin(int o) {
		this.origin = o;
	}
	
	public long getNutrients() {
		return nutrients;
	}
	
	public long getSunlight() {
		return sunlight;
	}
	
	public boolean isDone() {
		return done;
	}

	public void setDone(boolean b) {
		done = b;
	}
}