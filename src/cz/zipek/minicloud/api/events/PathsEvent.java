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
package cz.zipek.minicloud.api.events;

import cz.zipek.minicloud.api.External;
import cz.zipek.minicloud.api.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Jan Zípek
 */
public class PathsEvent extends SuccessEvent {

	protected Path[] paths;
	
	public PathsEvent(External sender, JSONObject data, String action_id) {
		super(sender, data, action_id);
		
		try {
			JSONArray list = data.optJSONArray("data");

			paths = new Path[list.length()];
			for(int i = 0; i < list.length(); i++) {
				paths[i] = new Path(sender, list.optJSONObject(i));
			}
		} catch (JSONException ex) {
			Logger.getLogger(PathsEvent.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * @return list of paths contained in event
	 */
	public Path[] getPaths() {
		return paths;
	}
	
}
