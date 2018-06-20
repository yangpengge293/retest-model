package de.retest.ui.descriptors;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SuffixAttribute extends StringAttribute {

	private static final long serialVersionUID = 1L;

	private static final String SUFFIX_KEY = "suffix";

	// Used by JaxB
	@SuppressWarnings( "unused" )
	private SuffixAttribute() {}

	public SuffixAttribute( final String value ) {
		this( value, null );
	}

	public SuffixAttribute( final String value, final String variableName ) {
		super( SUFFIX_KEY, value, variableName );
	}

	@Override
	public boolean isVisible() {
		// Is not visible, because Path is shown
		return false;
	}

	@Override
	public Attribute applyChanges( final Serializable actual ) {
		return new SuffixAttribute( (String) actual, getVariableName() );
	}

	@Override
	public ParameterizedAttribute applyVariableChange( final String variableName ) {
		return new SuffixAttribute( getValue(), variableName );
	}
}