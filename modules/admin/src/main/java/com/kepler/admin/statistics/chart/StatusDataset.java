package com.kepler.admin.statistics.chart;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务静态静态统计
 * 
 * @author longyaokun
 * 
 */
public class StatusDataset {

	private Map<String, ChartLine> gc = new HashMap<String, ChartLine>();

	private ChartLine memoryFree;

	private ChartLine memoryHeap;

	private ChartLine memoryNonHeap;

	private ChartLine thread4vm;

	private ChartLine thread4kepler;

	private ChartLine thread4stacks;

	private ChartLine trafficInput;

	private ChartLine trafficOutput;

	private ChartLine loadAverage;

	private ChartLine request;

	public StatusDataset(String title) {
		super();
		// Memory
		this.memoryFree = ChartLine.def(title);
		this.memoryHeap = ChartLine.def(title);
		this.memoryNonHeap = ChartLine.def(title);
		// Thread
		this.thread4vm = ChartLine.def(title);
		this.thread4kepler = ChartLine.def(title);
		this.thread4stacks = ChartLine.def(title);
		// 流量
		this.trafficInput = ChartLine.def(title);
		this.trafficOutput = ChartLine.def(title);
		// Other
		this.request = ChartLine.def(title);
		this.loadAverage = ChartLine.def(title);
	}

	public StatusDataset gc(Object[] data, String title) {
		ChartLine chart = this.gc.get(title);
		chart = chart != null ? chart : ChartLine.def(title);
		chart.add(data);
		this.gc.put(title, chart);
		return this;
	}

	public StatusDataset memoryFree(Object[] data) {
		this.memoryFree.add(data);
		return this;
	}

	public StatusDataset memoryHeap(Object[] data) {
		this.memoryHeap.add(data);
		return this;
	}

	public StatusDataset memoryNonHeap(Object[] data) {
		this.memoryNonHeap.add(data);
		return this;
	}

	public StatusDataset thread4vm(Object[] data) {
		this.thread4vm.add(data);
		return this;
	}

	public StatusDataset thread4stacks(Object[] data) {
		this.thread4stacks.add(data);
		return this;
	}

	public StatusDataset thread4kepler(Object[] data) {
		this.thread4kepler.add(data);
		return this;
	}

	public StatusDataset trafficInput(Object[] data) {
		this.trafficInput.add(data);
		return this;
	}

	public StatusDataset trafficOutput(Object[] data) {
		this.trafficOutput.add(data);
		return this;
	}

	public StatusDataset loadAverage(Object[] data) {
		this.loadAverage.add(data);
		return this;
	}

	public StatusDataset request(Object[] data) {
		this.request.add(data);
		return this;
	}

	public ChartLine getMemoryFree() {
		return this.memoryFree;
	}

	public ChartLine getMemoryHeap() {
		return this.memoryHeap;
	}

	public ChartLine getMemoryNonHeap() {
		return this.memoryNonHeap;
	}

	public ChartLine getThread4vm() {
		return this.thread4vm;
	}

	public ChartLine getThread4kepler() {
		return this.thread4kepler;
	}

	public ChartLine getThread4stacks() {
		return this.thread4stacks;
	}

	public ChartLine getTrafficInput() {
		return this.trafficInput;
	}

	public ChartLine getTrafficOutput() {
		return this.trafficOutput;
	}

	public ChartLine getLoadAverage() {
		return this.loadAverage;
	}

	public ChartLine getRequest() {
		return this.request;
	}

	public Collection<ChartLine> getGc() {
		return this.gc.values();
	}
}
