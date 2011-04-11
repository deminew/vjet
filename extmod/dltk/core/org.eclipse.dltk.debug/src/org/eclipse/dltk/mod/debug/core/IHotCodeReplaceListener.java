package org.eclipse.dltk.mod.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.dltk.mod.debug.core.model.IScriptDebugTarget;

/**
 * Notification of hot code replace failure and success. As resources are
 * modified in the workspace, targets that support hot code replace are
 * updated.
 */
public interface IHotCodeReplaceListener {

	/**
	 * Notification that a hot code replace attempt failed in the given target.
	 * 
	 * @param target the target in which the hot code replace failed
	 * @param exception the exception generated by the hot code replace
	 *  failure, or <code>null</code> if the hot code replace failed because
	 *  the target VM does not support hot code replace
	 */
	public void hotCodeReplaceFailed(IScriptDebugTarget target, DebugException exception);
	
	/**
	 * Notification that a hot code replace attempt succeeded in the
	 * given target.
	 * 
	 * @param target the target in which the hot code replace succeeded
	 */
	public void hotCodeReplaceSucceeded(IScriptDebugTarget target);
	
}

