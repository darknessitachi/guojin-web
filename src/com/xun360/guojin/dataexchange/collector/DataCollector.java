package com.xun360.guojin.dataexchange.collector;

import com.xun360.guojin.dataexchange.exception.CollectDataException;

public interface DataCollector {

	public void collect() throws CollectDataException;
}
