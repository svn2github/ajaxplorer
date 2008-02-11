package org.argeo.ajaxplorer.jdrivers.file;

import java.io.File;
import java.io.FileFilter;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.argeo.ajaxplorer.jdrivers.AjxpAnswer;
import org.argeo.ajaxplorer.jdrivers.AjxpDriverException;
import org.omg.CORBA.Request;

public class FileLsAction extends FileAction {
	public AjxpAnswer execute(HttpServletRequest request) {
		String modeStr = request.getParameter("mode");
		final LsMode mode;
		if (modeStr == null)
			mode = LsMode.NULL;
		else if (modeStr.equals("complete"))
			mode = LsMode.COMPLETE;
		else if (modeStr.equals("file_list"))
			mode = LsMode.FILE_LIST;
		else if (modeStr.equals("search"))
			mode = LsMode.SEARCH;
		else
			throw new AjxpDriverException("Unkown mode " + modeStr);

		String path = request.getParameter("dir");
		if (path == null) {
			path = "/";
		}

		boolean dirOnly = false;
		if (mode == LsMode.NULL || mode == LsMode.COMPLETE) {
			dirOnly = true;
		}

		File dir = new File(getFileDriverContext().getBasePath() + path);

		if (!dir.exists())
			throw new AjxpDriverException("Dir " + dir + " does not exist.");

		File[] files = dir.listFiles(createFileFilter(request, dir));
		List<AjxpFile> ajxpFiles = new Vector<AjxpFile>();
		for (File file : files) {
			if (file.isDirectory()) {
				ajxpFiles.add(new AjxpFile(file, path));
			} else {
				if (!dirOnly)
					ajxpFiles.add(new AjxpFile(file, path));
			}
		}

		return new AxpLsAnswer(ajxpFiles, mode);
	}

	/** To be overridden. Accept all by default. */
	protected FileFilter createFileFilter(HttpServletRequest request, File dir) {
		return new FileFilter() {
			public boolean accept(File pathname) {
				return true;
			}

		};
	}

	protected class AxpLsAnswer implements AjxpAnswer {
		private final List<AjxpFile> files;
		private final LsMode mode;

		public AxpLsAnswer(List<AjxpFile> files, LsMode mode) {
			this.files = files;
			this.mode = mode;
		}

		public void updateResponse(HttpServletResponse response) {
			final String encoding = getFileDriverContext().getEncoding();
			response.setCharacterEncoding(encoding);
			response.setContentType("text/xml");

			ServletOutputStream out = null;
			OutputStreamWriter writer = null;
			try {
				out = response.getOutputStream();
				writer = new OutputStreamWriter(out, encoding);
				writer.write("<?xml version=\"1.0\" encoding=\"" + encoding
						+ "\"?>");
				writer.write("<tree>");
				for (AjxpFile file : files) {
					writer.write(file.toXml(mode, encoding));
				}
				writer.write("</tree>");
				writer.flush();

			} catch (Exception e) {
				throw new AjxpDriverException("Could not write response.", e);
			} finally {
				IOUtils.closeQuietly(writer);
				IOUtils.closeQuietly(out);
			}
		}

	}
}
