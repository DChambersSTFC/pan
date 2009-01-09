/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/CompilerTest.java $
 $Id: CompilerTest.java 1450 2007-03-09 17:17:08Z loomis $
 */

package org.quattor.pan;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.quattor.pan.utils.TestUtils.collectDirectories;
import static org.quattor.pan.utils.TestUtils.collectTests;
import static org.quattor.pan.utils.TestUtils.extractExpectation;
import static org.quattor.pan.utils.TestUtils.extractFormatter;
import static org.quattor.pan.utils.TestUtils.getTestdir;
import static org.quattor.pan.utils.TestUtils.getTmpdir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.tools.ant.BuildException;
import org.junit.Test;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.output.Formatter;
import org.quattor.pan.output.XmlDBFormatter;
import org.xml.sax.InputSource;

public class JavaCompilerTest {

	protected Compiler getDefaultCompiler(File tplfile, File dir,
			Formatter formatter) {
		List<File> path = new LinkedList<File>();
		path.add(dir);
		CompilerOptions options = new CompilerOptions(null, null, true, false,
				100, 50, formatter, getTmpdir(), null, path, 0, false, 2, false, false);
		List<File> tplfiles = new LinkedList<File>();
		tplfiles.add(tplfile);
		return new Compiler(options, new LinkedList<String>(), tplfiles);
	}

	// Tests that an object name that doesn't exist raises an exception.
	@Test(expected = EvaluationException.class)
	public void testErrorOnMissingObjectFile() {

		List<File> path = new LinkedList<File>();
		path.add(getTmpdir());

		CompilerOptions options = new CompilerOptions(null, null, true, false,
				100, 50, XmlDBFormatter.getInstance(), getTmpdir(), null, path,
				0, false, 2, false, false);

		List<String> objects = new LinkedList<String>();
		objects.add("non-existant/object/template");

		new Compiler(options, objects, new LinkedList<File>());
	}

	@Test(expected = EvaluationException.class)
	public void testErrorOnMisplacedObjectFile() throws FileNotFoundException,
			IOException, Throwable {

		File tmpdir = getTmpdir();

		// Write the object template.
		File tpldir = new File(tmpdir, "misplaced");
		if (!tpldir.exists() && !tpldir.mkdirs()) {
			throw new BuildException("cannot create directory: "
					+ tpldir.getAbsolutePath());
		}
		File tplfile = new File(tpldir, "template.tpl");
		String contents = "object template misplaced/template;";
		FileWriter fw = new FileWriter(tplfile);
		fw.write(contents);
		fw.close();

		// Create the path. Should be a path pointing to the WRONG place.
		List<File> path = new LinkedList<File>();
		path.add(tpldir);

		CompilerOptions options = new CompilerOptions(null, null, true, false,
				100, 50, XmlDBFormatter.getInstance(), tmpdir, null, path, 0,
				false, 2, false, false);

		// Create the list of input files.
		List<File> tplfiles = new LinkedList<File>();
		tplfiles.add(tplfile);

		CompilerResults results = Compiler.run(options,
				new LinkedList<String>(), tplfiles);

		Set<Throwable> errors = results.getErrors();
		assertTrue(errors.size() == 1);
		Throwable[] errorArray = errors.toArray(new Throwable[errors.size()]);
		throw errorArray[0];
	}

	@Test
	public void javaFunctionalTests() {

		// Locate the directory with the functionality tests and extract all of
		// the children.
		File root = new File(getTestdir(), "Functionality");
		TreeSet<File> testdirs = collectDirectories(root);

		// Create a list to hold all of the errors.
		List<String> errors = new LinkedList<String>();

		// Loop over all of the children and treat each of those in turn.
		for (File dir : testdirs) {

			TreeSet<File> tests = collectTests(dir);

			for (File t : tests) {

				File tpl = null;
				File rootdir = null;

				// Set the root directory and name of the template. In the case
				// of a directory, the main test template is expected to have
				// the same name as the directory with ".tpl" appended.
				if (t.isFile()) {
					tpl = t;
					rootdir = t.getParentFile();
				} else if (t.isDirectory()) {
					tpl = new File(t, t.getName() + ".tpl");
					rootdir = t;
				}

				// Run an individual test and collect any errors that arise.
				if (tpl != null) {
					if (tpl.exists()) {
						String message = invokeTest(rootdir, tpl);
						if (message != null) {
							errors.add(tpl.getName() + ": " + message);
						}
					} else {
						errors.add(tpl.getName() + ": test does not exist");
					}
				} else {
					errors.add(t.getName()
							+ ": file is not ordinary file or directory");
				}
			}
		}

		// If the error list isn't empty, then fail.
		if (errors.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String message : errors) {
				sb.append(message);
				sb.append("\n");
			}
			fail(sb.toString());
		}

	}

	/**
	 * Invoke a single test by extracting the expected result from the object
	 * template, building the configuration, and ensuring that the expected
	 * result (either an exception or an XPath expression) is returned.
	 * 
	 * @param rootdir
	 *            root directory for build of this object template
	 * @param objtpl
	 *            the object template to compile
	 * 
	 * @return message (String) if there is an error, null otherwise
	 */
	protected String invokeTest(File rootdir, File objtpl) {

		// Create the name of the output XML file from the template name.
		String fname = objtpl.getName();
		fname = fname.substring(0, fname.length() - 4) + ".xml";
		File xml = new File(getTmpdir(), fname);

		// Delete the output XML file, if it exists.
		if (xml.exists()) {
			if (!xml.delete()) {
				throw new BuildException("cannot delete existing XML file: "
						+ xml.getAbsolutePath());
			}
		}

		// Extract what is expected from the template file. This should either
		// be an XPath expression, exception class, or a string that indicates
		// an error.
		Object expectation = extractExpectation(objtpl);

		// Extract the formatter to use.
		Formatter formatter = extractFormatter(objtpl);

		// Compile the given template and collect any errors.
		Compiler compiler = getDefaultCompiler(objtpl, rootdir, formatter);
		Set<Throwable> exceptions = compiler.process().getErrors();

		if (expectation instanceof Class) {

			// Cast to an exception class.
			Class<?> exceptionClass = (Class<?>) expectation;

			// An exception was expected. Ensure that the output XML file does
			// not exist.
			if (xml.exists()) {
				return "expected " + exceptionClass.getSimpleName() + " but "
						+ xml.getName() + " exists";
			}

			// Check that there is at least one exception.
			if (exceptions.size() == 0) {
				return ("expected " + exceptionClass.getSimpleName() + " but no exception was thrown");
			}

			// There shouldn't be more than one exception, but check
			// them all if there is. The type should be the exception that was
			// indicated in the file.
			for (Throwable t : exceptions) {
				try {
					exceptionClass.cast(t);
				} catch (ClassCastException cce) {
					t.printStackTrace();
					return ("expected " + exceptionClass.getSimpleName()
							+ " but got " + t.getClass().getSimpleName());
				}
			}

		} else if (expectation instanceof XPathExpression) {

			// An XPath expression was returned, so the template should have
			// compiled. Ensure that the output file exists and that the XPath
			// expression evaluates to true.
			XPathExpression xp = (XPathExpression) expectation;

			// Ensure that the output file exists.
			if (!xml.exists()) {

				// Check to see if there was an exception thrown. If so,
				// communicate what this exception was.
				if (exceptions.size() > 0) {
					Throwable[] errorArray = exceptions
							.toArray(new Throwable[exceptions.size()]);
					errorArray[0].printStackTrace();
					return ("unexpected exception of type '"
							+ errorArray[0].getClass().getSimpleName() + "' was thrown");
				} else {
					return xml.getName()
							+ " does not exist and no exception was thrown";
				}
			}

			// Now check that the given XPath expression evaluates to true.
			try {

				InputStream is = new FileInputStream(xml);
				InputSource source = new InputSource(is);

				Boolean ok = (Boolean) xp.evaluate(source,
						XPathConstants.BOOLEAN);
				if (!ok.booleanValue()) {
					return "XPath expression evaluated to false";
				}

			} catch (ClassCastException cce) {
				return "XPath expression is not a Boolean";
			} catch (FileNotFoundException fnfe) {
				return "can't find template file: " + objtpl.getName();
			} catch (XPathExpressionException xpee) {
				return xpee.getMessage();
			}

		} else if (expectation instanceof String) {
			return (String) expectation;
		} else {
			return "unexpected exception type "
					+ ((Class<?>) expectation).getSimpleName();
		}

		// Everything's OK, so return null to indicate this.
		return null;
	}

}