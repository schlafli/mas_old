package env.mineworld.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import env.mineworld.WorldModel;

public class WorldView extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7854246456104748560L;
	private WorldModel world;
	private WorldPanel worldPanel;
	
	private boolean loaded = false;

	private int width;
	private int height;

	public WorldView(String name, WorldModel world, int width, int height){
		super(name);
		this.world = world;
		this.width = width;
		this.height = height;
		initComponents();
	}

	public boolean loadAll(){
		loaded = worldPanel.loadResources();		
		return loaded;
	}

	public void initComponents(){
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setSize(width, height);
		this.setPreferredSize(new Dimension(width,height));

	
		worldPanel = new WorldPanel(world);
		worldPanel.setCustomSize(width, height);

		this.add(worldPanel,BorderLayout.CENTER);

		this.pack();
		this.setVisible(true);

		repaint();
	}

}
