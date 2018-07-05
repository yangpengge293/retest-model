package de.retest.ui.image;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.retest.ui.image.Screenshot.ImageType;

public class ImageUtils {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger( ImageUtils.class );

	public static final int MARKING_WIDTH = 2;

	public static final String MAX_SCREENSHOT_HEIGHT_PROP = "de.retest.screenshot.max.height";
	public static final int MAX_SCREENSHOT_HEIGHT_DEFAULT = 1200 * 2;
	public static final String MAX_SCREENSHOT_WIDTH_PROP = "de.retest.screenshot.max.width";
	public static final int MAX_SCREENSHOT_WIDTH_DEFAULT = 1800 * 2;
	private static final int maxScreenshotHeight =
			Integer.getInteger( MAX_SCREENSHOT_HEIGHT_PROP, MAX_SCREENSHOT_HEIGHT_DEFAULT );
	private static final int maxScreenshotWidth =
			Integer.getInteger( MAX_SCREENSHOT_WIDTH_PROP, MAX_SCREENSHOT_WIDTH_DEFAULT );

	public static BufferedImage screenshot2Image( final Screenshot input ) {
		if ( input == null || input.getBinaryData() == null ) {
			return null;
		}
		try {
			final InputStream in = new ByteArrayInputStream( input.getBinaryData() );
			return ImageIO.read( in );
		} catch ( final IOException exc ) {
			throw new RuntimeException( exc );
		}
	}

	public static Screenshot image2Screenshot( final String prefix, final BufferedImage image ) {
		if ( image == null ) {
			return null;
		}
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write( image, "png", baos );
			return new Screenshot( prefix, baos.toByteArray(), ImageType.PNG );
		} catch ( final IOException exc ) {
			throw new RuntimeException( exc );
		}
	}

	public static Screenshot mark( final Screenshot image, final Rectangle mark ) {
		if ( image == null ) {
			return null;
		}
		return image2Screenshot( image.getPersistenceIdPrefix(), mark( screenshot2Image( image ), mark ) );
	}

	public static BufferedImage mark( final BufferedImage image, final Rectangle mark ) {
		return mark( image, Collections.singletonList( mark ) );
	}

	public static BufferedImage mark( final BufferedImage image, final List<Rectangle> marks ) {
		if ( image == null ) {
			return null;
		}
		BufferedImage result = image;
		int imageOffsetX = 0;
		int imageOffsetY = 0;
		for ( final Rectangle mark : marks ) {
			if ( mark == null || mark.height == -1 || mark.width == -1 ) {
				continue;
			}
			// from http://stackoverflow.com/a/2319251/58997
			if ( mark.x > result.getWidth() ) {
				logger.error( "Cannot create mark at x value {} with in image of width {}.", mark.x,
						result.getWidth() );
				continue;
			}
			if ( mark.y > result.getHeight() ) {
				logger.error( "Cannot create mark at y value {} in image of width {}.", mark.y, result.getHeight() );
				continue;
			}
			if ( mark.x + mark.width > result.getWidth() ) {
				logger.debug( "Width of mark is bigger than image of width {}, shortening mark.", result.getWidth() );
				mark.width = result.getWidth() - mark.x;
			}
			if ( mark.y + mark.height > result.getHeight() ) {
				logger.debug( "Height of mark is bigger than image of height {}, shortening mark.",
						result.getHeight() );
				mark.height = result.getHeight() - mark.y;
			}
			mark.x = mark.x + imageOffsetX;
			mark.y = mark.y + imageOffsetY;
			int additionalOffsetX = 0;
			int additionalOffsetY = 0;
			int extendedWidth = 0;
			int extendedHeight = 0;
			if ( mark.x - MARKING_WIDTH < 0 ) {
				final int newImageOffsetX = MARKING_WIDTH - mark.x;
				additionalOffsetX = newImageOffsetX - imageOffsetX;
				imageOffsetX = newImageOffsetX;
				extendedWidth = imageOffsetX;
				mark.x = mark.x + imageOffsetX;
			}
			if ( mark.x + mark.width + MARKING_WIDTH > result.getWidth() + extendedWidth ) {
				extendedWidth += MARKING_WIDTH;
			}
			if ( mark.y - MARKING_WIDTH < 0 ) {
				final int newImageOffsetY = MARKING_WIDTH - mark.y;
				additionalOffsetY = newImageOffsetY - imageOffsetY;
				imageOffsetY = newImageOffsetY;
				extendedHeight = imageOffsetY;
				mark.y = mark.y + imageOffsetY;
			}
			if ( mark.y + mark.height + MARKING_WIDTH > result.getHeight() + extendedHeight ) {
				extendedHeight += MARKING_WIDTH;
			}
			final BufferedImage newResult = new BufferedImage( result.getWidth() + extendedWidth,
					result.getHeight() + extendedHeight, TYPE_INT_ARGB );
			final Graphics g = newResult.getGraphics();
			g.drawImage( result, additionalOffsetX, additionalOffsetY, null );
			g.setColor( Color.RED );
			final int marking = 2 * MARKING_WIDTH;
			g.fillRect( mark.x - MARKING_WIDTH, mark.y - MARKING_WIDTH, MARKING_WIDTH, mark.height + marking );
			g.fillRect( mark.x - MARKING_WIDTH, mark.y - MARKING_WIDTH, mark.width + marking, MARKING_WIDTH );
			g.fillRect( mark.x + mark.width, mark.y - MARKING_WIDTH, MARKING_WIDTH, mark.height + marking );
			g.fillRect( mark.x - MARKING_WIDTH, mark.y + mark.height, mark.width + marking, MARKING_WIDTH );
			g.dispose();
			result = newResult;
		}
		return result;
	}

	public static BufferedImage toBufferedImage( final Image img ) {
		if ( img instanceof BufferedImage ) {
			return (BufferedImage) img;
		}
		final BufferedImage bimage =
				new BufferedImage( img.getWidth( null ), img.getHeight( null ), BufferedImage.TYPE_INT_ARGB );
		final Graphics bGr = bimage.getGraphics();
		bGr.drawImage( img, 0, 0, null );
		bGr.dispose();
		return bimage;
	}

	public static BufferedImage readImage( final File file ) throws IOException {
		return ImageIO.read( file );
	}

	public static BufferedImage readImage( final String path ) throws IOException {
		return readImage( new File( path ) );
	}

	public static Image scaleProportionallyToMaxWidthHeight( final BufferedImage image, final int maxWidth,
			final int maxHeight ) {
		if ( maxWidth >= image.getWidth() && maxHeight >= image.getHeight() ) {
			return image;
		}
		final double heightRatio = (double) Math.min( maxHeight, image.getHeight() ) / (double) image.getHeight();
		final double widthRatio = (double) Math.min( maxWidth, image.getWidth() ) / (double) image.getWidth();
		if ( heightRatio < widthRatio ) {
			final int newHeight = (int) (image.getHeight() * heightRatio);
			final int newWidth = (int) (image.getWidth() * heightRatio);
			return image.getScaledInstance( newWidth, newHeight, Image.SCALE_SMOOTH );
		}
		final int newHeight = (int) (image.getHeight() * widthRatio);
		final int newWidth = (int) (image.getWidth() * widthRatio);
		return image.getScaledInstance( newWidth, newHeight, Image.SCALE_SMOOTH );
	}

	public static Image scaleToSameSize( final BufferedImage img1, final BufferedImage img2 ) {
		final int newWidth = Math.min( img1.getWidth(), img2.getWidth() );
		final int newHeight = Math.min( img1.getHeight(), img2.getHeight() );
		if ( newWidth >= img1.getWidth() && newHeight >= img1.getHeight() ) {
			return img1;
		}
		return img1.getScaledInstance( newWidth, newHeight, Image.SCALE_SMOOTH );
	}

	public static Screenshot cutImage( final Screenshot screenshot, final Rectangle bounds ) {
		if ( screenshot == null ) {
			return null;
		}
		return image2Screenshot( screenshot.getPersistenceIdPrefix(),
				cutImage( screenshot2Image( screenshot ), bounds ) );
	}

	public static BufferedImage cutImage( final BufferedImage image, final Rectangle bounds ) {
		if ( image == null || bounds == null ) {
			return null;
		}
		if ( bounds.x >= image.getWidth() ) {
			logger.error( "X value of image to cut {} is outside of bigger image with width {}!", bounds.x,
					image.getWidth() );
			return null;
		}
		if ( bounds.y >= image.getHeight() ) {
			logger.error( "Y value of image to cut {} is outside of bigger image with height {}!", bounds.y,
					image.getHeight() );
			return null;
		}
		if ( bounds.x + bounds.width > image.getWidth() ) {
			final int newWidth = image.getWidth() - bounds.x;
			logger.debug( "x coordinate '{}' + width '{}' is outside of image of width '{}', setting width to {}.",
					bounds.x, bounds.width, image.getWidth(), newWidth );
			bounds.width = newWidth;
		}
		if ( bounds.y + bounds.height > image.getHeight() ) {
			final int newHeight = image.getHeight() - bounds.y;
			logger.debug( "y coordinate '{}' + height '{}' is outside of image of height '{}', setting height to {}.",
					bounds.y, bounds.height, image.getHeight(), newHeight );
			bounds.height = newHeight;
		}
		try {
			return image.getSubimage( bounds.x, bounds.y, bounds.width, bounds.height );
		} catch ( final Exception exc ) {
			logger.error( "Exception cutting image: ", exc );
		}
		return image;
	}

	public static BufferedImage cutToMax( final BufferedImage image ) {
		if ( image.getHeight() < maxScreenshotHeight && image.getWidth() < maxScreenshotWidth ) {
			return image;
		}
		final int imageHeight = Math.min( image.getHeight(), maxScreenshotHeight );
		final int imageWidth = Math.min( image.getWidth(), maxScreenshotWidth );
		return cutImage( image, new Rectangle( imageWidth, imageHeight ) );
	}

	public static void exportScreenshot( final Screenshot image, final File result ) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream( result );
			fos.write( image.getBinaryData() );
		} finally {
			if ( fos != null ) {
				fos.close();
			}
		}
	}

	public static String removeFileExtension( final String filename ) {
		final String extension = "." + ImageType.PNG;
		if ( filename.toLowerCase().endsWith( extension.toLowerCase() ) ) {
			return filename.substring( 0, filename.length() - extension.length() );
		}
		return filename;
	}

	public static String getTextFromIcon( final Icon icon ) {
		String result = null;
		if ( icon != null ) {
			result = icon.toString();
			// Normalize file:/-URLs to their base name
			if ( result.startsWith( "jar:" ) ) {
				result = result.replace( "jar:", "" );
				result = result.replace( "file:", "" );
				final String[] splitted = result.split( "!" );
				return new File( splitted[0] ).getName() + "!" + splitted[1];
			}
			if ( result.startsWith( "file:" ) ) {
				result = result.replace( "file:", "" );
				result = new File( result ).getName();
			}
			// Normalize OSGI-bundleressources
			if ( result.startsWith( "bundleresource:" ) ) {
				result = result.replaceFirst( "bundleresource:\\/\\/[\\d\\.\\w:]+\\/", "" );
				return result;
			}
			result = new File( result ).getName();
		}
		return result;
	}

	public static BufferedImage toBufferedImage( final Icon icon, final Component component ) {
		final int width = icon.getIconWidth();
		final int height = icon.getIconHeight();
		final BufferedImage bimage = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		final Graphics graphics = bimage.getGraphics();
		icon.paintIcon( component, graphics, 0, 0 );
		graphics.dispose();
		return bimage;
	}

	public static Icon scaleIcon( final Icon icon, final Component component, final int width, final int height ) {
		if ( icon == null ) {
			return null;
		}
		if ( icon.getIconWidth() == width && icon.getIconHeight() == height ) {
			return icon;
		}
		final BufferedImage image = toBufferedImage( icon, component );
		final Image scaled = scaleProportionallyToMaxWidthHeight( image, width, height );
		return new ImageIcon( scaled );
	}

}