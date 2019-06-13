package com.xebia;

import com.xebia.ioc.ContextRegistry;
import com.xebia.ioc.IContext;
import com.xebia.scanner.DockerImageScanner;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class DockerImageScannerTest {

    private IContext _context;
    private IStepExecutor _steps;

    @Before
    public void setup() {
        _context = mock(IContext.class);
        _steps = mock(IStepExecutor.class);

        when(_context.getStepExecutor()).thenReturn(_steps);

        ContextRegistry.registerContext(_context);

    }

    @Test
    public void callScanStepWithNoError() {
        // prepare
        String imageNameWithTag = "mandeepmehra/ge-poc:1";
        DockerImageScanner dockerImageScanner= new DockerImageScanner(imageNameWithTag, "ip","host","6060");
        when(_steps.scanImage(anyString(), anyString(), anyString(), anyString())).thenReturn(0);

        // execute
        dockerImageScanner.scan();

        // verify
        verify(_steps).scanImage(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void callScanStepWithError() {
        // prepare
        String imageNameWithTag = "mandeepmehra/ge-poc:1";
        DockerImageScanner dockerImageScanner= new DockerImageScanner(imageNameWithTag,"ip","host","6060");
        when(_steps.scanImage(anyString(), anyString(), anyString(), anyString())).thenReturn(1);

        // execute
        dockerImageScanner.scan();

        // verify
        verify(_steps).error(anyString());
    }

}
