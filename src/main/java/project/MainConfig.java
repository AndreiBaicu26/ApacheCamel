package project;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainConfig {

    @Value("${port}")
    private int port;

    public int getPort() {
        return port;
    }
}
