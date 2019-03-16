package com.mossle.spi.device;

public interface DeviceConnector {

	DeviceDTO findDevice(String code);

	void saveDevice(DeviceDTO deviceDto);

	String saveDevice(DeviceDTO deviceDto, String userId);

	void removeAll(String account_id);
	
	String getToeknValue(String userId);
}
