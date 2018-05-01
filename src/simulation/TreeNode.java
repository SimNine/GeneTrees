package simulation;

import java.util.HashSet;

import framework.GeneTrees;

public class TreeNode {
	
	/*
	 * node types:
	 * 0 = structure node
	 * 1 = leaf node
	 * 2 = root node
	 * 3 = raincatcher node (currently disabled)
	 */
	private int type;
	private int size; // diameter in pixels
	private double dist; // distance from parent
	private int angle; // angle (clockwise) from directly below parent
	private int xPos; // xPos of the center
	private int yPos; // yPos of the center
	
	private GeneTree owner;
	private TreeNode parent;
	private HashSet<TreeNode> children = new HashSet<TreeNode>();
	
	// deeply, recursively clones a TreeNode
	public TreeNode(TreeNode tgt, TreeNode parent, GeneTree owner) {
		this.size = tgt.getSize();
		this.type = tgt.getType();
		this.parent = parent;
		this.owner = owner;
		this.dist = tgt.getDist();
		this.angle = tgt.getAngle();

		for (TreeNode n : tgt.getChildren()) {
			children.add(new TreeNode(n, this, owner));
		}
	}
	
	// creates a new TreeNode with given owner and parent node
	public TreeNode(GeneTree t, TreeNode n) {
		this.size = (int)(Math.random()*9.0) + 20;
		this.type = (int)(Math.random()*3.0);
		this.parent = n;
		this.owner = t;
		this.dist = Math.random()*30.0 + 40;
		this.angle = (int)(Math.random()*360.0);
		
		// if this is the root node, it must be a structure node
		if (parent == null) {
			this.type = 0;
		}
		
		// if this happens to become a structure node, there is a %50 chance of getting a child node
		if (type == 0 && Math.random() < 0.5) {
			this.addChild();
		}
	}
	
	// recursively mutates this node and its children
	public void mutate() {
		// 7% chance of mutating node type
		if (Math.random() < 0.07) {
			int newType = (int)(Math.random()*3.0);
			if (this.type == newType) { // if the type wouldn't change
				newType = 4; // set a dead type
			}
			switch (newType) {
			case 0: // change to structure
				this.type = 0;
				if (Math.random() < 0.5) { // there is a 50% chance of gaining a child if this node changes to structure
					this.addChild();
				}
				break;
			case 1: // change to leaf
				this.type = 1;
				children.clear();
				break;
			case 2: // change to root
				this.type = 2;
				children.clear();
				break;
			case 3: // change to raincatcher
				this.type = 3;
				children.clear();
				break;
			case 4: // dummy case
				break;
			default: // if something is wrong
				throw new IllegalStateException("something fucked up");
			}
		}
		
		// 20% chance of mutating node size
		if (Math.random() < 0.20) {
			int sizeInc = (int)(Math.random()*16.0) - 16;
			this.size += sizeInc;
			
			if (this.size < 10) {
				this.size = 10;
			}
		}
		
		HashSet<TreeNode> toDelete = new HashSet<TreeNode>();
		for (TreeNode n : children) {
			// 5% chance to lose each child node
			if (Math.random() < .05) {
				toDelete.add(n);
			}
			
			// mutate each child
			n.mutate();
		}
		children.removeAll(toDelete);
		
		// if this is a structure node, there is a 8% chance of adding a child node
		if (this.type == 0 && Math.random() < 0.08) {
			this.addChild();
		}
		
		if (Math.random() < 0.10) { // 10% chance to mutate angle
			int angleInc = (int)(Math.random()*30) - 30; // by up to +/- 15 degs
			angle += angleInc;
		}
		
		if (Math.random() < 0.10) { // 10% chance of changing this node's distance from parent
			double distInc = Math.random()*30.0 - 30;
			dist += distInc;
			
			// dist must be 10 at minimum
			if (dist < 10) {
				dist = 10;
			}
		}
	}
	
	// recursively computes the location within the simulation of this node, its children, grandchildren, etc
	public void initLocation() {
		// if this node is the root node
		if (this.parent == null) {
			xPos = GeneTrees.panel.getWidth()/2; // PLACEHOLDER
			yPos = GeneTrees.panel.getGroundLevel(); // PLACEHOLDER
			
			// initialize the location of children
			for (TreeNode n : children) {
				n.initLocation();
			}
			return;
		}
		
		// angle correction
		while (angle > 360) {
			angle -= 360;
		}
		while (angle < 0) {
			angle += 360;
		}
		
		// computing the absolute location of this node
		int tempAngle = angle;
		while (tempAngle > 90) {
			tempAngle -= 90;
		}
		double angleInRads = Math.toRadians(tempAngle);
		angleInRads = Math.abs(angleInRads);
		
		// REMEMBER HERE
		// "angle" is the angle from THIS NODE TO ITS PARENT
		// NOT THE OTHER WAY AROUND
		// (hence the wonky sin and cos)
		if (angle == 0) {
			xPos = parent.getXPos();
			yPos = parent.getYPos() - (int)dist;
		} else if (angle < 90) {
			xPos = parent.getXPos() + (int)(Math.sin(angleInRads)*dist);
			yPos = parent.getYPos() - (int)(Math.cos(angleInRads)*dist);
		} else if (angle == 90) {
			xPos = parent.getXPos() + (int)dist;
			yPos = parent.getYPos();
		} else if (angle > 90 && angle < 180) {
			xPos = parent.getXPos() + (int)(Math.cos(angleInRads)*dist);
			yPos = parent.getYPos() + (int)(Math.sin(angleInRads)*dist);
		} else if (angle == 180) {
			xPos = parent.getXPos();
			yPos = parent.getYPos() + (int)dist;
		} else if (angle > 180 && angle < 270) {
			xPos = parent.getXPos() - (int)(Math.sin(angleInRads)*dist);
			yPos = parent.getYPos() + (int)(Math.cos(angleInRads)*dist);
		} else if (angle == 270) {
			xPos = parent.getXPos() - (int)dist;
			yPos = parent.getYPos();
		} else if (angle > 270 && angle < 360) {
			xPos = parent.getXPos() - (int)(Math.cos(angleInRads)*dist);
			yPos = parent.getYPos() - (int)(Math.sin(angleInRads)*dist);
		} else {
			throw new IllegalStateException("illegal angle of: " + angle);
		}
		
		// initialize the location of children
		for (TreeNode n : children) {
			n.initLocation();
		}
	}
	
	// recursively returns this node and all its children, grandchildren, etc
	public HashSet<TreeNode> getNodes() {
		HashSet<TreeNode> ret = new HashSet<TreeNode>();
		
		ret.add(this);
		for (TreeNode t : children) {
			ret.addAll(t.getNodes());
		}
		
		return ret;
	}
	
	// returns just this node's children
	public HashSet<TreeNode> getChildren() {
		return children;
	}
	
	// returns this node's parent node
	public TreeNode getParent() {
		return parent;
	}
	
	// returns this node's GeneTree
	public GeneTree getOwner() {
		return owner;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getType() {
		return type;
	}
	
	public double getDist() {
		return dist;
	}
	
	public int getAngle() {
		return angle;
	}
	
	public int getXPos() {
		return xPos;
	}
	
	public int getYPos() {
		return yPos;
	}
	
	public void addChild() {
		children.add(new TreeNode(owner, this));
	}
	
	public void setParent(TreeNode p) {
		this.parent = p;
	}
}