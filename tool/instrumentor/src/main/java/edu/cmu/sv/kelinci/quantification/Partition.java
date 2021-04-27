package edu.cmu.sv.kelinci.quantification;

import java.util.ArrayList;
import java.util.List;

public class Partition {

	private List<Long> values;

	public Partition() {
		this.values = new ArrayList<>();
	}

	public int size() {
		return values.size();
	}

	public void clear() {
		this.values = new ArrayList<>();
	}

	public boolean canTake(long newValue, double epsilon) {
		if (values.isEmpty()) {
			return true;
		} else {
			return Math.abs(newValue - getMin()) <= epsilon && Math.abs(newValue - getMax()) <= epsilon;
		}
	}

	/* Assumes adding in ascending order. */
	public void addValue(long newValue) {
		values.add(newValue);
	}

	public long getMin() {
		return this.values.get(0);
	}

	public long getMax() {
		return this.values.get(values.size() - 1);
	}
	
	public void removeMin() {
		this.values.remove(0);
	}
	
	public void removeMax() {
		this.values.remove(values.size() - 1);
	}
	
	public List<Long> getValues() {
		return this.values;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < this.values.size(); i++) {
			sb.append(this.values.get(i));
			if (i < this.values.size() - 1) {
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}

}
