package test.nemo.mngnetwork.info;


public class MemoryInfo {
	
	public static enum Unit { B, KB, MB, GB }
	
	//Unit unit;
	String unit;
	long total;
	long usage;
	long free;
	long max;
	
	public MemoryInfo() {
		this(MemoryInfo.Unit.MB);
	}

	public MemoryInfo(Unit unit) {
		this.unit=unit.toString();
		Runtime rt=Runtime.getRuntime();
		total=format(rt.totalMemory());
		usage=format(rt.totalMemory()-rt.freeMemory());
		free=format(rt.freeMemory());
		max=format(rt.maxMemory());
	}
	
	long format(long val) {
		switch(unit.toUpperCase()) {
		case "GB" : return val/1024/1024/1024;
		case "MB" : return val/1024/1024;
		case "KB" : return val/1024;
		case "B" : default : return val;
		}
	}

	/**
	 * @return the total */
	public long getTotal() {
		return total;
	}
	
	/**
	 * @return the usage */
	public long getUsage() {
		return usage;
	}
	
	/**
	 * @return the free */
	public long getFree() {
		return free;
	}
	
	/**
	 * @return the max */
	public long getMax() {
		return max;
	}

}
