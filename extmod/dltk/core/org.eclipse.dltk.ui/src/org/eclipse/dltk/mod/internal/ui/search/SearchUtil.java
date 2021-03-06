/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.mod.internal.ui.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.mod.core.IModelElement;
import org.eclipse.dltk.mod.core.ISourceModule;
import org.eclipse.dltk.mod.internal.corext.util.Messages;
import org.eclipse.dltk.mod.ui.DLTKUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;


/**
 * This class contains some utility methods for J Search.
 */
public class SearchUtil {

	// LRU working sets
	public static final int LRU_WORKINGSET_LIST_SIZE= 3;
	private static LRUWorkingSetsList fgLRUWorkingSets;

	// Settings store
	private static final String DIALOG_SETTINGS_KEY= "ModelElementSearchActions"; //$NON-NLS-1$
	private static final String STORE_LRU_WORKING_SET_NAMES= "lastUsedWorkingSetNames"; //$NON-NLS-1$
	
	//private static final String BIN_PRIM_CONST_WARN_DIALOG_ID= "BinaryPrimitiveConstantWarningDialog"; //$NON-NLS-1$

	public static boolean isSearchPlugInActivated() {
		return Platform.getBundle("org.eclipse.search").getState() == Bundle.ACTIVE; //$NON-NLS-1$
	}

	
	/**
	 * This helper method with Object as parameter is needed to prevent the loading
	 * of the Search plug-in: the Interpreter verifies the method call and hence loads the
	 * types used in the method signature, eventually triggering the loading of
	 * a plug-in (in this case ISearchQuery results in Search plug-in being loaded).
	 */
	public static void runQueryInBackground(Object query) {
		NewSearchUI.runQueryInBackground((ISearchQuery)query);
	}
	
	/**
	 * This helper method with Object as parameter is needed to prevent the loading
	 * of the Search plug-in: the Interpreter verifies the method call and hence loads the
	 * types used in the method signature, eventually triggering the loading of
	 * a plug-in (in this case ISearchQuery results in Search plug-in being loaded).
	 */
	public static IStatus runQueryInForeground(IRunnableContext context, Object query) {
		return NewSearchUI.runQueryInForeground(context, (ISearchQuery)query);
	}
	
	/**
	 * Returns the compilation unit for the givenscriptelement.
	 * 
	 * @param	element thescriptelement whose compilation unit is searched for
	 * @return	the compilation unit of the givenscriptelement
	 */
	static ISourceModule findSourceModule(IModelElement element) {
		if (element == null)
			return null;
		return (ISourceModule) element.getAncestor(IModelElement.SOURCE_MODULE);
	}


	public static String toString(IWorkingSet[] workingSets) {
		Arrays.sort(workingSets, new WorkingSetComparator());
		String result= ""; //$NON-NLS-1$
		if (workingSets != null && workingSets.length > 0) {
			boolean firstFound= false;
			for (int i= 0; i < workingSets.length; i++) {
				String workingSetLabel= workingSets[i].getLabel();
				if (firstFound)
					result= Messages.format(SearchMessages.SearchUtil_workingSetConcatenation, new String[] {result, workingSetLabel}); 
				else {
					result= workingSetLabel;
					firstFound= true;
				}
			}
		}
		return result;
	}

	// ---------- LRU working set handling ----------

	/**
	 * Updates the LRU list of working sets.
	 * 
	 * @param workingSets	the workings sets to be added to the LRU list
	 */
	public static void updateLRUWorkingSets(IWorkingSet[] workingSets) {
		if (workingSets == null || workingSets.length < 1)
			return;
		
		getLRUWorkingSets().add(workingSets);
		saveState(getDialogStoreSection());
	}

	private static void saveState(IDialogSettings settingsStore) {
		IWorkingSet[] workingSets;
		Iterator iter= fgLRUWorkingSets.iterator();
		int i= 0;
		while (iter.hasNext()) {
			workingSets= (IWorkingSet[])iter.next();
			String[] names= new String[workingSets.length];
			for (int j= 0; j < workingSets.length; j++)
				names[j]= workingSets[j].getName();
			settingsStore.put(STORE_LRU_WORKING_SET_NAMES + i, names);
			i++;
		}
	}

	public static LRUWorkingSetsList getLRUWorkingSets() {
		if (fgLRUWorkingSets == null) {
			restoreState();
		}
		return fgLRUWorkingSets;
	}

	private static void restoreState() {
		fgLRUWorkingSets= new LRUWorkingSetsList(LRU_WORKINGSET_LIST_SIZE);
		IDialogSettings settingsStore= getDialogStoreSection();
		
		boolean foundLRU= false;
		for (int i= LRU_WORKINGSET_LIST_SIZE - 1; i >= 0; i--) {
			String[] lruWorkingSetNames= settingsStore.getArray(STORE_LRU_WORKING_SET_NAMES + i);
			if (lruWorkingSetNames != null) {
				Set workingSets= new HashSet(2);
				for (int j= 0; j < lruWorkingSetNames.length; j++) {
					IWorkingSet workingSet= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(lruWorkingSetNames[j]);
					if (workingSet != null) {
						workingSets.add(workingSet);
					}
				}
				foundLRU= true;
				if (!workingSets.isEmpty())
					fgLRUWorkingSets.add((IWorkingSet[])workingSets.toArray(new IWorkingSet[workingSets.size()]));
			}
		}
		if (!foundLRU)
			// try old preference format
			restoreFromOldFormat();
	}

	private static IDialogSettings getDialogStoreSection() {
		IDialogSettings settingsStore= DLTKUIPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS_KEY);
		if (settingsStore == null)
			settingsStore= DLTKUIPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS_KEY);
		return settingsStore;
	}

	private static void restoreFromOldFormat() {
		fgLRUWorkingSets= new LRUWorkingSetsList(LRU_WORKINGSET_LIST_SIZE);
		IDialogSettings settingsStore= getDialogStoreSection();

		boolean foundLRU= false;
		String[] lruWorkingSetNames= settingsStore.getArray(STORE_LRU_WORKING_SET_NAMES);
		if (lruWorkingSetNames != null) {
			for (int i= lruWorkingSetNames.length - 1; i >= 0; i--) {
				IWorkingSet workingSet= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(lruWorkingSetNames[i]);
				if (workingSet != null) {
					foundLRU= true;
					fgLRUWorkingSets.add(new IWorkingSet[]{workingSet});
				}
			}
		}
		if (foundLRU)
			// save in new format
			saveState(settingsStore);
	}	
}
