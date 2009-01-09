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

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/data/PersistentElement.java $
 $Id: PersistentElement.java 3506 2008-07-30 18:09:38Z loomis $
 */

package org.quattor.pan.dml.data;

/**
 * Subclasses of this abstract class can appear as valid elements in a generated
 * configuration tree (hence can be made persistent).
 * 
 * @author loomis
 * 
 */
abstract public class PersistentElement extends Element {

	private static final long serialVersionUID = -298195786323539427L;

}