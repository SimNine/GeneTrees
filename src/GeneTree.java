import java.awt.Graphics;

public class GeneTree {
	private int fitness = 0;
	
	private int rootPos;
	private int[] genes = new int[100];
	
	public GeneTree(int x, GeneTree p1, GeneTree p2) {
		rootPos = x;
	}

	public void draw(Graphics g) {
		// TODO Auto-generated method stub
	}
	
	public int[] getGenes() {
		return genes;
	}
	
	public static int[] mutate(GeneTree p1, GeneTree p2) {
		int[] ret = new int[100];
		int[] g1 = p1.getGenes();
		int[] g2 = p2.getGenes();
		
		for (int i = 0; i < 100; i++) {
			if (Math.random() < .5) {
				ret[i] = g1[i];
			} else {
				ret[i] = g2[i];
			}
		}
		
		return ret;
	}

}