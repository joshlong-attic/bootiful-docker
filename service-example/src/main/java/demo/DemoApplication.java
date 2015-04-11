package demo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class DemoApplication {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Profile("lattice")
    @Configuration
    public static class LatticeConfiguration {

        @Bean
        DataSource postgres(
                LatticeClient latticeClient,
                @Value("${db.serviceId}") String serviceId,
                @Value("${spring.datasource.username}") String user,
                @Value("${spring.datasource.password}") String pw) {

            LongRunningProcess firstAvailablePostgresqlInstance =
                    latticeClient.longRunningProcesses(serviceId).iterator().next();

            DataSourceBuilder factory = DataSourceBuilder
                    .create(this.getClass().getClassLoader())
                    .driverClassName(Driver.class.getName())
                    .url(
                            String.format("jdbc:postgresql://%s:%s/%s",
                                firstAvailablePostgresqlInstance.getAddress(),
                                firstAvailablePostgresqlInstance.getPorts()[0].getHostPort(),
                                "postgres"))
                    .username(user)
                    .password(pw);
            return factory.build();
        }

    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@Component
class LatticeClient {

    @Autowired
    private RestTemplate restTemplate;

    List<LongRunningProcess> longRunningProcesses(String guid) {
        return this.restTemplate.exchange(
            "http://receptor.192.168.11.11.xip.io/v1/actual_lrps/{guid}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<LongRunningProcess>>() { },
                guid)
            .getBody();
    }
}

@RestController
class LrpRestEndpoint {

    @Autowired
    private LatticeClient latticeClient;

    @RequestMapping("/lrps/{guid}")
    List<LongRunningProcess> allByGuid(@PathVariable String guid) {
        return this.latticeClient.longRunningProcesses(guid);
    }

}

/**
 * @author Matt Stine
 */

class LongRunningProcess {

    @JsonProperty("process_guid")
    private String processGuid;

    @JsonProperty("instance_guid")
    private String instanceGuid;

    @JsonProperty("cell_id")
    private String cellId;

    private String domain;

    private int index;

    private String address;

    private Port[] ports;

    private String state;

    private long since;

    public String getProcessGuid() {
        return processGuid;
    }

    public void setProcessGuid(String processGuid) {
        this.processGuid = processGuid;
    }

    public String getInstanceGuid() {
        return instanceGuid;
    }

    public void setInstanceGuid(String instanceGuid) {
        this.instanceGuid = instanceGuid;
    }

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Port[] getPorts() {
        return ports;
    }

    public void setPorts(Port[] ports) {
        this.ports = ports;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getSince() {
        return since;
    }

    public void setSince(long since) {
        this.since = since;
    }

    @Override
    public String toString() {
        return "LongRunningProcess{" +
                "processGuid='" + processGuid + '\'' +
                ", instanceGuid='" + instanceGuid + '\'' +
                ", cellId='" + cellId + '\'' +
                ", domain='" + domain + '\'' +
                ", index=" + index +
                ", address='" + address + '\'' +
                ", ports=" + Arrays.toString(ports) +
                ", state='" + state + '\'' +
                ", since=" + since +
                '}';
    }
}


/**
 * @author Matt Stine
 */
class Port {
    @JsonProperty("container_port")
    private int containerPort;
    @JsonProperty("host_port")
    private int hostPort;

    public int getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(int containerPort) {
        this.containerPort = containerPort;
    }

    public int getHostPort() {
        return hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    @Override
    public String toString() {
        return "Port{" +
                "containerPort=" + containerPort +
                ", hostPort=" + hostPort +
                '}';
    }
}

