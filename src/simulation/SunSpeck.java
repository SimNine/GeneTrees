package simulation;
import java.awt.Color;
import java.awt.Graphics;

import framework.GeneTrees;

public class SunSpeck {
	private int x;
	private int y;
	private long power;
	
	public SunSpeck(int x, int y) {
		this.x = x;
		this.y = y;
		this.power = 100000;
	}

	public void draw(Graphics g) {
		g.setColor(Color.YELLOW);
		g.drawRect(x-1, y-1, 3, 3);
		
		if (GeneTrees.debug) {
			g.drawString("" + power, x, y);
		}
	}
	
	public void tick() {
		y++;
		power -= 200; // power is completely drained by 500 ticks
	}
	
	public int getXPos() {
		return x;
	}
	
	public int getYPos() {
		return y;
	}
	
	public long getPower() {
		return power;
	}
}