package com.kepler.admin.statistics.barchart;

import java.util.ArrayList;
import java.util.List;

/**
 * @author longyaokun
 * @date 2016年3月21日
 *
 */
public class BarChartData {

	private List<String> categories = new ArrayList<String>();

	private List<Series> series = new ArrayList<Series>();

	public List<String> getCategories() {
		return this.categories;
	}

	public List<Series> getSeries() {
		return this.series;
	}
	
	public void addCategory(String category){
		this.categories.add(category);
	}

	public void addSeries(Series series){
		this.series.add(series);
	}
}
