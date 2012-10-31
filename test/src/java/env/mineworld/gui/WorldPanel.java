package env.mineworld.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import env.mineworld.WorldModel;
import env.mineworld.tile.Terrain;
import env.mineworld.tile.TerrainManager;

public class WorldPanel extends JPanel{

	private static final long serialVersionUID = -6024967304383090591L;
	private WorldModel model;
	private BufferedImage alphaMask;
	private BufferedImage [] terrains;
	private BufferedImage [] terrainsScaled;

	
	private Point lastTileClicked;
	
	private boolean loaded = false;

	//Zoom level, 1.0 is the natural size of the map(fullX * fullY)
	double viewPortZoom = 1.0;
	private boolean zoomChanged = true;

	//offset at which to start drawing the map, give in sizes of the panel 1.0 being 1 full panel
	double viewPortX = 0.0;
	double viewPortY = 0.0;

	//absolute offset at which to start drawing the map 
	double viewPortOffsetX = 0;
	double viewPortOffsetY = 0;


	//percent/100 of the width to start drawing the next cell
	private double wToHratio;


	//size of 1 cell at the current zoom level
	private double xSize;
	private double ySize;

	//offset of the odd cells at the current zoom level
	private double xOffset;
	private double yOffset;


	//number of cells 
	private int worldWidth;
	private int worldHeight;


	//complete size of unscaled map
	private double fullY;
	private double fullX;


	//complete size of zoomed map
	private double xZoomedSize; 
	private double yZoomedSize;


	private String mapfolderURI = "resources/gui/map/";

	public WorldPanel(WorldModel wm){
		super();
		model = wm;

		worldWidth = model.getGridWorld().getWidth();
		worldHeight = model.getGridWorld().getHeight();
		wToHratio = model.getGridWorld().getWidthToHeightRatio();
		
		this.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(e.getWheelRotation()<0){
					zoomIn(e.getPoint());
				}else{
					zoomOut(e.getPoint());
				}

				repaint();
			}
		});

		this.addMouseMotionListener(new MouseMotionListener() {
			private int lastX;
			private int lastY;

			public void mouseMoved(MouseEvent e) {
				lastX = e.getX();
				lastY = e.getY();
			}

			public void mouseDragged(MouseEvent e) {
				int difX = e.getX() - lastX;
				int difY = e.getY() - lastY;

				viewPortX += (difX*1.0)/getWidth();
				viewPortY += (difY*1.0)/getHeight();

				lastX = e.getX();
				lastY = e.getY();
				repaint();
			}
		});

		this.addMouseListener(new MouseListener() {

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseClicked(MouseEvent e) {
				Point p  = getSelectedTile(e.getPoint());
				lastTileClicked = p;
				if(p!=null){
					
					System.out.println("Clicked on tile: ("+p.x+", "+p.y+")");
					
					
				}
				repaint();
			}
		});
	}

	public void zoomIn(Point point){
		Point2D.Double tmp = getPointOnMap(point.x, point.y);

		viewPortZoom *= 1.1;
	
		if(viewPortZoom>2){
			viewPortZoom = 2;
		}
		zoomChanged = true;

		//System.out.println("Centering: "+tmp.x+", "+tmp.y+" on "+point.getX()/getWidth()+", "+point.getY()/getHeight());
		setMapPostoScreenPos(tmp.x,tmp.y,point.getX()/getWidth(),point.getY()/getHeight());
		repaint();
	}

	public void zoomOut(Point point){
		Point2D.Double tmp = getPointOnMap(point.x, point.y);

		viewPortZoom *= 0.9;
		zoomChanged = true;

		//System.out.println("Centering: "+tmp.x+", "+tmp.y+" on "+point.getX()/getWidth()+", "+point.getY()/getHeight());
		setMapPostoScreenPos(tmp.x,tmp.y,point.getX()/getWidth(),point.getY()/getHeight());
		repaint();
	}

	public void centerOn(double x, double y){
		setMapPostoScreenPos(x, y, 0.5, 0.5);
	}

	public void setMapPostoScreenPos(double mapX, double mapY, double screenX, double screenY){
		updateViewPort();

		double mapPosX = mapX * -xZoomedSize;
		double mapPosY = mapY * -yZoomedSize;

		mapPosX += screenX * getWidth();
		mapPosY += screenY * getHeight();

		viewPortX = mapPosX / getWidth();
		viewPortY = mapPosY / getHeight();

		repaint(); 
	}

	public Point2D.Double getPointOnMap(int x, int y){
		updateViewPort();
		Point2D.Double pnt = new Point2D.Double();

		double xx = x+viewPortOffsetX;
		double yy = y+viewPortOffsetY;
		pnt.setLocation(xx/xZoomedSize,yy/yZoomedSize);
		return pnt;
	}

	public boolean loadResources(){
		String terrainFolderURI = mapfolderURI + "terrain/"; 
		TerrainManager tm = TerrainManager.getInstance();
		Terrain [] terrainArray = tm.getTerrainArray();

		terrains = new BufferedImage[terrainArray.length];
		terrainsScaled = new BufferedImage[terrainArray.length];


		try {
			alphaMask = ImageIO.read(new File(terrainFolderURI+"mask2.png"));

			//double wth = (alphaMask.getHeight()*1.0)/(alphaMask.getWidth()*1.0);
			//wth = 1.0 - ((1-wth)*2);
			//wToHratio = wth;

			Image tmpMask = transformGrayToTransparency(alphaMask);
			File tmp = null; //Used to verify a terrain type exists else assign the default one.
			BufferedImage tempImage;
			String [] supportedFileFormats = ImageIO.getReaderFileSuffixes();

			for(int i=0; i< terrainArray.length;i++){
				for(String fileType: supportedFileFormats){
					tmp = new File(terrainFolderURI+(terrainArray[i].getName().toLowerCase().replaceAll("\\ ", "\\\\ "))+"."+fileType);
					if(tmp.exists()){
						break;
					}
				}
				if(tmp == null || !tmp.exists()){
					tmp = new File(terrainFolderURI+"unknown.png");
				}

				tempImage = ImageIO.read(tmp);

				//System.out.println("TempImage size:"+ tempImage.getWidth()+"x"+tempImage.getHeight());
				if(tempImage.getWidth()<alphaMask.getWidth() || tempImage.getHeight()<alphaMask.getHeight() ){
					//If the image is too small scale it
					//System.out.println("Too small");
					if(tempImage.getWidth()<alphaMask.getWidth()){//if the width is too small
						tempImage = getScaledInstance(tempImage, alphaMask.getWidth(), -1, true);
					}else{
						tempImage = getScaledInstance(tempImage, -1, alphaMask.getHeight(), true);
					}
				} else if((tempImage.getWidth()>alphaMask.getWidth()) && (tempImage.getHeight()>alphaMask.getHeight())){
					//System.out.println("Too big");
					//if the image is too large scale it down
					if(((tempImage.getWidth()-alphaMask.getWidth())/(1.0*alphaMask.getWidth())) < ((tempImage.getHeight()-alphaMask.getHeight())/(1.0*alphaMask.getHeight()))){
						tempImage = getScaledInstance(tempImage, alphaMask.getWidth(), -1, true);
					}else{
						tempImage = getScaledInstance(tempImage, -1, alphaMask.getHeight(), true);
					}
				}

				terrains[i] =  applyTransparency(
						tempImage.getSubimage(0, 0, alphaMask.getWidth()-1, alphaMask.getHeight()-1), 
						tmpMask);
				terrainsScaled[i] = terrains[i];
			}

			fullY = terrains[0].getHeight() * worldHeight;
			fullX = terrains[0].getWidth() * worldWidth * wToHratio;

			System.out.println("world Size : " + fullX+"x"+fullY);

			loaded = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}

		return loaded;
	}

	public void setCustomSize(int x, int y){
		this.setPreferredSize(new Dimension(x, y));
		this.setMinimumSize(new Dimension(x, y));
	}

	public void updateViewPort(){
		xSize = terrains[0].getWidth() * viewPortZoom;
		ySize = terrains[0].getHeight() * viewPortZoom;

		yOffset = ySize/2;
		xOffset = xSize*wToHratio;

		yZoomedSize = (ySize * worldHeight)+yOffset;
		xZoomedSize = (xSize * worldWidth * wToHratio)+((1-wToHratio)*xSize);

		viewPortOffsetX = -1*viewPortX*getWidth();
		viewPortOffsetY = -1*viewPortY*getHeight();

		if(zoomChanged){
			if(viewPortZoom>1.0){
				for(int i=0;i<terrains.length;i++){
					terrainsScaled[i] = terrains[i];
				}
			}else{
				for(int i=0;i<terrains.length;i++){
					terrainsScaled[i] = getScaledInstance(terrains[i], (int)Math.round(terrains[i].getWidth()*viewPortZoom), (int)Math.round(terrains[i].getHeight()*viewPortZoom), true);
				}
			}
			zoomChanged = false;
		}

	}

	private void paintMap(Graphics2D g2){
		if(loaded){
			g2.setColor(Color.DARK_GRAY);
			g2.fillRect(0, 0, getWidth(), getHeight());

			updateViewPort();

			//System.out.println("Drawing from:" + (-1.0 * viewPortOffsetX )+", "+ (-1.0 * viewPortOffsetY )+"\n To: "+
			//		(xZoomedSize+(-1.0 * viewPortOffsetX ))+", "+ (yZoomedSize+(-1.0 * viewPortOffsetY )));

			double y1;
			double y2;

			double x1;
			double x2;

			short index;


			int y = (int)Math.ceil((viewPortOffsetY/ySize)-1.5);
			if(y<0){
				y=0;
			}
			int upperY = (int)Math.ceil((getHeight()+viewPortOffsetY)/ySize);

			//System.out.println("Starting drawing from:" + y+" till "+upperY);

			for(;y<worldHeight && y<upperY;y++){

				y1 = (y*ySize) - viewPortOffsetY;
				y2 = ((y+1)*ySize) - viewPortOffsetY;

				for(int x=0;x<worldWidth;x+=2){

					x1 = (x*xOffset)-viewPortOffsetX;
					if(x1>getWidth())break;
					x2 = ((x*xOffset)+xSize)-viewPortOffsetX;
					if(x2<0)continue;

					index = model.getGridWorld().getTerrainTypeAt(x, y);
					g2.drawImage(terrainsScaled[index], 
							(int)Math.round(x1),(int)Math.round(y1+yOffset), 
							(int)Math.round(x2),(int)Math.round(y2+yOffset), 
							0, 0, terrainsScaled[index].getWidth(), terrainsScaled[index].getHeight(), null);
				}

				for(int x=1;x<worldWidth;x+=2){

					x1 = (x*xOffset)-viewPortOffsetX;
					if(x1>getWidth())break;
					x2 = ((x*xOffset)+xSize)-viewPortOffsetX;
					if(x2<0)continue;

					index = model.getGridWorld().getTerrainTypeAt(x, y);
					g2.drawImage(terrainsScaled[index], 
							(int)Math.round(x1),(int)Math.round(y1), 
							(int)Math.round(x2),(int)Math.round(y2), 
							0, 0, terrainsScaled[index].getWidth(), terrainsScaled[index].getHeight(), null);
				}

			}

		}

	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		paintMap(g2);
		
		if( lastTileClicked!=null){
			Point p = getViewPortCoordFromMapCoord(model.getGridWorld().getMapCoordFromTile(lastTileClicked));
			
			double size = 40 * viewPortZoom;
			
			g2.setColor(Color.RED);
			g2.fillOval((int)(p.x - (size/2)), (int)(p.y - (size/2)), (int)size, (int)size);
			//g2.drawLine(p.x, 0, p.x, getHeight());
		}
	}

	private Image transformGrayToTransparency(BufferedImage image)
	{
		ImageFilter filter = new RGBImageFilter()
		{
			public final int filterRGB(int x, int y, int rgb)
			{
				return (rgb << 8) & 0xFF000000;
			}
		};

		ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	private BufferedImage applyTransparency(BufferedImage image, Image mask)
	{
		BufferedImage dest = new BufferedImage(
				image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = dest.createGraphics();
		g2.drawImage(image, 0, 0, null);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0F);
		g2.setComposite(ac);
		g2.drawImage(mask, 0, 0, null);
		g2.dispose();
		return dest;
	}

	public BufferedImage getScaledInstance(BufferedImage img,
			int targetWidth,
			int targetHeight,
			boolean higherQuality)
	{
		int type = (img.getTransparency() == Transparency.OPAQUE) ?
				BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage)img;

		if(targetHeight<1 && targetWidth<1){
			targetWidth = img.getWidth();
			targetHeight = img.getHeight();
		}

		if(targetHeight<0){
			targetHeight = (int)Math.ceil(img.getHeight() * ( (targetWidth*1.0)/img.getWidth()));
		}else if(targetWidth<0){
			targetWidth = (int)Math.ceil(img.getWidth() * ( (targetHeight*1.0)/img.getHeight()));
		}


		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		do {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;

		} while (w != targetWidth || h != targetHeight);

		return ret;
	}

	public Point getSelectedTile(Point p){
		Point2D.Double tmp = getPointOnMap(p.x, p.y);
		return model.getGridWorld().getTileFromMapCoord(tmp);
	}


	public Point getViewPortCoordFromMapCoord(Point2D.Double p){
		int x=-1;
		int y=-1;
		
		x = (int) ((p.x * (xZoomedSize - ((1.0-wToHratio)*xSize))) - viewPortOffsetX);
		y = (int) ((p.y * (yZoomedSize)) - viewPortOffsetY);
		
		return new Point(x, y);
	}
	
}
