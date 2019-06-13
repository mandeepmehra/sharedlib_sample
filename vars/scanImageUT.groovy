import com.xebia.ioc.ContextRegistry
import com.xebia.scanner.DockerImageScanner

def call(String imageNameWithPath, String scannerClientIP, String scannerHost, String scannerPort){
    ContextRegistry.registerDefaultContext(this)

    def scanner = new DockerImageScanner(imageNameWithPath, scannerClientIP, scannerHost, scannerPort)
    scanner.scan()
}