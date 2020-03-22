package com.hyperion.selfcontrol.backend;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class CredentialService {

    public static final String FILE_LOCATION = "C:\\Users\\moore\\credentials.json";

    private JSONObject fileContents;

    public CredentialService() throws IOException {
        String join = String.join("\n", Files.readAllLines(Paths.get(FILE_LOCATION)));
        fileContents = new JSONObject(join);
    }

    public String getNetNannyUsername() {
        return fileContents.getString("net-nanny-username");
    }

    public String getNetNannyPassword() {
        return fileContents.getString("net-nanny-password");
    }
}
