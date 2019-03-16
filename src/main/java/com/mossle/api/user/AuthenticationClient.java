package com.mossle.api.user;

import java.net.UnknownHostException;

public interface AuthenticationClient {
    String doAuthenticate(String username, String password, String type,
            String application) throws UnknownHostException;
}
