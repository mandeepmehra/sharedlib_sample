import com.xebia.ioc.ContextRegistry
import com.xebia.scanner.DockerImageScanner

def call(String imageNameWithPath){
    ContextRegistry.registerContext(this)

    def scanner = new DockerImageScanner(imageNameWithPath)
    scanner.scan()
}