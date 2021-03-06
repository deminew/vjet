/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.mod.ui.text.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dltk.mod.internal.corext.util.Messages;
import org.eclipse.dltk.mod.internal.ui.DLTKUIMessages;
import org.eclipse.dltk.mod.internal.ui.dialogs.OptionalMessageDialog;
import org.eclipse.dltk.mod.ui.DLTKUIPlugin;
import org.eclipse.dltk.mod.ui.PreferenceConstants;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension2;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * A content assist processor that aggregates the proposals of the
 * {@link org.eclipse.dltk.mod.ui.text.java.IScriptCompletionProposalComputer}s
 * contributed via the
 * <code>org.eclipse.dltk.mod.ui.javaCompletionProposalComputer</code> extension
 * point.
 * <p>
 * Subclasses may extend:
 * <ul>
 * <li><code>createContext</code> to provide the context object passed to the
 * computers</li>
 * <li><code>createProgressMonitor</code> to change the way progress is
 * reported</li>
 * <li><code>filterAndSort</code> to add sorting and filtering</li>
 * <li><code>getContextInformationValidator</code> to add context validation
 * (needed if any contexts are provided)</li>
 * <li><code>getErrorMessage</code> to change error reporting</li>
 * </ul>
 * </p>
 * 
 * 
 */
public abstract class ContentAssistProcessor implements IContentAssistProcessor {
	private static final boolean DEBUG = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.dltk.mod.ui/debug/ResultCollector")); //$NON-NLS-1$//$NON-NLS-2$

	/**
	 * Dialog settings key for the "all categories are disabled" warning dialog.
	 * See {@link OptionalMessageDialog}.
	 * 
	 */
	private static final String PREF_WARN_ABOUT_EMPTY_ASSIST_CATEGORY = "EmptyDefaultAssistCategory"; //$NON-NLS-1$

	private final List fCategories;

	private final String fPartition;

	private final ContentAssistant fAssistant;

	private char[] fCompletionAutoActivationCharacters;

	private static final Comparator ORDER_COMPARATOR = new Comparator() {

		public int compare(Object o1, Object o2) {
			CompletionProposalCategory d1 = (CompletionProposalCategory) o1;
			CompletionProposalCategory d2 = (CompletionProposalCategory) o2;

			return d1.getSortOrder() - d2.getSortOrder();
		}

	};

	/* cycling stuff */
	private int fRepetition = -1;

	private List fCategoryIteration = null;

	private String fIterationGesture = null;

	private int fNumberOfComputedResults = 0;

	private String fErrorMessage;

	public ContentAssistProcessor(ContentAssistant assistant, String partition) {
		Assert.isNotNull(partition);
		Assert.isNotNull(assistant);

		fPartition = partition;
		fCategories = CompletionProposalComputerRegistry.getDefault()
				.getProposalCategories();
		fAssistant = assistant;
		fAssistant.addCompletionListener(new ICompletionListener() {

			public void assistSessionStarted(ContentAssistEvent event) {
				if (event.processor != ContentAssistProcessor.this)
					return;

				fIterationGesture = getIterationGesture();
				KeySequence binding = getIterationBinding();

				// this may show the warning dialog if all categories are
				// disabled
				fCategoryIteration = getCategoryIteration();
				for (Iterator it = fCategories.iterator(); it.hasNext();) {
					CompletionProposalCategory cat = (CompletionProposalCategory) it
							.next();
					cat.sessionStarted();
				}

				fRepetition = 0;
				if (event.assistant instanceof IContentAssistantExtension2) {
					IContentAssistantExtension2 extension = (IContentAssistantExtension2) event.assistant;

					if (fCategoryIteration.size() == 1) {
						extension.setRepeatedInvocationMode(false);
						extension.setShowEmptyList(false);
					} else {
						extension.setRepeatedInvocationMode(true);
						extension.setStatusLineVisible(true);
						extension.setStatusMessage(createIterationMessage());
						extension.setShowEmptyList(true);
						if (extension instanceof IContentAssistantExtension3) {
							IContentAssistantExtension3 ext3 = (IContentAssistantExtension3) extension;
							((ContentAssistant) ext3)
									.setRepeatedInvocationTrigger(binding);
						}
					}

				}
			}

			/*
			 * @see org.eclipse.jface.text.contentassist.ICompletionListener#assistSessionEnded(org.eclipse.jface.text.contentassist.ContentAssistEvent)
			 */
			public void assistSessionEnded(ContentAssistEvent event) {
				if (event.processor != ContentAssistProcessor.this)
					return;

				for (Iterator it = fCategories.iterator(); it.hasNext();) {
					CompletionProposalCategory cat = (CompletionProposalCategory) it
							.next();
					cat.sessionEnded();
				}

				fCategoryIteration = null;
				fRepetition = -1;
				fIterationGesture = null;
				if (event.assistant instanceof IContentAssistantExtension2) {
					IContentAssistantExtension2 extension = (IContentAssistantExtension2) event.assistant;
					extension.setShowEmptyList(false);
					extension.setRepeatedInvocationMode(false);
					extension.setStatusLineVisible(false);
					if (extension instanceof IContentAssistantExtension3) {
						IContentAssistantExtension3 ext3 = (IContentAssistantExtension3) extension;
						((ContentAssistant) ext3)
								.setRepeatedInvocationTrigger(KeySequence
										.getInstance());
					}
				}
			}

			public void selectionChanged(ICompletionProposal proposal,
					boolean smartToggle) {
			}

		});
	}

	public final ICompletionProposal[] computeCompletionProposals(
			ITextViewer viewer, int offset) {

		final long startTime = System.currentTimeMillis();

		clearState();

		IProgressMonitor monitor = createProgressMonitor();
		monitor.beginTask(
				ScriptTextMessages.ContentAssistProcessor_computing_proposals,
				fCategories.size() + 1);

		ContentAssistInvocationContext context = createContext(viewer, offset);
		final long setupTime = System.currentTimeMillis();

		monitor
				.subTask(ScriptTextMessages.ContentAssistProcessor_collecting_proposals);
		final List proposals = collectProposals(viewer, offset, monitor, context);
		final long collectTime = System.currentTimeMillis();

		monitor
				.subTask(ScriptTextMessages.ContentAssistProcessor_sorting_proposals);

		final List filtered = filterAndSortProposals(proposals, monitor, context);
		fNumberOfComputedResults = filtered.size();
		
		final long filterTime = System.currentTimeMillis();

		ICompletionProposal[] result = (ICompletionProposal[]) filtered
				.toArray(new ICompletionProposal[filtered.size()]);
		monitor.done();

		if (DEBUG) {
			System.err
					.println("Code Assist stats of " + result.length + " proposals:"); //$NON-NLS-1$ //$NON-NLS-2$
			System.err
					.println("Code Assist (setup):\t" + (setupTime - startTime)); //$NON-NLS-1$
			System.err
					.println("Code Assist (collect):\t" + (collectTime - setupTime)); //$NON-NLS-1$
			System.err
					.println("Code Assist (sort):\t" + (filterTime - collectTime)); //$NON-NLS-1$
		}

		return result;
	}

	private void clearState() {
		fErrorMessage = null;
		fNumberOfComputedResults = 0;
	}

	private List collectProposals(ITextViewer viewer, int offset,
			IProgressMonitor monitor, ContentAssistInvocationContext context) {
		List proposals = new ArrayList();
		List providers = getCategories();
		for (Iterator it = providers.iterator(); it.hasNext();) {
			CompletionProposalCategory cat = (CompletionProposalCategory) it
					.next();
			List computed = cat.computeCompletionProposals(context, fPartition,
					new SubProgressMonitor(monitor, 1));
			proposals.addAll(computed);
			if (fErrorMessage == null) {
				fErrorMessage = cat.getErrorMessage();
			}
		}

		return proposals;
	}

	/**
	 * Filters and sorts the proposals. The passed list may be modified and
	 * returned, or a new list may be created and returned.
	 * 
	 * @param proposals
	 *            the list of collected proposals (element type:
	 *            {@link ICompletionProposal})
	 * @param monitor
	 *            a progress monitor
	 * @param context
	 *            TODO
	 * @return the list of filtered and sorted proposals, ready for display
	 *         (element type: {@link ICompletionProposal})
	 */
	protected List filterAndSortProposals(List proposals,
			IProgressMonitor monitor, ContentAssistInvocationContext context) {
		return proposals;
	}
	
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {		
		clearState();

		IProgressMonitor monitor = createProgressMonitor();
		monitor.beginTask(
				ScriptTextMessages.ContentAssistProcessor_computing_contexts,
				fCategories.size() + 1);

		monitor
				.subTask(ScriptTextMessages.ContentAssistProcessor_collecting_contexts);
		final List proposals = collectContextInformation(viewer, offset, monitor);

		monitor
				.subTask(ScriptTextMessages.ContentAssistProcessor_sorting_contexts);
		List filtered = filterAndSortContextInformation(proposals, monitor);
		fNumberOfComputedResults = filtered.size();

		IContextInformation[] result = (IContextInformation[]) filtered
				.toArray(new IContextInformation[filtered.size()]);
		monitor.done();

		return result;
	}

	private List collectContextInformation(ITextViewer viewer, int offset,
			IProgressMonitor monitor) {
		List proposals = new ArrayList();
		ContentAssistInvocationContext context = createContext(viewer, offset);
		setContextInformationMode(context);

		List providers = getCategories();
		for (Iterator it = providers.iterator(); it.hasNext();) {
			CompletionProposalCategory cat = (CompletionProposalCategory) it
					.next();
			List computed = cat.computeContextInformation(context, fPartition,
					new SubProgressMonitor(monitor, 1));
			proposals.addAll(computed);
			if (fErrorMessage == null) {
				fErrorMessage = cat.getErrorMessage();
			}
		}

		return proposals;
	}

	/**
	 * Filters and sorts the context information objects. The passed list may be
	 * modified and returned, or a new list may be created and returned.
	 * 
	 * @param contexts
	 *            the list of collected proposals (element type:
	 *            {@link IContextInformation})
	 * @param monitor
	 *            a progress monitor
	 * @return the list of filtered and sorted proposals, ready for display
	 *         (element type: {@link IContextInformation})
	 */
	protected List filterAndSortContextInformation(List contexts,
			IProgressMonitor monitor) {
		return contexts;
	}

	/**
	 * Sets this processor's set of characters triggering the activation of the
	 * completion proposal computation.
	 * 
	 * @param activationSet
	 *            the activation set
	 */
	public final void setCompletionProposalAutoActivationCharacters(
			char[] activationSet) {
		fCompletionAutoActivationCharacters = activationSet;
	}

	public final char[] getCompletionProposalAutoActivationCharacters() {
		return fCompletionAutoActivationCharacters;
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public String getErrorMessage() {
		if (fNumberOfComputedResults > 0)
			return null;
		if (fErrorMessage != null)
			return fErrorMessage;
		return DLTKUIMessages.ScriptEditor_codeassist_noCompletions;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/**
	 * Creates a progress monitor.
	 * <p>
	 * The default implementation creates a <code>NullProgressMonitor</code>.
	 * </p>
	 * 
	 * @return a progress monitor
	 */
	protected IProgressMonitor createProgressMonitor() {
		return new NullProgressMonitor();
	}
	
	protected void setContextInformationMode(
			ContentAssistInvocationContext context) {
		// empty
	}

	/**
	 * Creates the context that is passed to the completion proposal computers.
	 * 
	 * @param viewer
	 *            the viewer that content assist is invoked on
	 * @param offset
	 *            the content assist offset
	 * @return the context to be passed to the computers
	 */
	protected ContentAssistInvocationContext createContext(ITextViewer viewer,
			int offset) {
		return new ContentAssistInvocationContext(viewer, offset);
	}

	private List getCategories() {
		if (fCategoryIteration == null) {
			return fCategories;
		}

		int iteration = fRepetition % fCategoryIteration.size();
		fAssistant.setStatusMessage(createIterationMessage());
		fAssistant.setEmptyMessage(createEmptyMessage());
		fRepetition++;

		// fAssistant.setShowMessage(fRepetition % 2 != 0);
		
		return (List) fCategoryIteration.get(iteration);
	}

	private List getCategoryIteration() {
		List sequence = new ArrayList();
		sequence.add(getDefaultCategories());
		for (Iterator it = getSeparateCategories().iterator(); it.hasNext();) {
			CompletionProposalCategory cat = (CompletionProposalCategory) it
					.next();
			sequence.add(Collections.singletonList(cat));
		}
		return sequence;
	}

	private List getDefaultCategories() {
		// default mix - enable all included computers
		List included = getDefaultCategoriesUnchecked();

		if ((IDocument.DEFAULT_CONTENT_TYPE.equals(fPartition))
				&& included.isEmpty() && !fCategories.isEmpty())
			if (informUserAboutEmptyDefaultCategory())
				// preferences were restored - recompute the default categories
				included = getDefaultCategoriesUnchecked();

		return included;
	}

	private List getDefaultCategoriesUnchecked() {
		List included = new ArrayList();
		for (Iterator it = fCategories.iterator(); it.hasNext();) {
			CompletionProposalCategory category = (CompletionProposalCategory) it
					.next();
			if (category.isIncluded() && category.hasComputers(fPartition))
				included.add(category);
		}
		return included;
	}

	/**
	 * Informs the user about the fact that there are no enabled categories in
	 * the default content assist set and shows a link to the preferences.
	 */
	private boolean informUserAboutEmptyDefaultCategory() {
		if (OptionalMessageDialog
				.isDialogEnabled(PREF_WARN_ABOUT_EMPTY_ASSIST_CATEGORY)) {
			final Shell shell = DLTKUIPlugin.getActiveWorkbenchShell();
			String title = ScriptTextMessages.ContentAssistProcessor_all_disabled_title;
			String message = ScriptTextMessages.ContentAssistProcessor_all_disabled_message;
			// see PreferencePage#createControl for the 'defaults' label
			final String restoreButtonLabel = JFaceResources
					.getString("defaults"); //$NON-NLS-1$
			final String linkMessage = Messages
					.format(
							ScriptTextMessages.ContentAssistProcessor_all_disabled_preference_link,
							LegacyActionTools
									.removeMnemonics(restoreButtonLabel));
			final int restoreId = IDialogConstants.CLIENT_ID + 10;
			final OptionalMessageDialog dialog = new OptionalMessageDialog(
					PREF_WARN_ABOUT_EMPTY_ASSIST_CATEGORY, shell, title,
					null /* default image */, message, MessageDialog.WARNING,
					new String[] { restoreButtonLabel,
							IDialogConstants.CLOSE_LABEL }, 1) {
				/*
				 * @see org.eclipse.dltk.mod.internal.ui.dialogs.OptionalMessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
				 */
				protected Control createCustomArea(Composite composite) {
					// wrap link and checkbox in one composite without space
					Composite parent = new Composite(composite, SWT.NONE);
					GridLayout layout = new GridLayout();
					layout.marginHeight = 0;
					layout.marginWidth = 0;
					layout.verticalSpacing = 0;
					parent.setLayout(layout);

					Composite linkComposite = new Composite(parent, SWT.NONE);
					layout = new GridLayout();
					layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
					layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
					layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
					linkComposite.setLayout(layout);

					Link link = new Link(linkComposite, SWT.NONE);
					link.setText(linkMessage);
					link.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							close();
							PreferencesUtil
									.createPreferenceDialogOn(
											shell,
											"org.eclipse.dltk.mod.ui.preferences.CodeAssistPreferenceAdvanced", null, null).open(); //$NON-NLS-1$
						}
					});
					GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING,
							true, false);
					gridData.widthHint = this.getMinimumMessageWidth();
					link.setLayoutData(gridData);

					// create checkbox and "don't show this message" prompt
					super.createCustomArea(parent);

					return parent;
				}

				protected void createButtonsForButtonBar(Composite parent) {
					Button[] buttons = new Button[2];
					buttons[0] = createButton(parent, restoreId,
							restoreButtonLabel, false);
					buttons[1] = createButton(parent,
							IDialogConstants.CLOSE_ID,
							IDialogConstants.CLOSE_LABEL, true);
					setButtons(buttons);
				}
			};
			if (restoreId == dialog.open()) {
				IPreferenceStore store = getPreferenceStore();
				store
						.setToDefault(PreferenceConstants.CODEASSIST_CATEGORY_ORDER);
				store
						.setToDefault(PreferenceConstants.CODEASSIST_EXCLUDED_CATEGORIES);
				CompletionProposalComputerRegistry registry = CompletionProposalComputerRegistry
						.getDefault();
				registry.reload();
				return true;
			}
		}
		return false;
	}

	protected abstract IPreferenceStore getPreferenceStore();

	private List getSeparateCategories() {
		ArrayList sorted = new ArrayList();
		for (Iterator it = fCategories.iterator(); it.hasNext();) {
			CompletionProposalCategory category = (CompletionProposalCategory) it
					.next();
			if (category.isSeparateCommand()
					&& category.hasComputers(fPartition))
				sorted.add(category);
		}
		Collections.sort(sorted, ORDER_COMPARATOR);
		return sorted;
	}

	private String createEmptyMessage() {
		return Messages.format(
				ScriptTextMessages.ContentAssistProcessor_empty_message,
				new String[] { getCategoryLabel(fRepetition) });
	}

	private String createIterationMessage() {
		return Messages
				.format(
						ScriptTextMessages.ContentAssistProcessor_toggle_affordance_update_message,
						new String[] { getCategoryLabel(fRepetition),
								fIterationGesture,
								getCategoryLabel(fRepetition + 1) });
	}

	private String getCategoryLabel(int repetition) {
		int iteration = repetition % fCategoryIteration.size();
		if (iteration == 0)
			return ScriptTextMessages.ContentAssistProcessor_defaultProposalCategory;
		return toString((CompletionProposalCategory) ((List) fCategoryIteration
				.get(iteration)).get(0));
	}

	private String toString(CompletionProposalCategory category) {
		return category.getDisplayName();
	}

	private String getIterationGesture() {
		TriggerSequence binding = getIterationBinding();
		return binding != null ? Messages
				.format(
						ScriptTextMessages.ContentAssistProcessor_toggle_affordance_press_gesture,
						new Object[] { binding.format() })
				: ScriptTextMessages.ContentAssistProcessor_toggle_affordance_click_gesture;
	}

	private KeySequence getIterationBinding() {
		final IBindingService bindingSvc = (IBindingService) PlatformUI
				.getWorkbench().getAdapter(IBindingService.class);
		TriggerSequence binding = bindingSvc
				.getBestActiveBindingFor(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		if (binding instanceof KeySequence)
			return (KeySequence) binding;
		return null;
	}
}
