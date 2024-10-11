package ic.doc.camera;

public class Camera implements WriteListener {

  private final Sensor sensor;
  private final MemoryCard memoryCard;
  private boolean poweredOn = false;
  private boolean writingData = false;

  public Camera(Sensor sensor, MemoryCard memoryCard) {
    this.sensor = sensor;
    this.memoryCard = memoryCard;
  }

  public void pressShutter() {
    if (isPowerOn()) {
      byte[] data = sensor.readData();
      setWritingData(true);
      memoryCard.write(data);
    }
  }

  public void powerOn() {
    sensor.powerUp();
    setPoweredOn(true);
  }

  public void powerOff() {
    if (!isWritingData()) {
      sensor.powerDown();
      setPoweredOn(false);
    }
    // If it's writing data, the power down will be handled after writing is complete
  }

  @Override
  public void writeComplete() {
    setWritingData(false);
    // Check if the camera was requested to power off during writing
    if (!isPowerOn()) {
      sensor.powerDown();
    }
  }

  private boolean isPowerOn() {
    return poweredOn;
  }

  private void setPoweredOn(boolean poweredOn) {
    this.poweredOn = poweredOn;
  }

  private boolean isWritingData() {
    return writingData;
  }

  private void setWritingData(boolean writingData) {
    this.writingData = writingData;
    // Automatically handle pending power-off request
    if (!writingData && !isPowerOn()) {
      sensor.powerDown();
    }
  }
}
