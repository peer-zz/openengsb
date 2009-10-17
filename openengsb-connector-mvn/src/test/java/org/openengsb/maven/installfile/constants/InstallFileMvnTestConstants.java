/**

   Copyright 2009 openEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

package org.openengsb.maven.installfile.constants;

/**
 * 
 * @author David Waldhans
 * 
 */
public class InstallFileMvnTestConstants {

	public String validFullPathToFile;
	public String validGroupId;
	public String validArtifactId;
	public String validVersion;

	public String getValidFullPathToFile() {
		return validFullPathToFile;
	}

	public void setValidFullPathToFile(String validFullPathToFile) {
		this.validFullPathToFile = validFullPathToFile;
	}

	public String getValidGroupId() {
		return validGroupId;
	}

	public void setValidGroupId(String validGroupId) {
		this.validGroupId = validGroupId;
	}

	public String getValidArtifactId() {
		return validArtifactId;
	}

	public void setValidArtifactId(String validArtifactId) {
		this.validArtifactId = validArtifactId;
	}

	public String getValidVersion() {
		return validVersion;
	}

	public void setValidVersion(String validVersion) {
		this.validVersion = validVersion;
	}

	public String getJarPackaging() {
		return jarPackaging;
	}

	public void setJarPackaging(String jarPackaging) {
		this.jarPackaging = jarPackaging;
	}

	public String getInvalidFullPathToFile() {
		return invalidFullPathToFile;
	}

	public void setInvalidFullPathToFile(String invalidFullPathToFile) {
		this.invalidFullPathToFile = invalidFullPathToFile;
	}

	public String getSettingsFile() {
		return settingsFile;
	}

	public void setSettingsFile(String settingsFile) {
		this.settingsFile = settingsFile;
	}

	public String jarPackaging;
	public String invalidFullPathToFile;
	public String settingsFile;

}
