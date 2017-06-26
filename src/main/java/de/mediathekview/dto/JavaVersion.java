package de.mediathekview.dto;

public class JavaVersion {
	
	private String vendor;
	private String vmname;
	private String version;
	private String runtimeversion;
	
	
	
	public JavaVersion(String vendor, String vmname, String version, String runtimeversion) {
		this.vendor = vendor;
		this.vmname = vmname;
		this.version = version;
		this.runtimeversion = runtimeversion;
	}

	public String getVendor() {
		return vendor;
	}

	public String getVmname() {
		return vmname;
	}

	public String getVersion() {
		return version;
	}

	public String getRuntimeversion() {
		return runtimeversion;
	}
	
	@Override
	public String toString() {
		return "[ Vendor: "+vendor+" - Version: "+version+" - VMName: "+vmname+" - RuntimeVersion: "+runtimeversion+" ]";
	}

}
