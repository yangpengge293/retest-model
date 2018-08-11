package de.retest.ui.descriptors;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OutlineAttribute extends Attribute {

	private static final long serialVersionUID = 1L;

	// This is a max value used for normalization
	// Actual value doesn't matter as long as:
	// 1) it is the same for all comparisons (doesn't change)
	// 2) result of normalization is always <= 1
	public static final int MAX_HEIGHT = 20000;
	public static final int MAX_WIDHT = 2 * MAX_HEIGHT;

	@XmlElement
	private final int x;
	@XmlElement
	private final int y;
	@XmlElement
	private final int height;
	@XmlElement
	private final int width;

	// Used by JaxB
	@SuppressWarnings( "unused" )
	private OutlineAttribute() {
		x = -1;
		y = -1;
		height = -1;
		width = -1;
	}

	/**
	 * Position relative to the overall window
	 */
	public OutlineAttribute( final Rectangle outline ) {
		super( "outline" );
		x = outline == null ? -1 : outline.x;
		y = outline == null ? -1 : outline.y;
		height = outline == null ? -1 : outline.height;
		width = outline == null ? -1 : outline.width;
	}

	@Override
	public Rectangle getValue() {
		if ( x == -1 && y == -1 && height == -1 && width == -1 ) {
			return null;
		}
		return new Rectangle( x, y, width, height );
	}

	@Override
	public double getWeight() {
		return NORMAL_WEIGHT;
	}

	@Override
	public double match( final Attribute other ) {
		if ( !(other instanceof OutlineAttribute) ) {
			return NO_MATCH;
		}
		final OutlineAttribute outline = (OutlineAttribute) other;
		double result = 0.0;
		result += match( x, outline.x, MAX_WIDHT ) / 4;
		result += match( y, outline.y, MAX_HEIGHT ) / 4;
		result += match( width, outline.width, MAX_WIDHT ) / 4;
		result += match( height, outline.height, MAX_HEIGHT ) / 4;
		assert result >= 0.0;
		assert result <= 1.0;
		return result;
	}

	private double match( final int value1, final int value2, final int maxDiff ) {
		if ( value1 == value2 ) {
			return FULL_MATCH;
		}
		return (double) Math.abs( value1 - value2 ) / maxDiff;
	}

	public static Rectangle parse( final String value ) {
		if ( value == null || value.isEmpty() ) {
			return null;
		}
		try {
			final Pattern pattern = Pattern.compile(
					"java\\.awt\\.Rectangle\\[x=([\\- 0-9]+),y=([\\- 0-9]+),width=([\\- 0-9]+),height=([\\- 0-9]+)\\]" );
			final Matcher matcher = pattern.matcher( value );
			if ( !matcher.matches() || matcher.groupCount() != 4 ) {
				throw new IllegalArgumentException(
						"The given input '" + value + "' does not denote a valid rectangle." );
			}
			final int x = Integer.valueOf( matcher.group( 1 ) );
			final int y = Integer.valueOf( matcher.group( 2 ) );
			final int width = Integer.valueOf( matcher.group( 3 ) );
			final int height = Integer.valueOf( matcher.group( 4 ) );
			return new Rectangle( x, y, width, height );
		} catch ( final Exception e ) {
			throw new RuntimeException( "Exception parsing '" + value + "' to a valid rectangle.", e );
		}
	}

	@Override
	public Attribute applyChanges( final Serializable actual ) {
		return new OutlineAttribute( (Rectangle) actual );
	}
}