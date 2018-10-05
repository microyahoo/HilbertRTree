package com.njupt.utils;

// ZoomWindow.java

import java.awt.*;
import javax.swing.*;

/**
 * a JFrame that presents a given image at some initial magnification which can
 * be stretched and shrunk by the user without changing its aspect ratio.
 * 
 * @author Melinda Green
 */
public class ZoomWindow extends JFrame {
	private float zoomMagnification;

	/**
	 * creates a ZoomWindow which displays given images which are always scaled
	 * to the window size and tracks changes to window size.
	 */
	public ZoomWindow(String title, float zoomMagnification) {
		super(title);
		this.zoomMagnification = zoomMagnification;
	}

	public void setImage(final Image zoomimage) {
		int imageWidth = zoomimage.getWidth(null);
		int imageHeight = zoomimage.getHeight(null);
		final float imageAspectRatio = imageWidth / (float) imageHeight;
		final Dimension minimumImageSize = new Dimension(imageWidth,
				imageHeight);
		// System.out.println("min image size = " + minimumImageSize);
		Component minbox = javax.swing.Box.createRigidArea(minimumImageSize);
		getContentPane().add(minbox); // just for measurement. removed below
		pack(); // just to measure the min window size. true size is set at end.
		Dimension minimumWindowSize = new Dimension(getSize());
		int winXdiff = minimumWindowSize.width - minimumImageSize.width;
		int winYdiff = minimumWindowSize.height - minimumImageSize.height;
		// System.out.println("min window size: " + minimumWindowSize +
		// " diffs: " + winXdiff + "," + winYdiff);
		int scaledImageWidth = Math.round(imageWidth * zoomMagnification);
		int scaledImageHeight = Math.round(imageHeight * zoomMagnification);
		final Dimension preferedImageSize = new Dimension(scaledImageWidth,
				scaledImageHeight);
		final JPanel canvas = new JPanel() {
			public void paint(Graphics g) {
				super.paint(g);
				// get the requested new size
				Dimension canvasSize = getSize();
				Dimension largestImage = new Dimension(canvasSize);
				if (largestImage.width / (float) largestImage.height > imageAspectRatio)
					largestImage.width = (int) Math.ceil(largestImage.height
							* imageAspectRatio);
				else
					largestImage.height = (int) Math.ceil(largestImage.width
							/ imageAspectRatio);
				int xpadding = canvasSize.width - largestImage.width;
				int ypadding = canvasSize.height - largestImage.height;
				g.drawImage(zoomimage, xpadding / 2, ypadding / 2,
						largestImage.width, largestImage.height, null);
			}

			public Dimension getMinimumSize() {
				return minimumImageSize;
			}

			public Dimension getPreferedSize() {
				return preferedImageSize;
			}
		};
		// System.out.println("requested image size " + preferedImageSize);
		getContentPane().removeAll();
		getContentPane().add(canvas);
		Dimension initialWinSize = new Dimension(preferedImageSize.width
				+ winXdiff, preferedImageSize.height + winYdiff);
		setSize(initialWinSize);

		// System.out.println("new window size " + this.getSize());
	}

	/**
	 * a simple example program puts up a ZoomWindow displaying a named image
	 * expected to be found in the classpath.
	 */
	public static void main(String args[]) {
		String image_file_name = args.length == 1 ? args[0] : "d://2.jpg";
		System.out.println(image_file_name);
		Image testImage = new ImageIcon(
				ClassLoader.getSystemResource(image_file_name)).getImage();
		ZoomWindow zoomwin = new ZoomWindow("ZoomWindow Example", 3);
		zoomwin.setImage(testImage);
		zoomwin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		zoomwin.setVisible(true);
	}

} // end class ZoomWindow

