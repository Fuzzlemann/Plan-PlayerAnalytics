/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.ResponseHandler;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.errors.InternalErrorResponse;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class RootPageHandler extends PageHandler {

    private final ResponseHandler responseHandler;

    public RootPageHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    public Response getResponse(Request request, List<String> target) {
        Optional<Authentication> auth = request.getAuth();
        if (!auth.isPresent()) {
            return DefaultResponses.BASIC_AUTH.get();
        }

        try {
            WebUser webUser = auth.get().getWebUser();

            int permLevel = webUser.getPermLevel();
            switch (permLevel) {
                case 0:
                    return responseHandler.getPageHandler("server").getResponse(request, Collections.emptyList());
                case 1:
                    return responseHandler.getPageHandler("players").getResponse(request, Collections.emptyList());
                case 2:
                    return responseHandler.getPageHandler("player").getResponse(request, Collections.singletonList(webUser.getName()));
                default:
                    return responseHandler.forbiddenResponse(permLevel, 0);
            }
        } catch (WebUserAuthException e) {
            Log.toLog(this.getClass(), e);
            return new InternalErrorResponse("/", e);
        }
    }

    @Override
    public boolean isAuthorized(Authentication auth, List<String> target) {
        return true;
    }
}