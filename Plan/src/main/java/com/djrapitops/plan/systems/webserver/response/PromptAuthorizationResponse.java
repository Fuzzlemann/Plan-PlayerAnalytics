package main.java.com.djrapitops.plan.systems.webserver.response;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class PromptAuthorizationResponse extends Response {

    public PromptAuthorizationResponse() {
        super.setHeader("HTTP/1.1 401 Access Denied\r\n"
                + "WWW-Authenticate: Basic realm=\"/\";");
        super.setContent("<h1>401 Unauthorized</h1><p>Authentication Failed.<br>"
                + "- Ensure you have registered a user with <b>/plan register</b><br>"
                + "- Check that the username and password are correct<br>"
                + "- Username and password are case-sensitive<br>"
                + "<br>If you have forgotten your password, ask a staff member to delete your old user and re-register.</p>");
    }
}