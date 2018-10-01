package de.retest.ui.descriptors;

import static de.retest.ui.Path.fromString;
import static de.retest.ui.descriptors.IdentifyingAttributes.create;
import static de.retest.ui.descriptors.IdentifyingAttributes.createList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class DefaultRetestIdProviderTest {

	DefaultRetestIdProvider cut;

	@Before
	public void setUp() {
		cut = new DefaultRetestIdProvider();
	}

	@Test
	public void too_long_text_should_be_cut() {
		// for what you would actually call a text
		final IdentifyingAttributes ident = createIdentAttributes(
				"This is some very long sentence, that could be in a link text, or in some paragraph, and really is to long to be used as id." );
		final String retestId = cut.getRetestId( ident );
		assertThat( retestId.length() ).isLessThan( 20 );

		// and cut should still be unique!
		assertThat( retestId ).isNotEqualTo( cut.getRetestId( ident ) );
	}

	@Test
	public void too_long_words_should_be_cut() {
		// but also for single words
		final IdentifyingAttributes ident = createIdentAttributes( "supercalifragilisticexpialidocious" );
		final String retestId = cut.getRetestId( ident );
		assertThat( retestId.length() ).isLessThan( 20 );

		// and cut should still be unique!
		assertThat( retestId ).isNotEqualTo( cut.getRetestId( ident ) );
	}

	private IdentifyingAttributes createIdentAttributes( final String text ) {
		final Collection<Attribute> attributes = createList( fromString( "/HTML/DIV[1]" ), "DIV" );
		attributes.add( new StringAttribute( "text", text ) );
		return new IdentifyingAttributes( attributes );
	}

	@Test
	public void should_always_be_unique() {
		final IdentifyingAttributes ident = createIdentAttributes( "a" );
		final String retestId = cut.getRetestId( ident );
		assertThat( retestId ).isNotEqualTo( cut.getRetestId( ident ) );
		assertThat( retestId ).isNotEqualTo( cut.getRetestId( ident ) );
		assertThat( retestId ).isNotEqualTo( cut.getRetestId( ident ) );
		assertThat( retestId ).isNotEqualTo( cut.getRetestId( ident ) );
	}

	@Test
	public void works_even_only_for_path_and_type() {
		final IdentifyingAttributes ident = create( fromString( "/HTML/DIV[1]" ), "DIV" );
		final String id1 = cut.getRetestId( ident );
		final String id2 = cut.getRetestId( ident );
		assertThat( id1 ).isNotEqualTo( id2 );
	}

	@Test
	public void no_text_should_give_type() {
		final Collection<Attribute> attributes = createList( fromString( "/HTML/DIV[1]" ), "DIV" );
		attributes.add( new StringAttribute( "type", "DIV" ) );
		attributes.add( new SuffixAttribute( 3 ) );
		assertThat( cut.getRetestId( new IdentifyingAttributes( attributes ) ) ).isEqualTo( "div" );
	}

	@Test( expected = NullPointerException.class )
	public void null_should_give_exception() {
		cut.getRetestId( null );
	}

	@Test( expected = NullPointerException.class )
	public void null_path_should_give_exception() {
		cut.getRetestId( create( null, "DIV" ) );
	}

	@Test
	public void empty_text_should_yield_id_based_on_type() throws Exception {
		final IdentifyingAttributes identifyingAttributes = mock( IdentifyingAttributes.class );
		when( identifyingAttributes.get( "text" ) ).thenReturn( "" );
		when( identifyingAttributes.get( "type" ) ).thenReturn( "SomeType" );
		assertThat( cut.getRetestId( identifyingAttributes ) ).isEqualTo( "sometype" );
	}

	@Test
	public void empty_type_should_yield_id_that_is_at_least_not_empty() throws Exception {
		final IdentifyingAttributes identifyingAttributes = mock( IdentifyingAttributes.class );
		when( identifyingAttributes.get( "text" ) ).thenReturn( "" );
		when( identifyingAttributes.get( "type" ) ).thenReturn( "" );
		assertThat( cut.getRetestId( identifyingAttributes ) ).isNotEmpty();
	}

	@Test
	public void id_should_not_be_empty_when_text_or_type_characters_not_contained_in_id_language() throws Exception {
		final IdentifyingAttributes identifyingAttributes = mock( IdentifyingAttributes.class );
		when( identifyingAttributes.get( "text" ) ).thenReturn( "+" );
		when( identifyingAttributes.get( "type" ) ).thenReturn( "+" );
		final String id = cut.getRetestId( identifyingAttributes );
		assertThat( id ).isNotEmpty();
		assertThat( id ).contains( "component_id" );
	}

	@Test
	public void id_should_have_a_unique_suffix_when_it_is_already_contained_in_knownRetestId() {
		final IdentifyingAttributes identifyingAttributes = mock( IdentifyingAttributes.class );
		when( identifyingAttributes.get( "text" ) ).thenReturn( "+" );
		when( identifyingAttributes.get( "type" ) ).thenReturn( "+" );

		cut.getRetestId( identifyingAttributes );

		final IdentifyingAttributes identifyingAttributes1 = mock( IdentifyingAttributes.class );
		when( identifyingAttributes1.get( "text" ) ).thenReturn( "+" );
		when( identifyingAttributes1.get( "type" ) ).thenReturn( "+" );

		assertThat( cut.getRetestId( identifyingAttributes1 ) ).startsWith( "component_id-" );
	}
}
