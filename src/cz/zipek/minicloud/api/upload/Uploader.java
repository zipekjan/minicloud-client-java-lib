/* 
 * The MIT License
 *
 * Copyright 2016 Jan Zípek <jan at zipek.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.zipek.minicloud.api.upload;

import cz.zipek.minicloud.api.Eventor;
import cz.zipek.minicloud.api.External;
import cz.zipek.minicloud.api.File;
import cz.zipek.minicloud.api.Listener;
import cz.zipek.minicloud.api.encryption.Encryptor;
import cz.zipek.minicloud.api.upload.events.UploadAllDoneEvent;
import cz.zipek.minicloud.api.upload.events.UploadFailedEvent;
import cz.zipek.minicloud.api.upload.events.UploadFileDoneEvent;
import cz.zipek.minicloud.api.upload.events.UploadFileStartedEvent;
import cz.zipek.minicloud.api.upload.events.UploadProgressEvent;
import cz.zipek.minicloud.api.upload.events.UploadThreadProgressEvent;
import cz.zipek.minicloud.api.upload.events.UploadThreadSentEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Jan Zípek
 */
public class Uploader extends Eventor<UploadEvent> implements Listener {	
	private final List<UploadItem> items = new ArrayList<>();
	private final External source;
	private final Encryptor encryptor;
	
	private String targetFolder;
	private UploadThread thread;
	
	public Uploader(External source, Encryptor encryption) {
		this.source = source;
		encryptor = encryption;
	}
	
	public void add(java.io.File file, boolean isPublic) {
		add(new UploadItem(file, isPublic));
	}
	
	public void add(java.io.File local, File remote) {
		add(new UploadItem(local, remote));
	}
	
	public void add(java.io.File local, File remote, boolean isPublic) {
		add(new UploadItem(local, remote, isPublic));
	}
	
	public void add(java.io.File local, String target, boolean isPublic) {
		add(new UploadItem(local, target, isPublic));
	}
	
	public void add(InputStream local, File remote) {
		add(new UploadItem(local, remote));
	}
	
	public void add(InputStream local, File remote, boolean isPublic) {
		add(new UploadItem(local, remote, isPublic));
	}
	
	public void add(InputStream local, File remote, boolean isPublic, boolean createVersion) {
		add(new UploadItem(local, remote, isPublic, createVersion));
	}
	
	public void add(UploadItem item) {
		items.add(item);
	}
	
	public void start(String target) {
		if (thread == null && items.size() > 0) {
			targetFolder = target;
			uploadFile(items.get(0));
		}
	}
	
	public void stop() {
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	private void nextFile() {
		if (!items.isEmpty()) {
			items.remove(0);
			if (!items.isEmpty()) {
				uploadFile(items.get(0));
			} else {
				fireEvent(new UploadAllDoneEvent());
			}
		}
	}
	
	private void uploadFile(UploadItem file) {
		fireEvent(new UploadFileStartedEvent(file));
		
		if (file.getExisting() == null) {
			String target = file.getTarget();
			if (target == null) {
				file.setTarget(targetFolder);
			}
		}
		
		thread = new UploadThread(this, file, encryptor);
		
		thread.addListener(this);
		thread.start();
	}
	
	@Override
	public void handleEvent(Object event, Object sender) {
		if (event instanceof UploadFailedEvent) {
			fireEvent((UploadFailedEvent)event);
			thread = null;
		} else if (event instanceof UploadThreadProgressEvent) {
			UploadThreadProgressEvent e = (UploadThreadProgressEvent)event;
			UploadItem item = items.get(0);
			
			fireEvent(new UploadProgressEvent(item, e.getSent(), e.getTotal()));
		} else if (event instanceof UploadThreadSentEvent) {
			UploadThreadSentEvent e = (UploadThreadSentEvent)event;
			UploadItem item = items.get(0);
			
			//System.out.println(e.getResponse());
			
			JSONObject data = null;
			try {
				data = new JSONObject(e.getResponse());
			} catch (JSONException ex) {
				Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
			}
			
			fireEvent(new UploadFileDoneEvent(item, data));
			nextFile();
		}
	}
	
	/**
	 * @return the items
	 */
	public List<UploadItem> getItems() {
		return items;
	}

	/**
	 * @return the source
	 */
	public External getSource() {
		return source;
	}
}
