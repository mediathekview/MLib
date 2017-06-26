package de.mediathekview.mlib.tool;

public class JavaVersion {
	
	private String vendor;
	private String vmname;
	private String version;
	private String runtimeversion;
	
	
	
	public JavaVersion(String vendor, String vmname, String version, String runtimeversion) {
		super();
		this.vendor = vendor;
		this.vmname = vmname;
		this.version = version;
		this.runtimeversion = runtimeversion;
	}
	
	public JavaVersion() {
		this("", "", "", "");
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVmname() {
		return vmname;
	}

	public void setVmname(String vmname) {
		this.vmname = vmname;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRuntimeversion() {
		return runtimeversion;
	}

	public void setRuntimeversion(String runtimeversion) {
		this.runtimeversion = runtimeversion;
	}
	
	@Override
	public String toString() {
		return "[ Vendor: "+vendor+" - Version: "+version+" - VMName: "+vmname+" - RuntimeVersion: "+runtimeversion+" ]";
	}

}
