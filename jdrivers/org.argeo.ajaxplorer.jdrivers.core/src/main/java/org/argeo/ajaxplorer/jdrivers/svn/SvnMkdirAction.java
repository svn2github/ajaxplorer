package org.argeo.ajaxplorer.jdrivers.svn;

import java.io.File;

import org.argeo.ajaxplorer.jdrivers.AjxpDriverException;
import org.argeo.ajaxplorer.jdrivers.file.FileMkdirAction;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

public class SvnMkdirAction extends FileMkdirAction {
	static {
		FSRepositoryFactory.setup();
	}

	@Override
	protected void postProcess(File newDir) {
		SVNClientManager manager = SVNClientManager.newInstance();
		File baseDir = new File(getFileDriverContext().getBasePath());
		try {
			log.debug("SVN Update: " + baseDir);
			manager.getUpdateClient().doUpdate(baseDir, SVNRevision.HEAD, true);
			log.debug("SVN Add: " + newDir);
			manager.getWCClient().doAdd(newDir, true, newDir.isDirectory(), true,
					true);
			log.debug("SVN Commit: " + baseDir);
			manager.getCommitClient().doCommit(new File[] { baseDir }, true,
					"Commit new dir " + newDir, true, true);
			log.debug("SVN Update: " + baseDir);
			manager.getUpdateClient().doUpdate(baseDir, SVNRevision.HEAD, true);
		} catch (SVNException e) {
			throw new AjxpDriverException("Cannot commit new dir" + newDir, e);
		}
	}

}
