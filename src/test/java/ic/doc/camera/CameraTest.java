package ic.doc.camera;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

public class CameraTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private final Sensor sensor = context.mock(Sensor.class);
    private final MemoryCard memoryCard = context.mock(MemoryCard.class);
    private final byte[] data = new byte[0];

    private Camera createCameraAndSetCommonExpectations() {
        return new Camera(sensor, memoryCard);
    }

    @Test
    public void switchingTheCameraOnPowersUpTheSensor() {
        Camera camera = createCameraAndSetCommonExpectations();
        context.checking(new Expectations() {{
            oneOf(sensor).powerUp();
        }});

        camera.powerOn();
    }

    @Test
    public void switchingTheCameraOffPowersDownTheSensor() {
        Camera camera = createCameraAndSetCommonExpectations();
        context.checking(new Expectations() {{
            oneOf(sensor).powerDown();
        }});

        camera.powerOff();
    }

    @Test
    public void pressingShutterWhenPowerOffDoesNotReadData() {
        Camera camera = createCameraAndSetCommonExpectations();
        context.checking(new Expectations() {{
            never(sensor).readData();
        }});

        camera.pressShutter();
    }

    @Test
    public void pressingShutterWhenPowerOffDoesNotWriteData() {
        Camera camera = createCameraAndSetCommonExpectations();
        context.checking(new Expectations() {{
            never(memoryCard).write(with(any(byte[].class)));
        }});

        camera.pressShutter();
    }

    @Test
    public void pressingShutterWhenPowerOnWritesData() {
        Camera camera = createCameraAndSetCommonExpectations();
        context.checking(new Expectations() {{
            // Use allowing for setup actions that are not the focus of this test
            allowing(sensor).powerUp();
            oneOf(sensor).readData();
            will(returnValue(data));
            oneOf(memoryCard).write(data);
        }});

        camera.powerOn();
        camera.pressShutter();
    }

    @Test
    public void switchingCameraOffWhileWritingDataDoesNotPowerDownSensor() {
        Camera camera = createCameraAndSetCommonExpectations();
        context.checking(new Expectations() {{
            oneOf(sensor).powerUp();
            oneOf(sensor).readData();
            will(returnValue(data));
            oneOf(memoryCard).write(data);
            never(sensor).powerDown();
        }});

        camera.powerOn();
        camera.pressShutter();
        camera.powerOff();
        // Assuming this should simulate the completion of write operation
        camera.writeComplete();
    }

    @Test
    public void switchingCameraOffAfterWritingDataPowersDownSensor() {
        Camera camera = createCameraAndSetCommonExpectations();
        context.checking(new Expectations() {{
            oneOf(sensor).powerUp();
            oneOf(sensor).readData();
            will(returnValue(data));
            oneOf(memoryCard).write(data);
            // Ensuring sensor powers down after writing is complete
            oneOf(sensor).powerDown();
        }});

        camera.powerOn();
        camera.pressShutter();
        // Simulate completion of data writing
        camera.writeComplete();
        camera.powerOff();
    }
}
