package com.example.pbft.Service;

import java.util.List;

public interface PerformanceService {

	List<String> printPerformance();
	void logTaskStart();
	void logTaskEnd(int noOfTasks);

}
