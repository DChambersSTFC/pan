/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quattor.pan.dml.functions;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;
import org.quattor.pan.dml.data.ListResource;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.dml.operators.ListVariable;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.InvalidTermException;
import static org.quattor.pan.utils.MessageUtils.MSG_ONE_OR_MORE_ARG_REQ;

/**
 *
 * @author sci28761
 */
final public class Push extends BuiltInFunction {

    private Push(SourceRange sourceRange, Operation... operations)
            throws SyntaxException {
        super("push", sourceRange, operations);
    }

    public static Operation getInstance(SourceRange sourceRange,
            Operation... operations) throws SyntaxException {
        // Ensure that there is at least one argument.
        if (operations.length == 0) {
            throw SyntaxException.create(sourceRange, MSG_ONE_OR_MORE_ARG_REQ,
                    "merge");
        }
        
        // Create the appropriate list of operations. The raw operations cannot
		// be used because the variable must be turned into a ListVariable
		// operation.
		Operation[] modifiedOps = new Operation[2];
			modifiedOps[0] = ListVariable.getInstance(sourceRange, "SELF");
			modifiedOps[1] = operations[0];
                        
		return new Push(sourceRange, modifiedOps);
    }

    @Override
    public Element execute(Context context) {

        // Retrieve the values of the arguments
        Element[] args = calculateArgs(context);
        assert (args.length > 0);
        ListResource result;
        try {
            result = (ListResource) args[0];
        } catch (ClassCastException castException) {
            throw new EvaluationException("Push() must be used on a list.", getSourceRange(), context);
        }
        // Although, if the list is protected, then we'll have to create a copy.
        // This may happen if the argument is a constant or coming from
        // something like a value() call.
        if (result.isProtected()) {
            result = (ListResource) result.writableCopy();
        }
        
        String listType = null;

        for (Element element : args) {
            //Ignore the first element of the arguments array, as that as the original variable
            if (element != args[0]) {
                if (element == args[1]) {
                    listType = element.getTypeAsString();
                    result.append(element);
                } else if (element.getTypeAsString().equals(listType)) {
                    result.append(element);
                } else {
                    throw new EvaluationException("The type of the element did not match the other items in the list.", getSourceRange(), context);
                }
            }
        }
        return result;
    }
}
