/*
 * Copyright (c) 2018 Christopher Urffer
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package framework;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GeneTrees implements Runnable {
	public static final String ver = "0.5.1";
	public static JFrame frame;
	public static GeneTreesPanel panel;
	public static boolean debug = false;
	
	public static boolean ticking = true;
	public static int tickSpeed = 2;
	
	private static int numThreads;
	
	public static Timer time = new Timer(tickSpeed, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	if (ticking) {
        		panel.tick();
        	}
            panel.repaint();
        }
	});

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new GeneTrees());
		
		numThreads = Integer.parseInt(args[0]);
		
		System.out.println("starting GeneTrees v" + ver + " with " + numThreads + " threads");
	}

	@Override
	public void run() {
		frame = new JFrame("GeneTrees " + ver);
		
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setSize(800, 600);
		frame.setUndecorated(false);
		frame.setVisible(true);
		
		panel = new GeneTreesPanel(800, 600, numThreads);
		frame.add(panel);
		panel.init();
		time.start();
	}
}