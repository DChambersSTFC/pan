/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quattor.pan.dml.functions;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.Null;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.InvalidTermException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.CompileTimeContext;
import org.quattor.pan.template.Context;
import org.quattor.pan.utils.TermFactory;

/**
 *
 * @author sci28761
 */
public class PushTest extends BuiltInFunctionTestUtils {
    
    @Test
    public void checkGetInstance() {
        checkClassRequirements(Push.class);
    }
    
    @Test (expected = SyntaxException.class)
    public void testNoArguments() throws SyntaxException {
        Push.getInstance(null);
    }
    
    @Test(expected = SyntaxException.class)
    public void testNullValueOneArg() throws SyntaxException {
        Push.getInstance(null, Null.getInstance());
    }
        
    @Test(expected = SyntaxException.class)
    public void testInvalidFirstArg() throws SyntaxException {
        Push.getInstance(null, StringProperty.getInstance("BAD"), StringProperty.getInstance("OK"));
    }
    
    @Test
	public void testPushToList() throws SyntaxException, InvalidTermException {

		Context context = new CompileTimeContext();
		ListResource list = new ListResource();
		Element value = StringProperty.getInstance("OK");
                list.append(value);

		Operation dml = Push.getInstance(null, list, value);

		Element result = context.executeDmlBlock(dml);

		// Check that the list argument has been updated.
		assertTrue(list.size() == 2);
		Element element = list.get(TermFactory.create(0));
		assertTrue(value == element);

		// Verify that the same list is given as the result.
		assertTrue(result == list);
	}

	@Test
	public void testPushToProtectedList() throws SyntaxException,
			InvalidTermException {

		Context context = new CompileTimeContext();

		// Create a protected resource.
		ListResource list = new ListResource();
                Element value = StringProperty.getInstance("OK");
                list.append(value);
		list = (ListResource) list.protect();
                


		Operation dml = Append.getInstance(null, list, value);

		Element result = context.executeDmlBlock(dml);

		// Check that the list argument has NOT been updated.
		assertTrue(list.size() == 0);

		// Verify that a copy has been made.
		assertTrue(result != list);

		// It must also be a list.
		assertTrue(result instanceof ListResource);

		// Verify that the new list has the correct value.
		Element element = ((ListResource) result).get(TermFactory.create(0));
		assertTrue(value == element);
	}
}
