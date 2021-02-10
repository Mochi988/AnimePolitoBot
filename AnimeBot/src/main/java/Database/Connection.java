package Database;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connection {

    public java.sql.Connection getConnection() throws URISyntaxException, SQLException {

        String uri = "postgres://xiiziuanojnhjr:bcc22f23639e22ab12d091869aa17a6814bb39a63f7b49099a7f324f9c3f9fae@ec2-34-247-118-233.eu-west-1.compute.amazonaws.com:5432/d4me9liq9hir5";
        URI dbUri = new URI(uri);
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
        return DriverManager.getConnection(dbUrl, username, password);
    }
}
